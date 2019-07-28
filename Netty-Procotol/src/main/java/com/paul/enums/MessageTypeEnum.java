package com.paul.enums;

public enum MessageTypeEnum {

    HANDREQ(1, "握手请求"),
    HANDRESP(2, "握手响应"),
    PING(3,"心跳请求"),
    PONG(4,"请求应答"),
    REQUEST(5,"通信请求"),
    RESPONSE(6,"通信应答");

    public final int type;
    public final String desc;

    MessageTypeEnum(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
