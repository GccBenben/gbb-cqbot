package com.gccbenben.qqbotservice.utils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * http工具类
 *
 * @author GccBenben
 * @date 2022/04/24
 */
@Slf4j
public class HttpUtil {

    private static RequestConfig requestConfig = RequestConfig.custom()
            //从连接池中获取连接的超时时间
            // 要用连接时尝试从连接池中获取，若是在等待了一定的时间后还没有获取到可用连接（比如连接池中没有空闲连接了）则会抛出获取连接超时异常。
            .setConnectionRequestTimeout(15000)
            //与服务器连接超时时间：httpclient会创建一个异步线程用以创建socket连接，此处设置该socket的连接超时时间
            //连接目标url的连接超时时间，即客服端发送请求到与目标url建立起连接的最大时间。超时时间3000ms过后，系统报出异常
            .setConnectTimeout(15000)
            //socket读数据超时时间：从服务器获取响应数据的超时时间
            //连接上一个url后，获取response的返回等待时间 ，即在与目标url建立连接后，等待放回response的最大时间，在规定时间内没有返回响应的话就抛出SocketTimeout。
            .setSocketTimeout(15000)
            .build();


    /**
     * 通过字符串发送http
     *
     * @param requestMethod 请求方法
     * @param url           url
     * @param params        参数个数
     * @param header        头
     * @return {@link String}
     */
    public static String sendHttpByString(HttpRequestMethedEnum requestMethod, String url, String params, Map<String, String> header) {
        //1、创建一个HttpClient对象;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = null;
        String responseContent = null;
        //2、创建一个Http请求对象并设置请求的URL，比如GET请求就创建一个HttpGet对象，POST请求就创建一个HttpPost对象;
        HttpRequestBase request = requestMethod.createRequest(url);
        request.setConfig(requestConfig);
        //3、如果需要可以设置请求对象的请求头参数，也可以往请求对象中添加请求参数;
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }
        // 往对象中添加相关参数
        try {
            if (params != null) {
                ((HttpEntityEnclosingRequest) request).setEntity(
                        new StringEntity(params));
            }
            //4、调用HttpClient对象的execute方法执行请求;
            httpResponse = httpClient.execute(request);

            //5、获取请求响应对象和响应Entity;
            HttpEntity httpEntity = httpResponse.getEntity();
            //6、从响应对象中获取响应状态，从响应Entity中获取响应内容;
            if (httpEntity != null) {
                responseContent = EntityUtils.toString(httpEntity, "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //7、关闭响应对象;
                if (httpResponse != null) {
                    httpResponse.close();
                }
                //8、关闭HttpClient.
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    /**
     * 通过字符串发送http,获得返回值以及cookie
     *
     * @param requestMethod 请求方法
     * @param url           url
     * @param params        参数个数
     * @param header        头
     * @return {@link Map}
     */
    public static Map sendHttpByStringGetCookie(HttpRequestMethedEnum requestMethod, String url, String params, Map<String, String> header) {
        //1、创建一个HttpClient对象;
        BasicCookieStore cookieStore = new BasicCookieStore();
//        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        CloseableHttpResponse httpResponse = null;
        String responseContent = null;
        Map response = new HashMap();
        //2、创建一个Http请求对象并设置请求的URL，比如GET请求就创建一个HttpGet对象，POST请求就创建一个HttpPost对象;
        HttpRequestBase request = requestMethod.createRequest(url);
        request.setConfig(requestConfig);
        //3、如果需要可以设置请求对象的请求头参数，也可以往请求对象中添加请求参数;
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }
        // 往对象中添加相关参数
        try {
            if (params != null) {
                ((HttpEntityEnclosingRequest) request).setEntity(
                        new StringEntity(params));
            }
            //4、调用HttpClient对象的execute方法执行请求;
            httpResponse = httpClient.execute(request);

            //5、获取请求响应对象和响应Entity;
            HttpEntity httpEntity = httpResponse.getEntity();
            //6、从响应对象中获取响应状态，从响应Entity中获取响应内容;
            if (httpEntity != null) {
                responseContent = EntityUtils.toString(httpEntity, "UTF-8");
            }

            //获取cookie
            List<Cookie> cookies = cookieStore.getCookies();
            response.put("cookies", cookies);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //7、关闭响应对象;
                if (httpResponse != null) {
                    httpResponse.close();
                }
                //8、关闭HttpClient.
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        response.put("responseContent", responseContent);
        return response;
    }

    /**
     * 发送http请求
     *
     * @param requestMethod 请求方法(HttpGet、HttpPost、HttpPut、HttpDelete）
     * @param url           url(请求路径)
     * @param params        参数个数
     * @param header        请求头
     * @return {@link String}
     */
    public static String sendHttp(HttpRequestMethedEnum requestMethod, String url, Map<String, Object> params, Map<String, String> header) {
        //1、创建一个HttpClient对象;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = null;
        String responseContent = null;
        //2、创建一个Http请求对象并设置请求的URL，比如GET请求就创建一个HttpGet对象，POST请求就创建一个HttpPost对象;
        HttpRequestBase request = requestMethod.createRequest(url);
        request.setConfig(requestConfig);
        //3、如果需要可以设置请求对象的请求头参数，也可以往请求对象中添加请求参数;
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }
        // 往对象中添加相关参数
        try {
            if (params != null) {
                ((HttpEntityEnclosingRequest) request).setEntity(
                        new StringEntity(createRequestBody(params), "UTF-8"));
            }
            //4、调用HttpClient对象的execute方法执行请求;
            httpResponse = httpClient.execute(request);
            //5、获取请求响应对象和响应Entity;
            HttpEntity httpEntity = httpResponse.getEntity();
            //6、从响应对象中获取响应状态，从响应Entity中获取响应内容;
            if (httpEntity != null) {
                responseContent = EntityUtils.toString(httpEntity, "UTF-8");
//                log.info(responseContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //7、关闭响应对象;
                if (httpResponse != null) {
                    httpResponse.close();
                }
                //8、关闭HttpClient.
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    private static String createRequestBody(Map<String, Object> params) {
        String request = "";
        for (String key : params.keySet()) {
            request += key + "=" + params.get(key) + "&";
        }
        return request;
    }

    public static String sendHttpByJson(HttpRequestMethedEnum requestMethod, String url, JsonNode params, Map<String, String> header) {
        //1、创建一个HttpClient对象;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = null;
        String responseContent = null;
        //2、创建一个Http请求对象并设置请求的URL，比如GET请求就创建一个HttpGet对象，POST请求就创建一个HttpPost对象;
        HttpRequestBase request = requestMethod.createRequest(url);
        request.setConfig(requestConfig);
        //3、如果需要可以设置请求对象的请求头参数，也可以往请求对象中添加请求参数;
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }
        // 往对象中添加相关参数
        try {
            if (params != null) {
                ((HttpEntityEnclosingRequest) request).setEntity(
                        new StringEntity(JSONUtil.toJSONString(params),
                                ContentType.create("application/json", "UTF-8")));
            }
            //4、调用HttpClient对象的execute方法执行请求;
            httpResponse = httpClient.execute(request);
            //5、获取请求响应对象和响应Entity;
            HttpEntity httpEntity = httpResponse.getEntity();
            //6、从响应对象中获取响应状态，从响应Entity中获取响应内容;
            if (httpEntity != null) {
                responseContent = EntityUtils.toString(httpEntity, "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //7、关闭响应对象;
                if (httpResponse != null) {
                    httpResponse.close();
                }
                //8、关闭HttpClient.
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    /**
     * 通过json发送http,并附带cookie
     *
     * @param requestMethod 请求方法
     * @param url           url
     * @param params        参数个数
     * @param header        头
     * @param cookies       饼干
     * @return {@link String}
     */
    public static String sendHttpByJson(HttpRequestMethedEnum requestMethod, String url, JsonNode params, Map<String, String> header, List<Cookie> cookies) {
        //1、创建一个HttpClient对象;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = null;
        String responseContent = null;
        //2、创建一个Http请求对象并设置请求的URL，比如GET请求就创建一个HttpGet对象，POST请求就创建一个HttpPost对象;
        HttpRequestBase request = requestMethod.createRequest(url);
        request.setConfig(requestConfig);
        //3、如果需要可以设置请求对象的请求头参数，也可以往请求对象中添加请求参数;
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }

        //如果cookie不为空则设置cookie
        if(cookies != null){
            CookieStore cookieStore = new BasicCookieStore();
            httpClient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .build();
            for(Cookie cookie : cookies){
                cookieStore.addCookie(cookie);
            }
        }
        // 往对象中添加相关参数
        try {
            if (params != null) {
                ((HttpEntityEnclosingRequest) request).setEntity(
                        new StringEntity(JSONUtil.toJSONString(params),
                                ContentType.create("application/json", "UTF-8")));
            }
            //4、调用HttpClient对象的execute方法执行请求;
            httpResponse = httpClient.execute(request);
            //5、获取请求响应对象和响应Entity;
            HttpEntity httpEntity = httpResponse.getEntity();
            //6、从响应对象中获取响应状态，从响应Entity中获取响应内容;
            if (httpEntity != null) {
                responseContent = EntityUtils.toString(httpEntity, "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //7、关闭响应对象;
                if (httpResponse != null) {
                    httpResponse.close();
                }
                //8、关闭HttpClient.
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    public enum HttpRequestMethedEnum {
        // HttpGet请求
        HttpGet {
            @Override
            public HttpRequestBase createRequest(String url) {
                return new HttpGet(url);
            }
        },
        // HttpPost 请求
        HttpPost {
            @Override
            public HttpRequestBase createRequest(String url) {
                return new HttpPost(url);
            }
        },
        // HttpPut 请求
        HttpPut {
            @Override
            public HttpRequestBase createRequest(String url) {
                return new HttpPut(url);
            }
        },
        // HttpDelete 请求
        HttpDelete {
            @Override
            public HttpRequestBase createRequest(String url) {
                return new HttpDelete(url);
            }
        };

        public HttpRequestBase createRequest(String url) {
            return null;
        }

    }
}
