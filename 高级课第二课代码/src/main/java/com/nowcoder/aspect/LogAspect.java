package com.nowcoder.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;


@Aspect
@Component
public class LogAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Before("execution(* com.nowcoder.controller.*.*(..))")
    public void  before(){
        logger.info("before method");
    }

    @After("execution(* com.nowcoder.controller.*.*(..))")
    public void  after(JoinPoint j){
        logger.info("after method");
        StringBuffer sb = new StringBuffer();
        for (Object args : j.getArgs()){
            sb.append("agrs:"+args.toString() + "|");
        }
        logger.info(sb.toString());
    }
}
