package com.xc.common.exception;

import com.xc.common.api.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolationException;
import java.lang.reflect.Method;

/**
 * 切面切在controller上捕获全局异常并处理
 * @author Administrator
 */
@Component
@Aspect
@Slf4j
public class InterfaceRequestErrorAndPerformanceLog {
    /**
     * 切入到controller层的包和所有子包里的任意类的任意方法的执行
     */
    @Pointcut("execution(* com.xc.*.controller..*.*(..))")
    public void pointCut() {
    }


    @Around("pointCut()")
    public Object handleControllerMethod(ProceedingJoinPoint pjp) throws Throwable {
        long startTm = System.currentTimeMillis();
        Object res;
        try {
            //处理入参特殊字符和sql注入攻击
            checkRequestParam(pjp);
            //Signature s = pjp.getSignature();
            //执行访问接口操作
            res = pjp.proceed(pjp.getArgs());
            Long consumeTime = System.currentTimeMillis() - startTm;
            log.info("耗时：" + consumeTime + "(毫秒).");
            //当接口请求时间大于3秒时，标记为异常调用时间，并记录入库
            if (consumeTime > 3000) {
                log.warn("{" + pjp.getSignature() + "的执行时间超过三秒" + "}");
            }
        } catch (Exception throwable) {
            res = handlerException(pjp, throwable);
        }
        return res;
    }

    /**
     * 处理接口调用异常
     *
     * @param pjp
     * @param e
     * @return
     */
    private ResponseVO handlerException(ProceedingJoinPoint pjp, Throwable e) {
        //获取方法签名
        Signature signature = pjp.getSignature();
        //获取目标类
        Object controller = pjp.getTarget();
        //获取入参数
        Object[] args = pjp.getArgs();
        Class<?>[] parameterTypes = new Class<?>[args.length];
        try {
            for (int j = 0; j < args.length; j++) {
                Object arg = args[j];
                parameterTypes[j] = arg.getClass();
            }

            Method method = controller.getClass().getDeclaredMethod(signature.getName(), parameterTypes);
            Class<?> returnType = method.getReturnType();
            if (!returnType.equals(ResponseVO.class)) {
                return null;
            }
        } catch (Exception ex) {
            //TODO:出现空指针可能是get入参有问题
            log.error(ex.getMessage());
        }
        ResponseVO apiResponse;
        if (e.getClass().isAssignableFrom(ServiceException.class)) {
            apiResponse = ResponseVO.errorInstance(e.getMessage());
        } else if (e instanceof ConstraintViolationException) {
            //利用校验框架的验证异常
            String s = e.getMessage();
            String msg = s.substring(s.indexOf(":") + 1);
            apiResponse = ResponseVO.errorInstance(msg);
        } else if (e instanceof RuntimeException) {
            log.error("RuntimeException{方法：" + pjp.getSignature() + "， 参数：" + pjp.getArgs() + ",异常：" + e.getMessage() + "}", e);
            apiResponse = ResponseVO.errorInstance(e.getMessage());
        } else {
            log.error("异常{方法：" + pjp.getSignature() + "， 参数：" + pjp.getArgs() + ",异常：" + e.getMessage() + "}", e);
            apiResponse = ResponseVO.errorInstance(e.getMessage());
        }
        return apiResponse;
    }

    /**
     * @Author: wzd
     * @Description: 处理入参特殊字符和sql注入攻击
     */
    private void checkRequestParam(ProceedingJoinPoint pjp) {
        String str = String.valueOf(pjp.getArgs());
       /* if (!IllegalStrFilterUtil.sqlStrFilter(str)) {
            logger.info("访问接口：" + pjp.getSignature() + "，输入参数存在SQL注入风险！参数为：" + Lists.newArrayList(pjp.getArgs()).toString());
            DcErrorEntity dcErrorEntity = interfaceErrorService.processDcErrorEntity(pjp.getSignature() + "",Lists.newArrayList(pjp.getArgs()).toString(),"输入参数存在SQL注入风险!");
            throw new DataCenterException(dcErrorEntity);
        }
        if (!IllegalStrFilterUtil.isIllegalStr(str)) {
            logger.info("访问接口：" + pjp.getSignature() + ",输入参数含有非法字符!，参数为：" + Lists.newArrayList(pjp.getArgs()).toString());
            DcErrorEntity dcErrorEntity = interfaceErrorService.processDcErrorEntity(pjp.getSignature() + "",Lists.newArrayList(pjp.getArgs()).toString(),"输入参数含有非法字符!");
            throw new DataCenterException(dcErrorEntity);
        }*/
    }
}
