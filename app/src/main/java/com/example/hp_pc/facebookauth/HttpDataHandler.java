package com.example.hp_pc.facebookauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by hp-pc on 14-Jul-18.
 */

//class to handle Http request
public class HttpDataHandler {
    public HttpDataHandler(){

    }

    //this function will send GET request to Api and get result
    public String GetHTTPData(String requestUrl){
        URL url;
        String response = "";
        try{
            url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                String line;
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = bufferedReader.readLine()) != null)
                {
                    response+=line;
                }
            }
            else
                response = "";

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
