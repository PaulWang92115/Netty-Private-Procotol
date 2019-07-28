package com.paul.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Header {

    private String version = "Netty procotol 1.0";
    private String authorization;  //授权整数
    private int length;
    private String id;  //请求的唯一 id
    private Date date; // 请求发送的日期和时间
    private String host; //请求服务器的域名和端口好，用：隔开
    private int type;
    private Map<String,Object> attachment = new HashMap<>();

    public String getVersion() {
        return version;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Map<String, Object> getAttachment() {
        return attachment;
    }

    public void setAttachment(Map<String, Object> attachment) {
        this.attachment = attachment;
    }

    @Override
    public String toString() {
        return "Header{" +
                "version='" + version + '\'' +
                ", authorization='" + authorization + '\'' +
                ", length=" + length +
                ", id=" + id +
                ", date=" + date +
                ", host='" + host + '\'' +
                ", type=" + type +
                ", attachment=" + attachment +
                '}';
    }
}
