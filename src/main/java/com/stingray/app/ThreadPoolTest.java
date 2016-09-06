package com.stingray.app;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by z001j60 on 9/4/16.
 */
public class ThreadPoolTest {


    private static final String TOKEN = "/token";
    private static final String SEND_NOTIFICATION = "/api/notifications/SendNotification";

    private static final HttpClient httpClient = HttpClients.createDefault();
    private static final JSONParser parser = new JSONParser();

    public static void main(String[] args)
    {
        if (args.length != 1) {
            System.out.println("Missing url. Usage: com.stingray.app.StingRayApp <url>");
            System.exit(1);
        }

        final String url = args[0];

        // Get a token
        final String token = getToken(url);
        System.out.println("Received token: " + token);

        final HttpPost post = createNotificationRequest(url, token, 1);
        final HttpResponse response = executeRequest2(post);
        printResponse(response.getEntity());
        final String result = response.getStatusLine().getStatusCode() + "";

//        ExecutorService fixedPool = Executors.newFixedThreadPool(10);
//        for (int i=0; i<10; i++) {
//            fixedPool.submit(getCallable(url, token, i));
//        }
//        shutdownAndAwaitTermination(fixedPool);
    }

    private static String getToken(final String url) {
        final HttpPost post = createInitialPostRequest(url, TOKEN);
        final HttpEntity response = executeRequest(post);
        final JSONObject json = parseJSON(response);
        //printResponse(response);
        if (json != null) {
            return (String) json.get("access_token");
        } else {
            throw new RuntimeException("Token fetch failed, try again later.");
        }
    }

    private static JSONObject parseJSON(final HttpEntity entity) {
        JSONObject jsonObject = null;
        try {
            if (entity != null) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(entity.getContent()));
                jsonObject = (JSONObject) parser.parse(reader);
            }
        } catch(ParseException pe) {
            pe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return jsonObject;
    }

    private static HttpPost createNotificationRequest(final String url, final String token, final int number) {
        HttpPost httpPost = new HttpPost(url + SEND_NOTIFICATION);
        httpPost.addHeader("content-type", "application/x-www-form-urlencoded");
        httpPost.addHeader("Authorization", "Bearer " + token);

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("notificationName", "KD-PY-Scheduled-Test2"));
        params.add(new BasicNameValuePair("notificationBody", "some body"));
        params.add(new BasicNameValuePair("notificationSubject", "some subject"));
        params.add(new BasicNameValuePair("sendToUser", "user" + number + "@user.com"));
        params.add(new BasicNameValuePair("scheduledDate", "9/6/2016 12:00:00 AM"));
        params.add(new BasicNameValuePair("notificationTypeID", "1"));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return httpPost;
    }

    private static HttpPost createInitialPostRequest(final String url, final String endPoint) {
        HttpPost httpPost = new HttpPost(url + endPoint);
        httpPost.addHeader("content-type", "application/x-www-form-urlencoded");

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", "user@user.com"));
        params.add(new BasicNameValuePair("password", "User$23"));
        params.add(new BasicNameValuePair("grant_type", "password"));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return httpPost;
    }

    private static HttpEntity executeRequest(final HttpUriRequest request) {
        try {
            HttpResponse response = httpClient.execute(request);

            final int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                System.out.println("Request failed, response code : " + status);
            }

            return response.getEntity();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HttpResponse executeRequest2(final HttpPost httpPost) {
        try {


            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("notificationName", "KD-PY-Scheduled-Test2"));
            params.add(new BasicNameValuePair("notificationBody", "some body"));
            params.add(new BasicNameValuePair("notificationSubject", "some subject"));
            params.add(new BasicNameValuePair("sendToUser", "user" + 1 + "@user.com"));
            params.add(new BasicNameValuePair("scheduledDate", "9/6/2016 12:00:00 AM"));
            params.add(new BasicNameValuePair("notificationTypeID", "1"));

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            HttpResponse response = httpClient.execute(httpPost);
            System.out.println(response);
            return response;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void printResponse(final HttpEntity entity) {
        try {
            if (entity != null) {
                // EntityUtils to get the response content
                String content =  EntityUtils.toString(entity);
                System.out.println(content);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
