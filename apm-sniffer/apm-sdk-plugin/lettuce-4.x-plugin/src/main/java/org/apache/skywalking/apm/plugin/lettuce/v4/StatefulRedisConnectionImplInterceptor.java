package org.apache.skywalking.apm.plugin.lettuce.v4;

import com.lambdaworks.redis.protocol.RedisCommand;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceConstructorInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

public class StatefulRedisConnectionImplInterceptor implements InstanceMethodsAroundInterceptor, InstanceConstructorInterceptor {
    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        EnhancedInstance commandHandler = (EnhancedInstance)allArguments[0];
        String peer = (String)commandHandler.getSkyWalkingDynamicField();
        objInst.setSkyWalkingDynamicField(peer);
    }

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        RedisCommand command = (RedisCommand)allArguments[0];
        String comandType = String.valueOf(command.getType());
        if (checkIfNotIgnoreCommandType(comandType)) {
            String peer = String.valueOf(objInst.getSkyWalkingDynamicField());
            AbstractSpan span = ContextManager.createExitSpan("REDIS-Lettuce/" + comandType, peer);
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

    /**
     * Check the comandtype ,ignore  "AUTH"  "CLIENT" "CLUSTER" .
     */
    private Boolean checkIfNotIgnoreCommandType(String comandType) {
        if ("AUTH".equals(comandType) || "CLIENT".equals(comandType) || "CLUSTER".equals(comandType)) {
            return false;
        }
        return true;
    }
}
