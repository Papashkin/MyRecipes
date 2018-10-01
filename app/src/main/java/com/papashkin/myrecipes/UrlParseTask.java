package com.papashkin.myrecipes;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UrlParseTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... strings) {
        StringBuilder sb = new StringBuilder();
        String strLine;
        try {
            URL address = new URL(strings[0]);
            URLConnection urlConnection = address.openConnection();
            urlConnection.connect();
            BufferedReader buff = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));
            while ((strLine = buff.readLine()) != null){
                sb.append(strLine);
//                sb.append("\n");
                }
            buff.close();
            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
