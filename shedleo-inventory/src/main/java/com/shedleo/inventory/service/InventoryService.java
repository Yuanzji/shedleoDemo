package com.shedleo.inventory.service;

import com.shedleo.common.constant.InventoryConstant;
import com.shedleo.common.constant.RedisConstant;
import com.shedleo.common.exception.BusinessException;
import com.shedleo.common.result.ResultCode;
import com.shedleo.common.util.SnowflakeIdGenerator;
import com.shedleo.inventory.dto.DeductRequest;
import com.shedleo.inventory.dto.RollbackRequest;
import com.shedleo.inventory.entity.Inventory;
import com.shedleo.inventory.entity.InventoryLog;
import com.shedleo.inventory.entity.PresaleSchedule;
import com.shedleo.inventory.mapper.InventoryLogMapper;
import com.shedleo.inventory.mapper.InventoryMapper;
import com.shedleo.inventory.mapper.PresaleScheduleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final PresaleScheduleMapper presaleScheduleMapper;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    private static final String LUA_DEDUCT_SCRIPT =
            "local stock = tonumber(redis.call('GET', KEYS[1]))\n" +
            "if stock == nil then return -1 end\n" +
            "if stock < tonumber(ARGV[1]) then return -2 end\n" +
            "redis.call('DECRBY', KEYS[1], ARGV[1])\n" +
            "return 1";

    private static final String LUA_ROLLBACK_SCRIPT =
            "local stock = redis.call('GET', KEYS[1])\n" +
            "if stock == nil then return -1 end\n" +
            "redis.call('INCRBY', KEYS[1], ARGV[1])\n" +
            "return 1";

    public Integer getStock(Long skuId) {
        String stockStr = redisTemplate.opsForValue().get(RedisConstant.INVENTORY_STOCK_KEY + skuId);
        if (stockStr != null) {
            return Integer.parseInt(stockStr);
        }
        Inventory inventory = inventoryMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Inventory>()
                        .eq("sku_id", skuId));
        if (inventory == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }
        redisTemplate.opsForValue().set(RedisConstant.INVENTORY_STOCK_KEY + skuId, String.valueOf(inventory.getStock()));
        return inventory.getStock();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deduct(DeductRequest request) {
        if (Boolean.TRUE.equals(request.getPresale())) {
            return deductByPresale(request);
        }
        return deductSpot(request);
    }

    private boolean deductSpot(DeductRequest request) {
        Long skuId = request.getSkuId();
        RLock lock = redissonClient.getLock(RedisConstant.INVENTORY_LOCK_KEY + skuId);
        boolean locked = false;
        try {
            locked = lock.tryLock(RedisConstant.LOCK_WAIT_TIME, RedisConstant.LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取库存锁失败, skuId={}", skuId);
                throw new BusinessException("系统繁忙，请稍后重试");
            }

            String stockKey = RedisConstant.INVENTORY_STOCK_KEY + skuId;
            Long luaResult = redisTemplate.execute(
                    new org.springframework.data.redis.core.script.DefaultRedisScript<>(LUA_DEDUCT_SCRIPT, Long.class),
                    java.util.Collections.singletonList(stockKey),
                    String.valueOf(request.getQuantity()));

            if (luaResult == null || luaResult == -1L) {
                Inventory inventory = inventoryMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Inventory>()
                                .eq("sku_id", skuId));
                if (inventory == null) {
                    throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
                }
                redisTemplate.opsForValue().set(stockKey, String.valueOf(inventory.getStock()));
                luaResult = redisTemplate.execute(
                        new org.springframework.data.redis.core.script.DefaultRedisScript<>(LUA_DEDUCT_SCRIPT, Long.class),
                        java.util.Collections.singletonList(stockKey),
                        String.valueOf(request.getQuantity()));
            }

            if (luaResult == -2L) {
                log.warn("库存不足, skuId={}, need={}", skuId, request.getQuantity());
                throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH);
            }

            Inventory inventory = inventoryMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Inventory>()
                            .eq("sku_id", skuId));
            if (inventory == null || inventory.getStock() < request.getQuantity()) {
                rollbackRedis(skuId, request.getQuantity());
                throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH);
            }

            int rows = inventoryMapper.deductWithVersion(skuId, request.getQuantity(), inventory.getVersion());
            if (rows == 0) {
                rollbackRedis(skuId, request.getQuantity());
                log.warn("乐观锁扣减失败，重试 DB 扣减, skuId={}", skuId);
                Inventory retry = inventoryMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Inventory>()
                                .eq("sku_id", skuId));
                if (retry == null || retry.getStock() < request.getQuantity()) {
                    throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH);
                }
                rows = inventoryMapper.deductWithVersion(skuId, request.getQuantity(), retry.getVersion());
                if (rows == 0) {
                    rollbackRedis(skuId, request.getQuantity());
                    throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH);
                }
            }

            saveLog(request.getSkuId(), request.getOrderId(), InventoryConstant.CHANGE_TYPE_DEDUCT, request.getQuantity());

            log.info("现货扣减成功, skuId={}, orderId={}, quantity={}", skuId, request.getOrderId(), request.getQuantity());
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("获取锁被中断");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private boolean deductByPresale(DeductRequest request) {
        Inventory inventory = inventoryMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Inventory>()
                        .eq("sku_id", request.getSkuId()));
        if (inventory == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        PresaleSchedule schedule = new PresaleSchedule();
        schedule.setId(SnowflakeIdGenerator.generate());
        schedule.setSkuId(request.getSkuId());
        schedule.setOrderId(request.getOrderId());
        schedule.setExpectedDeliveryTime(LocalDateTime.now().plusDays(7));
        schedule.setStatus(InventoryConstant.PRESALE_STATUS_PENDING);
        presaleScheduleMapper.insert(schedule);

        saveLog(request.getSkuId(), request.getOrderId(), InventoryConstant.CHANGE_TYPE_PRESALE, request.getQuantity());

        log.info("预售排期创建成功, skuId={}, orderId={}", request.getSkuId(), request.getOrderId());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean rollback(RollbackRequest request) {
        Long skuId = request.getSkuId();
        RLock lock = redissonClient.getLock(RedisConstant.INVENTORY_LOCK_KEY + skuId);
        boolean locked = false;
        try {
            locked = lock.tryLock(RedisConstant.LOCK_WAIT_TIME, RedisConstant.LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取库存回滚锁失败, skuId={}", skuId);
                throw new BusinessException("系统繁忙，请稍后重试");
            }

            Inventory inventory = inventoryMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Inventory>()
                            .eq("sku_id", skuId));
            if (inventory == null) {
                throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
            }

            int rows = inventoryMapper.rollbackWithVersion(skuId, request.getQuantity(), inventory.getVersion());
            if (rows == 0) {
                Inventory retry = inventoryMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Inventory>()
                                .eq("sku_id", skuId));
                rows = inventoryMapper.rollbackWithVersion(skuId, request.getQuantity(), retry.getVersion());
                if (rows == 0) {
                    throw new BusinessException("库存回滚失败，请人工介入");
                }
            }

            rollbackRedis(skuId, request.getQuantity());

            saveLog(request.getSkuId(), request.getOrderId(), InventoryConstant.CHANGE_TYPE_ROLLBACK, request.getQuantity());

            log.info("库存回滚成功, skuId={}, orderId={}, quantity={}", skuId, request.getOrderId(), request.getQuantity());
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("获取锁被中断");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void rollbackRedis(Long skuId, int quantity) {
        String stockKey = RedisConstant.INVENTORY_STOCK_KEY + skuId;
        redisTemplate.execute(
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(LUA_ROLLBACK_SCRIPT, Long.class),
                java.util.Collections.singletonList(stockKey),
                String.valueOf(quantity));
    }

    private void saveLog(Long skuId, Long orderId, int changeType, int quantity) {
        InventoryLog logEntity = new InventoryLog();
        logEntity.setId(SnowflakeIdGenerator.generate());
        logEntity.setSkuId(skuId);
        logEntity.setOrderId(orderId);
        logEntity.setChangeType(changeType);
        logEntity.setQuantity(quantity);
        inventoryLogMapper.insert(logEntity);
    }

    public java.util.List<Inventory> listByMerchant(Long merchantId) {
        return inventoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getMerchantId, merchantId));
    }

    public java.util.List<Inventory> listLowStock() {
        return inventoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Inventory>()
                        .apply("stock < safe_stock"));
    }
}
