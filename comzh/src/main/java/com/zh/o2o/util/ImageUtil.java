package com.zh.o2o.util;

import com.zh.o2o.dto.ImageHolder;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Position;
import net.coobird.thumbnailator.geometry.Positions;
import net.coobird.thumbnailator.makers.ThumbnailMaker;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ImageUtil {
    private static String basePath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final Random r = new Random();
    public static String  generateThumbnail(ImageHolder thumbnail, String targetAddr){
        String realFileName = getRandomFileName();
        String extension = getFileExtension(thumbnail.getImageName());
        makeDirPath(targetAddr);
        String relativeAddr = targetAddr +realFileName + extension;
        File dest = new File(PathUtil.getImageBasePath()+relativeAddr);
        try {

            Thumbnails.of(thumbnail.getImage())
                    .size(200,200).watermark(Positions.BOTTOM_RIGHT, ImageIO.read(new File(basePath+"waterMark.png")),0.25F).outputQuality(0.8f)
                    .toFile(dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return relativeAddr;

    }

    public static String getRandomFileName() {
        int rannum = r.nextInt(89999) + 10000;
        String nowTimeStr = sDateFormat.format(new Date());
        return nowTimeStr + rannum;
    }

    //创建目标文件路径所涉及的目录
    private static void makeDirPath(String targetAddr) {
        String realFileParenPtath = PathUtil.getImageBasePath() + targetAddr;
        File dirPath = new File(realFileParenPtath);
        if (!dirPath.exists()){
            dirPath.mkdirs();
        }

    }

    private static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }


    public static void deleteFileOrPath(String storePath){
        File fileOrPath = new File(PathUtil.getImageBasePath() +  storePath);
        if (fileOrPath.exists()){
            if (fileOrPath.isDirectory()){
                File files[] = fileOrPath.listFiles();
                for (int i = 0 ; i < files.length;i++){
                    files[i].delete();
                }
            }
            fileOrPath.delete();
        }
    }


    public static void main(String[] args) {


        try {
            Thumbnails.of(new File("/Users/zhanghao/Desktop/zh.jpeg"))
                    .size(200,200).watermark(Positions.BOTTOM_RIGHT, ImageIO.read(new File(basePath+"waterMark.png")),0.25F).outputQuality(0.8f)
                    .toFile("/Users/zhanghao/Desktop/zhNew.jpeg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
