package com.dangdang.ddframe.job.example.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@Slf4j
public class RestClient {

    private static RestClient restClient;

    private RestClient() {
    }

    public static RestClient getInstance() {
        if (restClient == null) {
            synchronized (RestClient.class) {
                if (restClient == null) {
                    restClient = new RestClient();
                }
            }
        }
        return restClient;
    }

    public <T> RestResult<T> execute(HttpMethod method, String url_partten,
                                     Object[] url_params, Class<T> responseClazz, String responseContentType) {
        return execute(method, url_partten, url_params,
                new HashMap<String, String>(), null, null, responseClazz, responseContentType, false);
    }

    public <T> RestResult<T> execute(HttpMethod method, String url_partten,
                                     Object[] url_params, Class<T> responseClazz, String responseContentType, boolean useProxy) {
        return execute(method, url_partten, url_params,
                new HashMap<String, String>(), null, null, responseClazz, responseContentType, useProxy);
    }

    public <T> RestResult<T> execute(HttpMethod method, String url_partten,
                                     Object[] url_params, Map<String, String> headers, Class<T> responseClazz, String responseContentType) {
        return execute(method, url_partten, url_params, headers, null, null, responseClazz, responseContentType, false);
    }

    public <T> RestResult<T> execute(HttpMethod method, String url_partten,
                                     Object[] url_params, Map<String, String> headers, Class<T> responseClazz, String responseContentType, boolean useProxy) {
        return execute(method, url_partten, url_params, headers, null, null, responseClazz, responseContentType, useProxy);
    }

    public <T> RestResult<T> execute(HttpMethod method, String url_partten,
                                     Object[] url_params, Map<String, String> headers, InputStream body, Class<T> responseClazz, String responseContentType) {
        return this.execute(method, url_partten, url_params, headers, null, body, responseClazz, responseContentType, false);
    }

    public <T> RestResult<T> execute(HttpMethod method, String url_partten,
                                     Object[] url_params, Map<String, String> headers, String bodyJson, Class<T> responseClazz, String responseContentType) {
        return this.execute(method, url_partten, url_params, headers, bodyJson, null, responseClazz, responseContentType, false);
    }

    /**
     * 请求成功的条件：没有抛出异常
     *
     * @param method
     * @param url_partten
     * @param url_params
     * @param headers
     * @param body
     * @param useProxy
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> RestResult<T> execute(HttpMethod method, String url_partten,
                                     Object[] url_params, Map<String, String> headers, String bodyJson, InputStream body, Class<T> responseClazz, String responseContentType, boolean useProxy) {
        String url = url_partten;
        if (url_params != null && url_params.length > 0) {
            url = String.format(url_partten, url_params);
        }
        HttpRequestBase req = null;
        HttpResponse response = null;
        RestResult<T> result = new RestResult<T>();
        boolean sendSuccess = false;
        try {
            sendSuccess = false;
            if (method == HttpMethod.GET) {
                HttpGet get = new HttpGet(url);
                req = get;
            } else if (method == HttpMethod.POST) {
                HttpPost post = new HttpPost(url);
                if (body != null) {
                    HttpEntity entity = new BufferedHttpEntity(
                            new InputStreamEntity(body));
                    post.setEntity(entity);
                } else if (bodyJson != null) {
                    StringEntity entity = new StringEntity(bodyJson, ContentType.APPLICATION_JSON);
                    post.setEntity(entity);
                }
                req = post;
            } else if (method == HttpMethod.PUT) {
                HttpPut put = new HttpPut(url);
                if (body != null) {
                    HttpEntity entity = new BufferedHttpEntity(
                            new InputStreamEntity(body));
                    put.setEntity(entity);
                } else if (bodyJson != null) {
                    StringEntity entity = new StringEntity(bodyJson);
                    entity.setContentType("application/json");
                    entity.setContentEncoding("UTF-8");
                    put.setEntity(entity);
                }
                req = put;
            } else if (method == HttpMethod.DELETE) {
                HttpDelete delete = new HttpDelete(url);
                req = delete;
            } else {
                throw new IllegalArgumentException("unsupport http method "
                        + method);
            }
            if (null != headers) {
                for (Entry<String, String> header : headers.entrySet()) {
                    req.setHeader(header.getKey(), header.getValue());
                }
            }
            if (useProxy)
                response = HttpClientManager.getProxyHttpClient().execute(req);
            else
                response = HttpClientManager.getDefaultHttpClient().execute(req);
            sendSuccess = true;
            result.setStatus(response.getStatusLine().getStatusCode());
            for (org.apache.http.Header header : response.getAllHeaders()) {
                result.getHeaders().put(header.getName(), header.getValue());
            }
            if (response.getEntity() != null) {
                String content = EntityUtils.toString(response.getEntity());
                if (String.class.equals(responseClazz)) {
                    result.setContent((T) content);
                } else {
                    if (responseContentType.equals("application/xml")) {
                        result.setContent(CommonUtil.xml2object(content, responseClazz));
                    } else if (responseContentType.equals("application/json")) {
                        result.setContent(CommonUtil.json2object(content, responseClazz));
                    } else {
                        throw new IllegalArgumentException("unknow response Content-Type " + responseContentType);
                    }
                }
            }
        } catch (Exception e) {

            CallRemoteFailException ex = new CallRemoteFailException(String.format(url_partten, url_params), "内部处理失败", e);
            throw ex;
        } finally {
            result.setSendSuccess(sendSuccess);
            if (body != null)
                try {
                    body.close();
                } catch (IOException e) {
                    log.warn("close inputstream error {}", e);
                }
            if (req != null) {
                req.abort();
            }
            if (response != null) {
                if (response.getEntity() != null) {
                    try {
                        if (response.getEntity().getContent() != null)
                            response.getEntity().getContent().close();
                    } catch (IllegalStateException | IOException e) {
                        log.warn("close response inputstream error {}", e);
                    }
                }
            }
        }
        return result;
    }
}
