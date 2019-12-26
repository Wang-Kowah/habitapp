package com.kowah.habitapp.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class OCRUtil {
    private static final String NOTE_PIC_LOCATION = "/data/habit/pic/note/";
    private static final String BASE_URL = "http://www.shunlushunlu.cn/habit/user/pic?picName=";

    private static CloseableHttpClient httpClient;

    public static void main(String[] args) {
//        System.out.println(updateToken());
        System.out.println(processOCR("_PIC:1/1573363150158.jpg", "24.cafe131ad76121d3915abfa296ef0518.2592000.1577467964.282335-17874287"));
    }

    // 复用httpclient
    private static CloseableHttpClient getHttpClient() {
        RequestConfig config = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build();
        return httpClient == null ? HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build() : httpClient;
    }

    // 更新百度AI平台Token
    public static String updateToken() {
        try {
            httpClient = getHttpClient();
            HttpPost httpPost = new HttpPost("https://aip.baidubce.com/oauth/2.0/token");
            //组装表单
            List<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("grant_type", "client_credentials"));
            pairs.add(new BasicNameValuePair("client_id", "q2uPyBe6LmWTZlvb0g1dzcHV"));
            pairs.add(new BasicNameValuePair("client_secret", "y7S7hAI894BB3LF1yHYmvQEus1B6wPvj"));
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            return jsonObject.getString("access_token");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 请求百度OCR服务，图片的base64编码或者图片URL二选一，优先URL
    public static String processOCR(String picName, String token) {
        try {
            httpClient = getHttpClient();
            HttpPost httpPost = new HttpPost("https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=" + token);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            List<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("url", BASE_URL + picName));
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            JSONObject jsonObject = JSONObject.parseObject(responseBody);

            if (jsonObject.getInteger("error_code") == null) {
                return jsonObject.getJSONArray("words_result")
                        .stream()
                        .map(o -> ((JSONObject) o).getString("words"))
                        .collect(Collectors.joining(""));
            } else if (jsonObject.getInteger("error_code") == 282112) {
                // URL下载超时的话，改用base64请求
                return processBase64(picName, token);
            } else {
                return "error_msg: " + jsonObject.getString("error_msg");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // URL下载超时的话，改用base64请求
    private static String processBase64(String picName, String token) {
        try {
            httpClient = getHttpClient();
            HttpPost httpPost = new HttpPost("https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=" + token);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            List<NameValuePair> pairs = new ArrayList<>();
            String base64Str = image2Base64(NOTE_PIC_LOCATION + picName.substring(5));
            pairs.add(new BasicNameValuePair("image", base64Str));
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            JSONObject jsonObject = JSONObject.parseObject(responseBody);

            if (jsonObject.getInteger("error_code") == null) {
                return jsonObject.getJSONArray("words_result")
                        .stream()
                        .map(o -> ((JSONObject) o).getString("words"))
                        .collect(Collectors.joining(""));
            } else {
                return "error_msg: " + jsonObject.getString("error_msg");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 将图片文件转化为Base64编码字符串
    private static String image2Base64(String path) {
        byte[] data = null;
        // 读取图片字节数组
        try {
            InputStream in = new FileInputStream(path);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(data);
    }
}
