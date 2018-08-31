package org.apache.skywalking.apm.plugin.lettuce.v4;

import com.lambdaworks.redis.RedisURI;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceConstructorInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

public class RedisClusterClientInterceptor implements InstanceMethodsAroundInterceptor, InstanceConstructorInterceptor {
    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        Iterable<RedisURI> redisURI = (Iterable<RedisURI>)allArguments[1];
        objInst.setSkyWalkingDynamicField(redisURI);
    }

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {

    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        Iterable<RedisURI> redisURI = (Iterable<RedisURI>)objInst.getSkyWalkingDynamicField();
        EnhancedInstance obj = (EnhancedInstance)ret;
        obj.setSkyWalkingDynamicField(redisURI);
        return obj;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }
}
