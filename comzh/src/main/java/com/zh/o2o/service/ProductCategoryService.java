package com.zh.o2o.service;

import com.zh.o2o.dto.ProductCategoryExecution;
import com.zh.o2o.entity.ProductCategory;

import java.util.List;

public interface ProductCategoryService {
    public List<ProductCategory> getProductCategoryList(Long shopId);

    public ProductCategoryExecution batchAddPeoductCategoryList(List<ProductCategory> productCategoryList);

    ProductCategoryExecution deleteProductCategory(long productCategoryId,
                                                   long shopId) throws RuntimeException;
}
