package com.zh.o2o.service;

import com.zh.o2o.ShopOperationException;
import com.zh.o2o.dto.ImageHolder;
import com.zh.o2o.dto.ShopExecution;
import com.zh.o2o.entity.Shop;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.InputStream;

public interface ShopService {
   ShopExecution addShop(Shop shop, ImageHolder thumbnail);


   Shop getByShopId(Long shopId);

   ShopExecution modifyShop(Shop shop, ImageHolder thumbnail);

   public ShopExecution getShopList(Shop shopCondition,int pageIndex,int pageSize);
}
