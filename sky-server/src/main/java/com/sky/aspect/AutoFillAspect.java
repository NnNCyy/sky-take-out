package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面
 * 实现公共字段自动填充处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){ }

    /**
     * 前置通知
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段的自动填充");
        //获取到当前被拦截的方法上的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); //方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType = autoFill.value();//获得数据库操作类型

        //获取到当前被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if(args == null||args.length == 0){ return ;}
        Object obj = args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据当前不同操作类型赋值
        if(operationType==OperationType.INSERT){
            //为四个字段赋值
            try {
                Method setCreateTime = obj.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                Method setUpdateTime = obj.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setCreateUser = obj.getClass().getDeclaredMethod("setCreateUser", Long.class);
                Method setUpdateUser = obj.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                setCreateTime.invoke(obj,now);
                setCreateUser.invoke(obj,currentId);
                setUpdateTime.invoke(obj,now);
                setUpdateUser.invoke(obj,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            //为两个公共字段赋值
            try {
                Method setUpdateTime = obj.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = obj.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                setUpdateTime.invoke(obj,now);
                setUpdateUser.invoke(obj,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
