package io.kimmking.rpcfx.demo.consumer;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author Leo liang
 * @Date 2022/3/13
 *
 * 想的有问题，这是两个model，不在一个容器里，不能直接用spring的aop，只有自己去实现
 *
 */
@Aspect
@Component
public class ExceptionAop {

    @Pointcut("execution(* io.kimmking.rpcfx.client.Rpcfx.create(..))")
    public void point(){}

    @AfterThrowing("point()")
    public void AfterThrowing(JoinPoint joinPoint){
        System.out.println(joinPoint.getSignature().getModifiers());
        System.out.println("发生了异常");
    }




}
