package com.shedleo.product.controller;

import com.shedleo.common.result.Result;
import com.shedleo.product.client.ProductClient;
import com.shedleo.product.dto.SkuDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 商品服务壳 —— Controller
 *
 * 【实际职责】：
 *  - SPU/SKU 商品管理
 *  - 分类、品牌、规格管理
 *  - 商品搜索（ES）
 *  - 商品评价、问答
 *
 * 当前返回模拟商品信息。
 */
@Slf4j
@RestController
public class ProductController implements ProductClient {

    @Override
    public Result<SkuDTO> getSkuById(Long skuId) {
        SkuDTO sku = new SkuDTO();
        sku.setSkuId(skuId);
        sku.setProductId(skuId / 10);
        sku.setProductName("Shedleo 精选商品 #" + skuId);
        sku.setSkuName("默认规格");
        sku.setPrice(new BigDecimal("99.00"));
        sku.setImage("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=product%20photo&image_size=square_hd");
        sku.setSpec("颜色: 默认");
        return Result.success(sku);
    }
}
