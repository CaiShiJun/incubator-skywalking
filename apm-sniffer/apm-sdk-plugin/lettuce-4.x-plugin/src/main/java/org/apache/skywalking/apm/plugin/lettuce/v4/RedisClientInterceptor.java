package org.apache.skywalking.apm.plugin.lettuce.v4;

import com.lambdaworks.redis.RedisURI;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceConstructorInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

public class RedisClientInterceptor implements InstanceMethodsAroundInterceptor, InstanceConstructorInterceptor {
    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        RedisURI redisURI = (RedisURI)allArguments[1];
        String peer = redisURI.getHost() + ":" + redisURI.getPort();
        objInst.setSkyWalkingDynamicField(peer);
    }

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        String peer = (String)objInst.getSkyWalkingDynamicField();
        EnhancedInstance obj = (EnhancedInstance)allArguments[0];
        obj.setSkyWalkingDynamicField(peer);
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }
}
