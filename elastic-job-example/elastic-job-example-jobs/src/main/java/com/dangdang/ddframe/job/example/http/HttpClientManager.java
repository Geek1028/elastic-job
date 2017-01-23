package com.dangdang.ddframe.job.example.http;

import com.ksyun.ks3.http.HttpClientConfig;
import com.ksyun.ks3.http.HttpClientFactory;
import lombok.Setter;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

@Setter
public class HttpClientManager {

    private static String proxyHost;
    private static int proxyPort;
    private static int maxConnections;
    private static int socketTimeOut;
    private static int connectionTimeOut;

    private static HttpClient defaultHttpClient;

    private static HttpClient proxyHttpClient;

    private static OkHttpClient okHttpClient;

    private static OkHttpClient proxyOkHttpClient;

    static {
        proxyHost = "10.69.33.90";
        proxyPort = 80;
        maxConnections = 200;
        socketTimeOut = 20000;
        connectionTimeOut = 10000;
    }

    public static HttpClient getDefaultHttpClient() {
        if (defaultHttpClient == null) {
            synchronized (HttpClientManager.class) {
                if (defaultHttpClient == null) {
                    HttpClientFactory factory = new HttpClientFactory();
                    HttpClientConfig config = new HttpClientConfig();

                    config.setMaxConnections(maxConnections);
                    config.setSocketTimeOut(socketTimeOut);
                    config.setConnectionTimeOut(connectionTimeOut);
                    defaultHttpClient = factory.createHttpClient(config);
                }
            }
        }
        return defaultHttpClient;
    }

    public static HttpClient getProxyHttpClient() {
        if (proxyHttpClient == null) {
            synchronized (HttpClientManager.class) {
                if (proxyHttpClient == null) {
                    HttpClientFactory factory = new HttpClientFactory();
                    HttpClientConfig config = new HttpClientConfig();
                    if (useProxy()) {
                        config.setProxyHost(proxyHost);
                        config.setProxyPort(proxyPort);
                    }
                    config.setMaxConnections(maxConnections);
                    config.setSocketTimeOut(socketTimeOut);
                    config.setConnectionTimeOut(connectionTimeOut);
                    proxyHttpClient = factory.createHttpClient(config);
                }
            }
        }
        return proxyHttpClient;
    }

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            synchronized (HttpClientManager.class) {
                if (okHttpClient == null) {
                    Builder builder = new Builder()
                            .connectionPool(new ConnectionPool(maxConnections, 60, TimeUnit.SECONDS))
                            .connectTimeout(connectionTimeOut, TimeUnit.MILLISECONDS)
                            .readTimeout(100, TimeUnit.MILLISECONDS)
                            .retryOnConnectionFailure(true);
                    okHttpClient = builder.build();
                }
            }
        }
        return okHttpClient;
    }

    public static OkHttpClient getProxyOkHttpClient() {
        if (proxyOkHttpClient == null) {
            synchronized (HttpClientManager.class) {
                if (proxyOkHttpClient == null) {
                    Builder builder = new Builder()
                            .connectionPool(new ConnectionPool(maxConnections, 60, TimeUnit.SECONDS))
                            .connectTimeout(connectionTimeOut, TimeUnit.MILLISECONDS)
                            .readTimeout(100, TimeUnit.MILLISECONDS)
                            .retryOnConnectionFailure(true);
                    if (useProxy()) {
                        builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
                    }
                    proxyOkHttpClient = builder.build();
                }
            }
        }
        return proxyOkHttpClient;

    }

    public static boolean useProxy() {
        if (StringUtils.isNotBlank(proxyHost) && proxyPort > 0) {
            return true;
        }
        return false;
    }
}
