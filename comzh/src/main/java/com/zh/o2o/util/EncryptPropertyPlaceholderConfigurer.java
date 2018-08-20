package com.zh.o2o.util;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class EncryptPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer{
    private String[] encryptPropNames = {"jdbc.username","jdbc.password"};

    @Override
    protected String convertProperty(String propertyName, String propertyValue) {
        if (isEncryptProp(propertyName)){
            //解密
            String decryptValue = DESUtils.getDecryptString(propertyValue);
            return decryptValue;
        }else {
            return propertyValue;
        }
    }

    private boolean isEncryptProp(String propertyName){
        //若等于加密 field  加密
        for (String encryptpropertyName :encryptPropNames){
            if (encryptpropertyName.equals(propertyName)){
                return true;
            }
        }
        return false;
    }
}
