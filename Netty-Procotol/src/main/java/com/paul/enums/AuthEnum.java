package com.paul.enums;

public enum AuthEnum {

    NONE("none", "无认证"),
    BASIC("basic", "基础认证"),
    OAUTH("oauth","OAUTH认证");

    public final String type;
    public final String desc;

    AuthEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
