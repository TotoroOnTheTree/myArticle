package com.lagou.edu.mvcframework;

import com.lagou.edu.mvcframework.pojo.Handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TODO .
 *
 * @Author: xumao
 * @DateTime: 2020/6/11 15:52
 * @Version: 1.0
 **/
public interface Interceptor {

    boolean support( String path);

    boolean preInterceptor(HttpServletRequest req, HttpServletResponse resp, String path,Handler handler) throws IOException;


    void doInterceptor(HttpServletRequest req, HttpServletResponse resp, String path,Handler handler) throws IOException;

    void afterIntegerceptor(HttpServletRequest req, HttpServletResponse resp, Handler handler);
}
