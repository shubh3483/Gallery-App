package com.example.galleryapp;

import android.os.AsyncTask;

import org.jsoup.Connection.Response;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class RedirectedUrlHelper extends AsyncTask<String, Void, String> {


    OnCompleteListener listener;

    public RedirectedUrlHelper getRedirectedUrl(OnCompleteListener listener){
        this.listener = listener;
        return this;
    }

    @Override
    protected String doInBackground(String... url) {

        String secondURL = "";
        try {
            /*URL urlTmp = null;
            HttpURLConnection connection = null;

            try {
                urlTmp = new URL(url[0]);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }

            try {
                connection = (HttpURLConnection) urlTmp.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                connection.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
            }

            secondURL = connection.getURL().toString();
            connection.disconnect();*/

            //This one line will give us the redirected URL.
            Response response = Jsoup.connect(url[0]).ignoreContentType(true).execute();
            //System.out.println(response.statusCode() + " : " + response.url().toString());
            secondURL = response.url().toString();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return secondURL;
    }

    protected void onPostExecute(String secondURL) {
        listener.onFetched(secondURL);
    }

    interface OnCompleteListener{
        void onFetched(String url);
    }
}
