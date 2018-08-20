package com.zh.o2o.service.impl;

import com.beust.jcommander.ParameterException;
import com.zh.o2o.dao.ProductDao;
import com.zh.o2o.dao.ProductImgDao;
import com.zh.o2o.dto.ImageHolder;
import com.zh.o2o.dto.ProductExecution;
import com.zh.o2o.entity.Product;
import com.zh.o2o.entity.ProductImg;
import com.zh.o2o.enums.ProductStateEnum;
import com.zh.o2o.exception.ProductException;
import com.zh.o2o.service.ProductService;
import com.zh.o2o.util.ImageUtil;
import com.zh.o2o.util.PageCalculator;
import com.zh.o2o.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ProductImgDao productImgDao;

    @Override
    public ProductExecution addProduct(Product product, ImageHolder thumbnail, List<ImageHolder> imageHolderList) throws ProductException {
        if (product != null && product.getShop() != null && product.getShop().getShopId() != null) {
            product.setCreateTime(new Date());
            product.setLastEditTime(new Date());

            product.setEnableStatus(1);
            if (thumbnail != null) {
                addThumbnail(product, thumbnail);
            }
            try {
                int effectedNum = productDao.insertProduct(product);
                if (effectedNum <= 0) {
                    throw new ProductException("创建商品失败");
                }
            } catch (Exception e) {
                throw new ProductException("创建商品失败" + e.toString());

            }
            return new ProductExecution(ProductStateEnum.SUCCESS, product);
        } else {
            return new ProductExecution(ProductStateEnum.EMPTY);
        }
    }

    @Override
    public Product getProductById(long productId) {
        return productDao.queryProductById(productId);
    }

    @Override
    @Transactional
    public ProductExecution modifyProduct(Product product, ImageHolder thumnail, List<ImageHolder> productImageHolderList) throws ProductException {
        if (product !=null && product.getShop() != null && product.getShop().getShopId() !=null){
            product.setLastEditTime(new Date());
            if (thumnail != null){
                Product tempProduct = productDao.queryProductById(product.getProductId());
                if (tempProduct.getImgAddr()!=null){
                    ImageUtil.deleteFileOrPath(tempProduct.getImgAddr());
                }
                addThumbnail(product,thumnail);
            }

        if (productImageHolderList !=null && productImageHolderList.size()>0){
            deleteProductImgList(product.getProductId());
            addProductImgList(product,productImageHolderList);
        }

        try {
            int effectedNum = productDao.updateProduct(product);
            if (effectedNum<=0){
                throw new ProductException("更新商品信息失败");
            }
            return new ProductExecution(ProductStateEnum.SUCCESS,product);
        }catch (Exception e){
            throw new ProductException("更新商品失败" + e.toString());

        }
    }else {
            return new ProductExecution(ProductStateEnum.EMPTY);
        }
    }

    @Override
    public ProductExecution getProductList(Product productcondition, int pageIndex, int pageSize) {
        int rowIndex = PageCalculator.calculateRowIndex(pageIndex,pageSize);
        List<Product> productList = productDao.queryProductList(productcondition,rowIndex,pageSize);

        int count = productDao.queryProductCount(productcondition);
        ProductExecution pe = new ProductExecution();

        pe.setProductList(productList);
        pe.setCount(count);
        return pe;
    }

    private void deleteProductImgList(Long productId) {
        List<ProductImg> productImgList = productImgDao.queryProductImgList(productId);
        for (ProductImg productImg : productImgList){
            ImageUtil.deleteFileOrPath(productImg.getImgAddr());
        }
        productImgDao.delteProductImgByProductId(productId);
    }


    //添加缩略图
    private void addThumbnail(Product product, ImageHolder thumbnail) {
        String dest = PathUtil.getShopImagePath(product.getShop().getShopId());
        String thumbnailAddr = ImageUtil.generateThumbnail(thumbnail, dest);
        product.setImgAddr(thumbnailAddr);
    }

    //添批量添图片
    private void addProductImgList(Product product, List<ImageHolder> productImgHolderList) {
        // 获取图片存储路径，这里直接存到相应店铺的文件夹下
        String desc = PathUtil.getShopImagePath(product.getShop().getShopId());
        List<ProductImg> productImgList = new ArrayList<>();

        // 遍历图片依次去处理，并添加进 productImg 实体类中
        for (ImageHolder productImageHolder : productImgHolderList) {
            String imgAddr = ImageUtil.generateThumbnail(productImageHolder, desc);
            ProductImg productImg = new ProductImg();
            productImg.setImgAddr(imgAddr);
            productImg.setProductId(product.getProductId());
            productImg.setCreateTime(new Date());
            productImgList.add(productImg);
        }

        // 如果确定是有图片需要添加的，就执行批量添加操作
        if (productImgList.size() > 0) {
            try {

                int effectedNum = productImgDao.batchInsertProductImgDao(productImgList);
                if (effectedNum <= 0) {
                    throw new ProductException("创建商品详情图片失败!");
                }
            } catch (Exception e) {
                throw new ProductException("创建商品详情图片失败，" + e.toString());
            }
        }
    }
}