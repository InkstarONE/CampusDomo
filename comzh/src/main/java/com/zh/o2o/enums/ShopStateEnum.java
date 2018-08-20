package com.zh.o2o.enums;

import com.sun.org.apache.bcel.internal.generic.CHECKCAST;

public enum  ShopStateEnum {
    CHECK(0,"审核中"),OFFLINE(-1,"非法店铺"),SUCESS(1,"操作成功"),

    PASS(2,"通过验证"),INNER_ERROR(-1001,"内部系统错误"),

    NULL_SHOPID(-1002,"shopId为空"),NULL_SHOP(-1003,"shop信息为空");

    private int state;
    private String stateInfo;

    private ShopStateEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    //根据传入的state返回相应的enum值

    public static ShopStateEnum stateof(int state){
        for (ShopStateEnum shopStateEnum : values()){
            if (state == shopStateEnum.getState()){
                return shopStateEnum;
            }
        }
        return null;
    }
}
