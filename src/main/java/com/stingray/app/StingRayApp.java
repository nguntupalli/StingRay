package com.stingray.app;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class StingRayApp
{
    private static final String TOKEN = "/token";
    private static final String NOTIFICATION_TYPES = "/api/notifications/GetNotificationTypes";
    private static final String SEND_NOTIFICATION = "/api/notifications/SendNotification";

    private static final HttpClient httpClient = HttpClients.createDefault();
    private static final JSONParser parser = new JSONParser();

    public static void main(String[] args)
    {
        if (args.length < 2) {
            System.out.println("Missing url. Usage: com.stingray.app.StingRayApp <url> " +
                    "<action> <emailsCount> \n" +
                    "Action: notificationTypes or test");
            System.exit(1);
        }

        final String url = args[0];
        final String action = args[1];
        int emailsCount = 10;
        if (args.length == 3) {
            emailsCount = Integer.parseInt(args[2]);
        }

        // Get a token
        final String token = getToken(url);
        System.out.println("Received token: " + token);

        if (action.equals("notificationTypes")) {
            // Get Notification types
            final Iterator<JSONObject> notificationTypes = getNotificationTypes(url, token);
            while (notificationTypes.hasNext()) {
                JSONObject object = notificationTypes.next();
                System.out.println(object.toJSONString());
            }
        } else {
            final HttpPost post = createNotificationRequest(url, token, emailsCount);
            final HttpResponse response = executeRequest2(post);
            final String status = response.getStatusLine().getStatusCode() + "";
            System.out.format("SendNotification request status: %s, response: ", status);
            printResponse(response.getEntity());
            System.out.println();
        }
    }

    private static Callable getCallable(final String url, final String token, final int number) {
        // Create a Callable object of anonymous class
        Callable<String> aCallable = new Callable<String>(){
            String result = "";
            public String call() throws Exception {
                final HttpPost post = createNotificationRequest(url, token, number);
                final HttpResponse response = executeRequest2(post);
                result = response.getStatusLine().getStatusCode() + "";
                System.out.format("Thread: %d, status: %s, ", number, result);
                printResponse(response.getEntity());
                System.out.println();
                return result;
            }
        };
        return aCallable;
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

    private static Iterator<JSONObject> getNotificationTypes(final String url, final String token) {
        JSONArray jsonArray = null;
        final HttpGet request = createGetRequest(url + NOTIFICATION_TYPES, token);
        final HttpEntity response = executeRequest(request);

        try {
            if (response != null) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getContent()));
                jsonArray = (JSONArray) parser.parse(reader);
            }
        } catch(ParseException pe) {
            pe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (jsonArray != null) {
            return jsonArray.iterator();
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

    private static HttpGet createGetRequest(final String url, final String token) {
        HttpGet request = new HttpGet(url);
        request.addHeader("content-type", "application/x-www-form-urlencoded");
        request.addHeader("Authorization", "Bearer " + token);

        return request;
    }

    private static HttpPost createNotificationRequest(final String url, final String token, final int emailsCount) {
        HttpPost httpPost = new HttpPost(url + SEND_NOTIFICATION);
        httpPost.addHeader("content-type", "application/x-www-form-urlencoded");
        httpPost.addHeader("Authorization", "Bearer " + token);

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("notificationName", "KD-PY-Scheduled-Test2"));
        params.add(new BasicNameValuePair("notificationBody", "some body"));
        params.add(new BasicNameValuePair("notificationSubject", "some subject"));
        params.add(new BasicNameValuePair("scheduledDate", "9/6/2016 12:00:00 AM"));
        params.add(new BasicNameValuePair("notificationTypeID", "1"));

        final StringBuilder sb = new StringBuilder();
        final String emailPrefix = "user";
        final String emailSuffix = "@user.com";
        final String comma = ",";
        for (int i=0; i<emailsCount; i++) {
            sb.append(emailPrefix + i +emailSuffix);
            if (i+1 < emailsCount) {
                sb.append(comma);
            }
        }
        System.out.println("Notification is being sent to: " + sb.toString());
        params.add(new BasicNameValuePair("sendToUser", sb.toString()));

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

    private static HttpResponse executeRequest2(final HttpPost request) {
        try {
            HttpResponse response = httpClient.execute(request);
            //System.out.println(response);
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