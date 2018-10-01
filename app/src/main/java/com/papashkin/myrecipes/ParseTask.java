package com.papashkin.myrecipes;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class ParseTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... objects) {
        String charset = "UTF-8";
        StringBuilder sb = new StringBuilder();
        boolean isReady = false;
        String strLine;
        do {
            try {
                URL address = new URL(objects[0]);
                URLConnection urlConnection = address.openConnection();
                urlConnection.connect();
                BufferedReader buff = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), charset));
                while ((strLine = buff.readLine()) != null){
                    if (strLine.contains("charset=")){
                        String[] parts = strLine.split("charset=|/>");
                        if (!charset.equals(parts[1].split("\"")[0])){
                            charset = parts[1].split("\"")[0];
                            break;
                        }
                    }
                    if(strLine.contains("<title>")){
                        sb.append(strLine);
                        isReady = true;
                        break;
                    }
                }
                buff.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                return "Exception";
            }
        } while (!isReady);

        String[] partStr = sb.toString().split(">|</");
        return partStr[1];
    }
}
