package com.zh.o2o.service.impl;

import com.zh.o2o.ShopOperationException;
import com.zh.o2o.dao.ShopDao;
import com.zh.o2o.dto.ImageHolder;
import com.zh.o2o.dto.ShopExecution;
import com.zh.o2o.entity.Shop;
import com.zh.o2o.enums.ShopStateEnum;
import com.zh.o2o.service.ShopService;
import com.zh.o2o.util.ImageUtil;
import com.zh.o2o.util.PageCalculator;
import com.zh.o2o.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

@Service
public class ShopServiceImp implements ShopService {

    @Autowired
    private ShopDao shopDao;
    @Override
    public ShopExecution addShop(Shop shop, ImageHolder thumbnail) {
        if (shop == null){
            return new ShopExecution(ShopStateEnum.NULL_SHOP);
        }

        try {
        shop.setEnableStatus(0);
        shop.setCreateTime(new Date());
        shop.setLastEditTime(new Date());

        int effectedNum = shopDao.insertShop(shop);
        if (effectedNum <= 0){
            throw new ShopOperationException("店铺创建失败");
        }else {
            if (thumbnail.getImage() != null){
                try {
                    addShopImag(shop, thumbnail);
                }catch (Exception e){
                    throw new ShopOperationException("add shop error image error" + e.getMessage());
                }
                    effectedNum = shopDao.updateShop(shop);
                if (effectedNum <= 0){
                    throw new RuntimeException("更新图片地址失败");
                }
            }
        }
        }catch (Exception e){
            throw new ShopOperationException("add shop error" + e.getMessage());
        }
        return new ShopExecution(ShopStateEnum.CHECK,shop);
    }

    @Override
    public Shop getByShopId(Long shopId) {
        return shopDao.queryByshopId(shopId);
    }

    @Override
    public ShopExecution modifyShop(Shop shop, ImageHolder thumbnail) {
        if (shop == null || shop.getShopId() ==null){
            return new ShopExecution(ShopStateEnum.NULL_SHOP);
        }else {
            //1.判断是否需要处理照片
            try {
                if (thumbnail.getImage() != null && thumbnail.getImageName() != null && !"".equals(thumbnail.getImageName())){
                    Shop tempShop = shopDao.queryByshopId(shop.getShopId());
                    if (tempShop.getShopImg() != null){
                        ImageUtil.deleteFileOrPath(tempShop.getShopImg());
                    }
                    addShopImag(shop,thumbnail);
                }
                //2.更新店铺信息
                shop.setLastEditTime(new Date());
                int effectedNum = shopDao.updateShop(shop);
                if (effectedNum <= 0){
                    return new ShopExecution(ShopStateEnum.INNER_ERROR);
                }else {
                    shop = shopDao.queryByshopId(shop.getShopId());
                    return new ShopExecution(ShopStateEnum.SUCESS,shop);
                }
            }catch (Exception e){
                throw new ShopOperationException("modifyShop error"+e.getMessage());
            }
        }
    }

    @Override
    public ShopExecution getShopList(Shop shopCondition, int pageIndex, int pageSize) {
            int rowIndex = PageCalculator.calculateRowIndex(pageIndex,pageSize);
        List<Shop> shopList = shopDao.queryShopList(shopCondition,rowIndex,pageSize);
        int count = shopDao.queryShopCount(shopCondition);
        ShopExecution se = new ShopExecution();
        if (shopList != null){
            se.setShopList(shopList);
            se.setCount(count);
        }else {
            se.setState(ShopStateEnum.INNER_ERROR.getState());
        }
        return se;
    }

    private void addShopImag(Shop shop, ImageHolder thumbnail) {
        //获取shop 图片目录的相对路径
        String dest = PathUtil.getShopImagePath(shop.getShopId());
        String shopImgAddr = ImageUtil.generateThumbnail(thumbnail,dest);
        shop.setShopImg(shopImgAddr);
    }
}
