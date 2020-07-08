package com.lagou.edu.mvcframework.annotations;

import java.lang.annotation.*;

@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface Security {

    /**
     * 可以访问的用户名数组
     * @return
     */
    String[] value();
}
