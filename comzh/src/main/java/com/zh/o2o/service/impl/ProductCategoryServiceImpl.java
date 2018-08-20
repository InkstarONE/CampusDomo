package com.zh.o2o.service.impl;

import com.zh.o2o.dao.ProductCategoryDao;
import com.zh.o2o.dao.ProductDao;
import com.zh.o2o.dto.ProductCategoryExecution;
import com.zh.o2o.entity.ProductCategory;
import com.zh.o2o.enums.ProductCategoryStateEnum;
import com.zh.o2o.exception.ProductCategoryException;
import com.zh.o2o.exception.ProductException;
import com.zh.o2o.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {


    @Autowired
    private ProductCategoryDao productCategoryDao;


    @Autowired
    private ProductDao productDao;


    @Override
    public List<ProductCategory> getProductCategoryList(Long shopId) {
        return productCategoryDao.queryProductCategoryList(shopId);
    }

    @Override
    public ProductCategoryExecution batchAddPeoductCategoryList(List<ProductCategory> productCategoryList) throws ProductCategoryException {
        if (productCategoryList != null && productCategoryList.size() > 0 ){
            try {
                int effectedNum = productCategoryDao.batchInsertProductCategory(productCategoryList);
                if (effectedNum <= 0){
                    throw new ProductCategoryException("店铺创建失败");

                }else {
                    return new ProductCategoryExecution(ProductCategoryStateEnum.SUCCESS);
                }
            }catch (Exception e){
                throw new ProductCategoryException("batchAddProductCategoryList error" + e.getMessage());
            }
        }else {
            return new ProductCategoryExecution(ProductCategoryStateEnum.EMPTY_LIST);

        }
    }

    @Override
    public ProductCategoryExecution deleteProductCategory(long productCategoryId, long shopId) throws ProductCategoryException {

        //解除tb_product里的商品与该productCategoryId的关联
        try {
            int effectedNum = productDao.updateProductCategoryToNull(productCategoryId);
            if (effectedNum<0){
                throw new ProductException("商品更新失败");

            }
        }catch (Exception e){
            throw new ProductException("deleteProductCategory error" + e.getMessage());
        }


        //删除该productCategory
        try {
            int effectedNum = productCategoryDao.deleteProductCategory(productCategoryId,shopId);
            if (effectedNum <= 0){
                throw new ProductCategoryException("商品删除失败");
            }else {
                return new ProductCategoryExecution(ProductCategoryStateEnum.SUCCESS);
            }
        }catch (Exception e){
            throw new ProductCategoryException("deleteProductCategory error" + e.getMessage());
        }
    }
}
