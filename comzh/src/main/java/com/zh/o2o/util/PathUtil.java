package com.zh.o2o.util;

public class PathUtil {
    private static String separator = System.getProperty("file.separator");
    public static String getImageBasePath(){
        String os = System.getProperty("os.name");
        String basePath = "";
        if (os.toLowerCase().startsWith("win")){
            basePath = "/D:/prjectdev/image";
        }else {
            basePath = "/Users/zhanghao/Desktop/image";
        }
            basePath = basePath.replace("/",separator);
        return basePath;
    }

    public static String getShopImagePath(long shopId){
        String imagetPath = "/upload/item/shop/" + shopId + "/";
        return imagetPath.replace("/",separator);
    }
}
