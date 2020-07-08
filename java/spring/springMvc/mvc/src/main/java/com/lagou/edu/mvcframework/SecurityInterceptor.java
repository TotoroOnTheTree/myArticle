package com.lagou.edu.mvcframework;

import com.lagou.edu.mvcframework.annotations.LagouController;
import com.lagou.edu.mvcframework.annotations.LagouRequestMapping;
import com.lagou.edu.mvcframework.annotations.Security;
import com.lagou.edu.mvcframework.pojo.Handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * TODO .
 *
 * @Author: xumao
 * @DateTime: 2020/6/11 16:19
 * @Version: 1.0
 **/
public class SecurityInterceptor implements Interceptor {

    //存放路径和校验用户信息
    private Map<String,Set<String>> securityUrls = new HashMap<>();
    private static String AuthParamName = "username";

    public SecurityInterceptor(Map<String,Object> ioc) {
        if(ioc.isEmpty()){return;}
        //遍历找到标注了LagouController 、Security注解的类，建立映射关系
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Object obj = entry.getValue();
            Class objClass = obj.getClass();
            if(!objClass.isAnnotationPresent(LagouController.class)){continue;}
            
            String baseUrl = null;
            //类被标注了的话，所有方法都需要校验
            boolean classSecurity = objClass.isAnnotationPresent(Security.class);
            
            //获取父路径
            if(objClass.isAnnotationPresent(LagouRequestMapping.class)) {
                LagouRequestMapping annotation = (LagouRequestMapping) objClass.getAnnotation(LagouRequestMapping.class);
                baseUrl = annotation.value();
            }

            for (Method method : objClass.getMethods()) {
                if(!classSecurity && !method.isAnnotationPresent(Security.class)){ continue;}//不需要校验
                
                String url = baseUrl;
                //获取子路径
                if(method.isAnnotationPresent(LagouRequestMapping.class)) {
                    LagouRequestMapping annotation =  method.getAnnotation(LagouRequestMapping.class);
                    url += annotation.value();
                }

                HashSet<String> users = new HashSet<>();
                //将类中标注的用户和方法标注的用户合并，如果class要求 A 用户，method要求B 用户，那么访问该方法
                //时，A、B 用户均可以访问
                Security classAnno = (Security) objClass.getAnnotation(Security.class);
                if(classAnno!=null){
                    String[] value = classAnno.value();
                    users.addAll(Arrays.asList(value));
                }

                Security methodAnno = method.getAnnotation(Security.class);
                if(methodAnno!=null){
                    String[] value = methodAnno.value();
                    users.addAll(Arrays.asList(value));
                }
                securityUrls.put(url,users);
            }
            
            
        }
    }


    @Override
    public boolean support( String path) {
        System.out.println("开始用户安全前置校验……");
        return securityUrls.containsKey(path);
    }

    @Override
    public boolean preInterceptor(HttpServletRequest req, HttpServletResponse resp, String path,Handler handler) throws IOException {
        System.out.println("开始用户校验……");
        Set<String> strings = securityUrls.get(path);
        String parameter = req.getParameter(AuthParamName);
        if(!strings.contains(parameter)){
            resp.getWriter().write("you have no permission!");
            return false;
        }
        return true;
    }

    @Override
    public void doInterceptor(HttpServletRequest req, HttpServletResponse resp, String path,Handler handler) throws IOException {

    }

    @Override
    public void afterIntegerceptor(HttpServletRequest req, HttpServletResponse resp, Handler handler) {
        System.out.println("开始用户安全后置校验……");

    }
}
