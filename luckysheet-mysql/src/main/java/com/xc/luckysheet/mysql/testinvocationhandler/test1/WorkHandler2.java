package com.xc.luckysheet.mysql.testinvocationhandler.test1;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JDK自带的动态代理主要是指，实现了InvocationHandler接口的代理类，实现了InvocationHandler接口的类，会继承一个invoke方法，
 * 通过在这个方法中添加某些代码，从而完成在方法前后添加一些动态的东西。JDK自带的动态代理依赖于接口，如果有些类没有接口，则不能实现动态代理。
 * 
 * proxy是真实对象的真实代理对象，invoke方法可以返回调用代理对象方法的返回结果，也可以返回对象的真实代理对象
 * proxy参数是invoke方法的第一个参数，通常情况下我们都是返回真实对象方法的返回结果，但是我们也可以将proxy返回，
 * proxy是真实对象的真实代理对象，我们可以通过这个返回对象对真实的对象做各种各样的操作。
 */
public class WorkHandler2 implements InvocationHandler {

    private Object obj;

    public Object bind(Object obj) {
        this.obj = obj;
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("before 动态代理...");
        System.out.println(proxy.getClass().getName());
        System.out.println(this.obj.getClass().getName());
        if(method.getName().equals("work")) {
            method.invoke(this.obj, args);
            System.out.println("after 动态代理(work)...");
            return proxy;
        } else {
            System.out.println("after 动态代理...");
            return method.invoke(this.obj, args);
        }
    }

}

