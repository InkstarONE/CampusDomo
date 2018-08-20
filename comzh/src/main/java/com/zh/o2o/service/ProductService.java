package com.zh.o2o.service;

import com.zh.o2o.dto.ImageHolder;
import com.zh.o2o.dto.ProductExecution;
import com.zh.o2o.entity.Product;
import com.zh.o2o.exception.ProductException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.util.List;


public interface ProductService {
    ProductExecution addProduct(Product product, ImageHolder thumbnail,List<ImageHolder> imageHolderList) throws ProductException;

    Product getProductById(long productId);

    ProductExecution modifyProduct(Product product,ImageHolder thumnail,List<ImageHolder> productImageHolderList) throws ProductException;

    ProductExecution getProductList(Product productcondition,int pageIndex,int pageSize );
}
