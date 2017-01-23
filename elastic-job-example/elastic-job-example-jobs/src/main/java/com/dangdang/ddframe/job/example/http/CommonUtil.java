package com.dangdang.ddframe.job.example.http;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class CommonUtil {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    private final static ConcurrentMap<Class<?>, JAXBContext> jaxbContexts = new ConcurrentHashMap<Class<?>, JAXBContext>(64);

    static {
        objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    /**
     * 将json字符串转换成java对象
     *
     * @param _json 准备转换的json字符串
     * @param cls   准备转换的类
     * @return
     * @throws Exception
     */
    public static <T> T json2object(String _json, Class<T> cls) throws JsonParseException, JsonMappingException, IOException {
        if (_json == null || _json.equals(""))
            _json = "{}";
        return (T) objectMapper.readValue(_json, cls);
    }

    @SuppressWarnings("unchecked")
    public static <T> T xml2object(String xml, Class<T> cls) throws JAXBException {
        JAXBContext jaxbContext = getJaxbContext(cls);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (T) unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
    }

    private final static JAXBContext getJaxbContext(Class<?> clazz) {
        JAXBContext jaxbContext = jaxbContexts.get(clazz);
        if (jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(clazz);
                jaxbContexts.putIfAbsent(clazz, jaxbContext);
            } catch (JAXBException ex) {
                ex.printStackTrace();
            }
        }
        return jaxbContext;
    }

    public static void checkArgument(boolean check, String msg) {
        if (!check)
            throw new IllegalArgumentException(msg);
    }
}
