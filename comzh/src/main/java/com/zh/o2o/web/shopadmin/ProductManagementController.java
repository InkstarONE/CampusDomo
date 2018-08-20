package com.zh.o2o.web.shopadmin;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zh.o2o.dto.ImageHolder;
import com.zh.o2o.dto.ProductExecution;
import com.zh.o2o.entity.Product;
import com.zh.o2o.entity.ProductCategory;
import com.zh.o2o.entity.Shop;
import com.zh.o2o.enums.ProductStateEnum;
import com.zh.o2o.exception.ProductException;
import com.zh.o2o.service.ProductCategoryService;
import com.zh.o2o.service.ProductService;
import com.zh.o2o.util.CodeUtil;
import com.zh.o2o.util.HttpSrevletRuquestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shopadmin")
public class ProductManagementController {
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductCategoryService productCategoryService;

    private static final int IMAGE_MAX_COUNT = 5;



    @RequestMapping(value = "/getproductlistbyshop")
    @ResponseBody
    private Map<String,Object> getProductListByShop(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<>();
        int pageIndex = HttpSrevletRuquestUtil.getInt(request,"pageIndex");
        int pageSize = HttpSrevletRuquestUtil.getInt(request,"pageSize");

        Shop currentShop = (Shop) request.getSession().getAttribute("currentShop");

        if ((pageIndex > -1) && (pageSize>-1)&& currentShop!=null&&(currentShop.getShopId()!=null)){
            long productCategoryId = HttpSrevletRuquestUtil.getLong(request,"productCategoryId");
            String productName = HttpSrevletRuquestUtil.getString(request,"productName");
            Product productCondition = compactProdutContion(currentShop.getShopId(),productCategoryId,productName);
            ProductExecution pe = productService.getProductList(productCondition,pageIndex,pageSize);
            modelMap.put("productList",pe.getProductList());
            modelMap.put("count",pe.getCount());
            modelMap.put("success",true);
        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","empty pagesize or pageIndex or shopId");
        }
        return modelMap;
    }

    private Product compactProdutContion(Long shopId, long productCategoryId,String productName) {
        Product productCondition = new Product() ;
        Shop shop = new Shop();
        shop.setShopId(shopId);

        productCondition.setShop(shop);

        if (productCategoryId != -1L){
            ProductCategory productCategory = new ProductCategory();
            productCategory.setProductCategoryId(productCategoryId);
            productCondition.setProductCategory(productCategory);
        }

        if (productName != null){
            productCondition.setProductName(productName);
        }
        return productCondition;
    }


    @RequestMapping(value = "/modifyproduct",method = RequestMethod.POST)
    @ResponseBody
    private Map<String,Object> modifyProduct(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<>();
        boolean statusChange = HttpSrevletRuquestUtil.getBoolean(request,"statusChange");

        if (!statusChange && !CodeUtil.checkVerifyCode(request)){
            modelMap.put("success",false);
            modelMap.put("errMsg","输入了错误验证码");
            return modelMap;
        }

        ObjectMapper mapper = new ObjectMapper();
        Product product = null;
        ImageHolder thumbnail = null;
        List<ImageHolder> productImgList = new ArrayList<>();
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());

        try {
            if (multipartResolver.isMultipart(request)){
                MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
                CommonsMultipartFile thumbnailFile = (CommonsMultipartFile) multipartHttpServletRequest.getFile("thumbnail");

                if (thumbnailFile != null){
                    thumbnail = new ImageHolder(thumbnailFile.getOriginalFilename(),thumbnailFile.getInputStream());
                }

                for (int i= 0; i < IMAGE_MAX_COUNT;i++){
                    CommonsMultipartFile productImgFile = (CommonsMultipartFile) multipartHttpServletRequest.getFile("productImg" + i);
                    if (productImgFile != null){
                        ImageHolder productImg = new ImageHolder(productImgFile.getOriginalFilename(),productImgFile.getInputStream());
                        productImgList.add(productImg);
                    }else {
                        break;
                    }
                }
            }
        }catch (Exception e){
            modelMap.put("success",false);
            modelMap.put("errMsg",e.toString());
            return modelMap;
        }

         try {
            String productStr = HttpSrevletRuquestUtil.getString(request,"productStr");
            product = mapper.readValue(productStr,Product.class);
         }catch (Exception e){
            modelMap.put("success",false);
            modelMap.put("errMsg",e.toString());
            return modelMap;
         }

         if (product!=null){
             try {
                 Shop currentShop = (Shop) request.getSession().getAttribute("currentShop");
                 Shop shop = new Shop();
                 shop.setShopId(currentShop.getShopId());
                 product.setShop(shop);

                 ProductExecution pe = productService.modifyProduct(product,thumbnail,productImgList);
                 if (pe.getState()==ProductStateEnum.SUCCESS.getState()){
                     modelMap.put("success",true);
                 }else {
                     modelMap.put("success",false);
                     modelMap.put("errMsg",pe.getState());
                 }
             }catch (RuntimeException e){
                 modelMap.put("success",false);
                 modelMap.put("errMsg",e.toString());
                 return modelMap;
             }
         }else {
             modelMap.put("success", false);
             modelMap.put("errMsg", "请输入商品信息");
         }

