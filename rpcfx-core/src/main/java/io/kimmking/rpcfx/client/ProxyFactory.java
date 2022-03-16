package io.kimmking.rpcfx.client;

import com.alibaba.fastjson.JSON;
import io.kimmking.rpcfx.api.Filter;
import io.kimmking.rpcfx.api.RpcfxResponse;
import lombok.Data;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Leo liang
 * @Date 2022/3/13
 *
 */
@Data
public class ProxyFactory {

    private Object targetObject;//目标对象
    private BeforeAdvice beforeAdvice;//前值增强
    private AfterAdvice afterAdvice;//后置增强
    private ThrowAdvice throwAdvice;//异常增强

    /**
     * 用来生成代理对象
     * @return
     */
    public Object creatProxy(Object targetObject,BeforeAdvice beforeAdvice,AfterAdvice afterAdvice,ThrowAdvice throwAdvice,final Class<?> serviceClass, final String url, Filter... filters) {
        this.targetObject = targetObject;
        this.beforeAdvice = beforeAdvice;
        this.afterAdvice = afterAdvice;
        this.throwAdvice = throwAdvice;
        /**
         * 给出三个参数
         */
//        ClassLoader classLoader = this.getClass().getClassLoader();
        //获取当前类型所实现的所有接口类型
//        Class[] interfaces = targetObject.getClass().getInterfaces();

        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args){
                /**
                 * 在调用代理对象的方法时，会执行这里的内容
                 */
                Object result = null;
                RpcfxResponse response = null;
                try {
                    if (beforeAdvice != null) {
                        beforeAdvice.before();
                    }
//                    result = method.invoke(targetObject, args);//调用目标对象的目标方法
                    Rpcfx.RpcfxInvocationHandler handled = new Rpcfx.RpcfxInvocationHandler(serviceClass, url, filters);
                    result = handled.invoke(proxy, method, args);
                    if (afterAdvice != null) {
                        afterAdvice.after();
                    }
                    response = JSON.parseObject(JSON.toJSONString(result), RpcfxResponse.class);
                    if (!response.isStatus()) {
                        throwAdvice.afterThrowing();
                    }
                } catch (Exception e) {
                    throwAdvice.afterThrowing();
                } catch (Throwable throwable) {
                }finally {
                    result = JSON.parse(response.getResult().toString());
                }

                //返回目标对象的返回值
                return result;
            }
        };
        /**
         * 2、得到代理对象
         */
        Object proxyObject = Proxy.newProxyInstance(Rpcfx.class.getClassLoader(), new Class[]{serviceClass}, invocationHandler);
        return proxyObject;

    }








}
