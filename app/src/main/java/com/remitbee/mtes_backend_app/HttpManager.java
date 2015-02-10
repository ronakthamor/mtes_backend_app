package com.remitbee.mtes_backend_app;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Thamor on 2014-12-10.
 */
public class HttpManager {

    public static String getData(RequestPackage p){

        BufferedReader reader = null;
        String uri = p.getUri();

        if(p.getMethod().equals("GET")){
            uri += "?" + p.getEncodedParams();
        }

        try{
            URL url = new URL(uri);
            System.out.println("Posted URL is: " + url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(p.getMethod());

            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;
            while((line = reader.readLine()) != null){
                sb.append(line + "\n");
            }
            return sb.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        finally{
            if(reader != null){
                try{
                    reader.close();
                }
                catch (Exception e){
                    e.printStackTrace();
                    return null;
                }

            }
        }

    }

}
