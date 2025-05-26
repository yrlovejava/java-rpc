package org.xiaobai.rpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.xiaobai.rpc.model.RpcRequest;
import org.xiaobai.rpc.model.RpcResponse;
import org.xiaobai.rpc.serializer.JDKSerializer;
import org.xiaobai.rpc.serializer.Serializer;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 服务代理（JDK 动态代理）
 */
public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理
     * @param o 代理对象
     * @param method 方法
     * @param objects 参数
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        // 指定序列化器
        Serializer serializer = new JDKSerializer();

        // 构造请求
        RpcRequest req = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(objects)
                .build();

        try {
            // 序列化
            byte[] bodyBytes = serializer.serialize(req);
            // 发送请求
            // todo 这里地址被硬编码了（需要使用注册中心和服务发现机制解决）
            try (HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }
}
