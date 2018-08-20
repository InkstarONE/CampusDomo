package com.zh.o2o.dao;

import com.zh.o2o.entity.Product;
import com.zh.o2o.entity.ProductImg;

import java.util.List;

public interface ProductImgDao {
    List<ProductImg> queryProductImgList(long productId);
    int batchInsertProductImgDao(List<ProductImg> productImgList);

    int delteProductImgByProductId(long productId);
}
