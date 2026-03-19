package com.example.walkingmate.feature.auth.data;

import com.example.walkingmate.feature.auth.model.NaverUserModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NaverUserClient {
    public NaverUserModel fetchUser(String token) {
        String response = requestNaverUser(token);
        return parseUser(response);
    }

    private String requestNaverUser(String token) {
        String header = "Bearer " + token;
        String url = "https://openapi.naver.com/v1/nid/me";

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", header);
        return get(url, requestHeaders);
    }

    private NaverUserModel parseUser(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (!"00".equals(jsonObject.optString("resultcode"))) {
                return null;
            }

            JSONObject object = jsonObject.optJSONObject("response");
            if (object == null && jsonObject.has("response")) {
                object = new JSONObject(jsonObject.getString("response"));
            }
            if (object == null) {
                return null;
            }

            return new NaverUserModel(
                    object.getString("id"),
                    object.optString("nickname", ""),
                    object.optString("name", ""),
                    object.optString("age", ""),
                    object.optString("gender", ""),
                    object.optString("birthyear", "")
            );
        } catch (JSONException e) {
            throw new RuntimeException("네이버 사용자 정보 파싱 실패", e);
        }
    }

    private String get(String url, Map<String, String> requestHeaders) {
        HttpURLConnection connection = connect(url);
        try {
            connection.setRequestMethod("GET");
            for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readBody(connection.getInputStream());
            }
            return readBody(connection.getErrorStream());
        } catch (IOException e) {
            throw new RuntimeException("API 요청 및 응답 실패", e);
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection connect(String apiurl) {
        try {
            URL url = new URL(apiurl);
            return (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiurl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결을 실패했습니다. : " + apiurl, e);
        }
    }

    private String readBody(InputStream body) {
        InputStreamReader streamReader = new InputStreamReader(body);
        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }
            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다. ", e);
        }
    }
}
