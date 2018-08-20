package com.zh.o2o.web.shopadmin;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zh.o2o.dto.ImageHolder;
import com.zh.o2o.dto.ShopExecution;
import com.zh.o2o.entity.Area;
import com.zh.o2o.entity.PersonInfo;
import com.zh.o2o.entity.Shop;
import com.zh.o2o.entity.ShopCategory;
import com.zh.o2o.enums.ShopStateEnum;
import com.zh.o2o.service.AreaService;
import com.zh.o2o.service.ShopCategoryService;
import com.zh.o2o.service.ShopService;
import com.zh.o2o.util.CodeUtil;
import com.zh.o2o.util.HttpSrevletRuquestUtil;
import com.zh.o2o.util.ImageUtil;
import com.zh.o2o.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shopadmin")
public class ShopManagementController {

    @Autowired
    private ShopService shopService;
    @Autowired
    private ShopCategoryService shopCategoryService;
    @Autowired
    private AreaService areaService;


    @RequestMapping(value = "/getManagementInfo",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> getShopManagementInfo(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<>();
        long shopId = HttpSrevletRuquestUtil.getLong(request,"shopId");
        if (shopId <= 0 ){
            Object currentShopObj = request.getSession().getAttribute("currentShop");
            if (currentShopObj == null){
                modelMap.put("redirect",true);
                modelMap.put("url","/shopadmin/shoplist");

            }else {
                Shop currentShop = (Shop) currentShopObj;
                modelMap.put("redirect",false);
                modelMap.put("shopId",currentShop.getShopId());

            }
        }else {
            Shop currentShop = new Shop();
            currentShop.setShopId(shopId);
            request.getSession().setAttribute("currentShop",currentShop);
            modelMap.put("redirect",false);
        }
        return modelMap;
    }


    @RequestMapping(value = "/getshoplist",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> getShopList(HttpServletRequest request){

        Map<String,Object> modelMap = new HashMap<>();

        PersonInfo user = new PersonInfo();

        user.setUserId(1L);
        user.setName("tedt");
        request.getSession().setAttribute("user",user);
        user = (PersonInfo) request.getSession().getAttribute("user");
        try {
            Shop shooCondition = new Shop() ;
            shooCondition.setOwner(user);

            ShopExecution se = shopService.getShopList(shooCondition,0,100);
            modelMap.put("shopList",se.getShopList());
            modelMap.put("user",user);
            modelMap.put("success",true);
        }catch (Exception e){
            modelMap.put("success",false);
            modelMap.put("errMsg",e.getMessage());
        }
        return modelMap;
    }




    @RequestMapping(value = "/getshopinitinfo",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> getShopInitInfo(){
        Map<String,Object> modelMap = new HashMap<>();
        List<ShopCategory> shopCategoryList = new ArrayList<>();
        List<Area> areaList = new ArrayList<>();

        try {
            shopCategoryList = shopCategoryService.getShopCategoryList(new ShopCategory());
            areaList = areaService.getAreaList();
            modelMap.put("shopCategoryList",shopCategoryList);
            modelMap.put("areaList",areaList);
            modelMap.put("success",true);
            return modelMap;
        }catch (Exception e){
            modelMap.put("sucess",false);
            modelMap.put("errMsg",e.getMessage());
            return modelMap;
        }
    }


    //店铺注册
    @RequestMapping(value = "/registershop",method = {RequestMethod.POST})
    @ResponseBody
    private Map<String,Object> registerShop(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<>();
        if (!CodeUtil.checkVerifyCode(request)){
            modelMap.put("success",false);
            modelMap.put("errMsg","输入了错误的验证码");
            return modelMap;
        }

        String shopStr = HttpSrevletRuquestUtil.getString(request,"shopStr");
        ObjectMapper objectMapper = new ObjectMapper();
        Shop shop = null;



        //转换图片
        CommonsMultipartFile shopImg = null;
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext()
        );

        if (commonsMultipartResolver.isMultipart(request)){
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            shopImg = (CommonsMultipartFile)multipartHttpServletRequest.getFile("shopImg");

        }else {
            modelMap.put("sucess",false);
            modelMap.put("errMsg","上传图片不能为空");
            return modelMap;
        }
        try {
            System.out.println(shopStr);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
            shop = objectMapper.readValue(shopStr,Shop.class);
        }catch (Exception e){
            modelMap.put("sucess",false);
            modelMap.put("errMsg",e.getMessage());
            return modelMap;
        }

        //注册店铺
        if (shop != null && shopImg != null){
            request.getSession().setAttribute("user",shop);
            PersonInfo owner = (PersonInfo) request.getSession().getAttribute("user");
            shop.setOwner(owner);

            ShopExecution se = null;


            try {
                ImageHolder imageHolder = new ImageHolder(shopImg.getOriginalFilename(),shopImg.getInputStream());
                se = shopService.addShop(shop,imageHolder);
                if (se.getState() == ShopStateEnum.CHECK.getState()){
                    modelMap.put("success",true);
                    List<Shop> shopList = (List<Shop>) request.getSession().getAttribute("shopList");
                    if (shopList == null || shopList.size() == 0){
                        shopList = new ArrayList<>();
                    }
                    shopList.add(se.getShop());
                    request.getSession().setAttribute("shopList",shopList);
                }else {
                    modelMap.put("success",false);
                    modelMap.put("errMsg",se.getStateInfo());
                    return modelMap;
                }
            } catch (IOException e) {
                modelMap.put("success",false);
                modelMap.put("errMsg",e.getMessage());
                return modelMap;
            }
            return modelMap;

        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","请输入店铺信息");
            return modelMap;
        }
    }


    @RequestMapping(value = "/getshopbyid",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> getShopById(HttpServletRequest request){
        Map<String,Object> modlMap = new HashMap<>();
        Long shopId = HttpSrevletRuquestUtil.getLong(request,"shopId");
        if (shopId > -1){
            try {
                Shop shop = shopService.getByShopId(shopId);
                List<Area> areaList = areaService.getAreaList();
                modlMap.put("shop",shop);
                modlMap.put("areaList",areaList);
                modlMap.put("success",true);
            }catch (Exception e){
                modlMap.put("success",false);
                modlMap.put("errMsg",e.toString());
            }
        }else {
            modlMap.put("success",false);
            modlMap.put("errMsg","empty shopId");
        }
        return modlMap;
    }






    //修改店铺
    @RequestMapping(value = "/modifyshop",method = {RequestMethod.POST})
    @ResponseBody
    private Map<String,Object> modifyShop(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<>();
        if (!CodeUtil.checkVerifyCode(request)){
            modelMap.put("sucess",false);
            modelMap.put("errMsg","输入了错误的验证码");
            return modelMap;
           }

        String shopStr = HttpSrevletRuquestUtil.getString(request,"shopStr");
        ObjectMapper objectMapper = new ObjectMapper();
        Shop shop = null;



        //转换图片
        CommonsMultipartFile shopImg = null;
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext()
        );

        if (commonsMultipartResolver.isMultipart(request)){
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            shopImg = (CommonsMultipartFile)multipartHttpServletRequest.getFile("shopImg");

        }
        try {
            System.out.println(shopStr);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
            shop = objectMapper.readValue(shopStr,Shop.class);
        }catch (Exception e){
            modelMap.put("sucess",false);
            modelMap.put("errMsg",e.getMessage());
            return modelMap;
        }

        //修改店铺
        if (shop != null && shop.getShopId()!=null){


            ShopExecution se = null;
            try {
                ImageHolder imageHolder = new ImageHolder(shopImg.getOriginalFilename(),shopImg.getInputStream());
                if (shopImg == null){
                    se = shopService.modifyShop(shop,null);
                }else {
                    se = shopService.modifyShop(shop, imageHolder);
                }
                if (se.getState() == ShopStateEnum.SUCESS.getState()){
                    modelMap.put("success",true);
                }else {
                    modelMap.put("success",false);
                    modelMap.put("errMsg",se.getStateInfo());
                    return modelMap;
                }
            } catch (IOException e) {
                modelMap.put("success",false);
                modelMap.put("errMsg",e.getMessage());
                return modelMap;
            }
            return modelMap;

        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","请输入店铺id");
            return modelMap;
        }
    }
//    vate static void inputStringToFile(InputStream inputStream, File file){
//        FileOutputStream fileOutputStream = null;
//        try {
//            fileOutputStream = new FileOutputStream(file);
//            int bytesRead = 0;
//            byte [] buffer = new byte[1024];
//            while ((bytesRead = inputStream.read(buffer)) != -1){
//                fileOutputStream.write(buffer,0,bytesRead);
//            }
//        }catch (Exception e){
//            throw new RuntimeException("调用inputStringToFile异常" + e.getMessage());
//        }finally {
//            try {
//                if (fileOutputStream != null){
//                    fileOutputStream.close();
//                }
//                if (inputStream != null){
//                    inputStream.close();
//                }
//            }catch (IOException e){
//                throw new RuntimeException("inputStringToFile关闭io异常" + e.getMessage());
//            }
//        }
//    }
}
