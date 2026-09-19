package com.shedleo.product.client;

import com.shedleo.common.result.Result;
import com.shedleo.product.dto.SkuDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务 Feign 接口
 *
 * 【实际职责】：
 *  - 商品 SPU / SKU 管理（SPU 是商品抽象，SKU 是具体销售单元，如颜色×尺寸）
 *  - 商品分类管理（三级分类树）
 *  - 商品品牌管理
 *  - 商品属性/规格管理
 *  - 商品上下架
 *  - 商品搜索（对接 Elasticsearch）
 *  - 商品评价、问答
 */
@FeignClient(name = "shedleo-product")
public interface ProductClient {

    @GetMapping("/product/sku")
    Result<SkuDTO> getSkuById(@RequestParam("skuId") Long skuId);
}
