package org.apache.skywalking.apm.plugin.lettuce.v4;

import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.protocol.RedisCommand;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

public class StatefulRedisClusterConnectionImplInterceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        RedisCommand command = (RedisCommand)allArguments[0];
        String comandType = String.valueOf(command.getType());
        if (!"AUTH".equals(comandType)) {
            Iterable<RedisURI> redisURIs = (Iterable<RedisURI>)objInst.getSkyWalkingDynamicField();
            AbstractSpan span = ContextManager.createExitSpan("REDIS-Lettuce/" + comandType, redisURIs.toString());
            span.setComponent(ComponentsDefine.REDIS);
            Tags.DB_TYPE.set(span, "Redis");
            SpanLayer.asCache(span);
            Tags.DB_STATEMENT.set(span, command.getArgs().toString());
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        RedisCommand command = (RedisCommand)allArguments[0];
        if (null != command.getOutput().getError()) {
            AbstractSpan span = ContextManager.activeSpan();
            span.errorOccurred();
        }
        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        AbstractSpan span = ContextManager.activeSpan();
        span.errorOccurred();
        span.log(t);
    }
}
