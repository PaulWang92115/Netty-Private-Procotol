package com.paul.instance;



public interface NettyClient {

    void start(String host);

    void connect(String host);
}
