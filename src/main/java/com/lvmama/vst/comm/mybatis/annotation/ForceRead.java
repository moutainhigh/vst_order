package com.lvmama.vst.comm.mybatis.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** 
 * @ClassName: ForceRead 
 * @Description: 读写分离强制走读库
 * @author: lijunshuai
 * @date: 2018年7月26日 下午3:34:04  
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface ForceRead {

}