         return modelMap;
    }




    @RequestMapping(value = "/getproductbyid", method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> getProductById(@RequestParam Long productId){
     Map<String, Object> modelMap = new HashMap<>();
    if(productId > -1){
        Product product = productService.getProductById(productId);
        List<ProductCategory> productCategoryList = productCategoryService.getProductCategoryList(product.getShop().getShopId());
        modelMap.put("product",product);
        modelMap.put("productCategoryList",productCategoryList);
        modelMap.put("success",true);
    }else {
        modelMap.put("success",false);
        modelMap.put("errMsg",true);
    }
    return modelMap;

    }

    @RequestMapping(value = "/addproduct",method = RequestMethod.POST)
    @ResponseBody
    private Map<String, Object> addProduct(HttpServletRequest request) {
        Map<String, Object> modelMap = new HashMap<>(4);
        // 验证码校验
        if (!CodeUtil.checkVerifyCode(request)) {
            modelMap.put("success", false);
            modelMap.put("errMsg", "输入了错误的验证码!");
            return modelMap;
        }

        // 接收前端参数的变量的初始化，包括商品，缩略图，详情图片列表实现类
        ObjectMapper mapper = new ObjectMapper();
        Product product;
        String productStr = HttpSrevletRuquestUtil.getString(request, "productStr");
        MultipartHttpServletRequest multipartRequest;
        ImageHolder thumbnail = null;
        List<ImageHolder> productImgList = new ArrayList<>();

        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        try {
            // 若请求中存在文件流，则取出相关的文件(包括缩略图和详情图)
            if (multipartResolver.isMultipart(request)) {
                //thumbnail = handleImage(request, thumbnail, productImgList);
                multipartRequest = (MultipartHttpServletRequest) request;
                // 取出缩略图并构建 ImageHolder 对象
                CommonsMultipartFile thumbnailFile = (CommonsMultipartFile) multipartRequest.getFile("thumbnail");
                if (thumbnailFile != null) {
                    thumbnail = new ImageHolder(thumbnailFile.getOriginalFilename(), thumbnailFile.getInputStream());
                }

                // 取出详情图列表并构建 List<ImageHolder> 列表对象，最多支持5张图片上传
                for (int i = 0; i < IMAGE_MAX_COUNT; i++) {
                    CommonsMultipartFile productImgFile = (CommonsMultipartFile)
                            multipartRequest.getFile("productImg" + i);
                    if (productImgFile != null) {
                        // 如果取出的第i个详情图片文件流不为空，则讲其加入详情图列表
                        ImageHolder productImg = new ImageHolder(
                                productImgFile.getOriginalFilename(), productImgFile.getInputStream());
                        productImgList.add(productImg);
                    } else {
                        // 如果取出的第i个详情图片文件流为空，则终止循环
                        break;
                    }
                }

            } else {
                modelMap.put("success", false);
                modelMap.put("errMsg", "上传图片不能为空!");
                return modelMap;
            }
        } catch (Exception e) {
            modelMap.put("success", false);
            modelMap.put("errMsg", e.toString());
            return modelMap;
        }

        try {
            // 尝试获取前端传过来的表单 string 流并将其转换为 Product 实体类
            product = mapper.readValue(productStr, Product.class);
        } catch (Exception e) {
            modelMap.put("success", false);
            e.printStackTrace();
            modelMap.put("errMsg", e.toString());
            return modelMap;
        }

        // 若 product 信息，缩略图以及详情图非空，则开始进行商品添加操作
        if (product != null && thumbnail != null && productImgList.size() > 0) {
            try {
                // 从 session 中获取当前店铺的 id 并赋值给 product，减少对前端数据的依赖
                Shop currentShop = (Shop) request.getSession().getAttribute("currentShop");
                product.setShop(currentShop);

                // 执行添加操作
                ProductExecution pe = productService.addProduct(product, thumbnail, productImgList);
                if (pe.getState() == ProductStateEnum.SUCCESS.getState()) {
                    modelMap.put("success", true);
                } else {
                    modelMap.put("success", false);
                    modelMap.put("errMsg", pe.getStateInfo());
                }
            } catch (ProductException e) {
                modelMap.put("success", false);
                modelMap.put("errMsg", e.toString());
                return modelMap;
            }
        } else {
            modelMap.put("success", false);
            modelMap.put("errMsg", "请输入商品信息!");
        }

        return modelMap;
    }

    private ImageHolder handleImage(HttpServletRequest request, ImageHolder thumbnail, List<ImageHolder> productImgList)
            throws IOException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        // 取出缩略图并构建 ImageHolder 对象
        CommonsMultipartFile thumbnailFile = (CommonsMultipartFile) multipartRequest.getFile("thumbnail");
        if (thumbnailFile != null) {
            thumbnail = new ImageHolder(thumbnailFile.getOriginalFilename(), thumbnailFile.getInputStream());
        }

        // 取出详情图列表并构建 List<ImageHolder> 列表对象，最多支持5张图片上传
        for (int i = 0; i < IMAGE_MAX_COUNT; i++) {
            CommonsMultipartFile productImgFile = (CommonsMultipartFile)
                    multipartRequest.getFile("productImg" + i);
            if (productImgFile != null) {
                // 如果取出的第i个详情图片文件流不为空，则讲其加入详情图列表
                ImageHolder productImg = new ImageHolder(
                        productImgFile.getOriginalFilename(), productImgFile.getInputStream());
                productImgList.add(productImg);
            } else {
                // 如果取出的第i个详情图片文件流为空，则终止循环
                break;
            }
        }
        return thumbnail;
    }
}


