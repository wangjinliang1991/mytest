package com.ai.utils;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class ProtostuffUtil {
    // store Class which cannot serialize/deserialize directly
    private static final Set<Class<?>> WRAPPER_SET = new HashSet<>();

    static {
        WRAPPER_SET.add(List.class);
        WRAPPER_SET.add(ArrayList.class);
        WRAPPER_SET.add(CopyOnWriteArrayList.class);
        WRAPPER_SET.add(LinkedList.class);
        WRAPPER_SET.add(Stack.class);
        WRAPPER_SET.add(Vector.class);
        WRAPPER_SET.add(Map.class);
        WRAPPER_SET.add(HashMap.class);
        WRAPPER_SET.add(TreeMap.class);
        WRAPPER_SET.add(LinkedHashMap.class);
        WRAPPER_SET.add(Hashtable.class);
        WRAPPER_SET.add(SortedMap.class);
        WRAPPER_SET.add(Object.class);
    }

    //register object which need to use wrapperClass to serialize
    public static void registerWrapperClass(Class<?> clazz) {
        WRAPPER_SET.add(clazz);
    }

    public static <T> byte[] serialize(T t, boolean useWrapper) {
        Object serializerObj = t;
        if (useWrapper) {
            serializerObj = SerializeDeserializeWrapper.build(t);
        }
        return serialize(serializerObj);
    }

    /**
     * object -> byte[]
     * @param t
     * @return
     * @param <T>
     */
    public static <T> byte[] serialize(T t){
        Class<?> clazz = t.getClass();
        Object serializerObj = t;
        if (WRAPPER_SET.contains(clazz)) {
            serializerObj = SerializeDeserializeWrapper.build(t);
        }
        return doSerialize(serializerObj);
    }

    private static <T> byte[] doSerialize(T t) {
        Class<T> clazz = (Class<T>) t.getClass();
        // create schema by provided class, lazily created
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        // reuse this buffer to avoid allocating on every serialization
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        byte[] protoStuff = ProtostuffIOUtil.toByteArray(t, schema, buffer);
        buffer.clear();
        return protoStuff;
    }

    public static <T> T deserialize(byte[] data, Class<T> clazz) {
        //check if wrapper
        if (WRAPPER_SET.contains(clazz)) {
            SerializeDeserializeWrapper<T> wrapper = new SerializeDeserializeWrapper<>();
            ProtostuffIOUtil.mergeFrom(data, wrapper, RuntimeSchema.getSchema(SerializeDeserializeWrapper.class));
            return wrapper.getData();
        } else {
            Schema<T> schema = RuntimeSchema.getSchema(clazz);
            T newMessage = schema.newMessage();
            ProtostuffIOUtil.mergeFrom(data, newMessage, schema);
            return newMessage;
        }
    }

    private static class SerializeDeserializeWrapper<T> {
        // wrapper class
        T data;

        public static <T> SerializeDeserializeWrapper<T> build(T data) {
            SerializeDeserializeWrapper<T> wrapper = new SerializeDeserializeWrapper<>();
            wrapper.setData(data);
            return wrapper;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }
}
