package com.example.galleryapp;

import android.os.AsyncTask;

import org.jsoup.Connection.Response;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class RedirectedUrlHelper extends AsyncTask<String, Void, String> {


    OnCompleteListener listener;
    public static final String DATE_FORMAT_1 = "hh:mm a";
    public RedirectedUrlHelper getRedirectedUrl(OnCompleteListener listener){
        this.listener = listener;
        return this;
    }

    @Override
    protected String doInBackground(String... url) {

        String secondURL = "";
        try {
            Response response = Jsoup.connect(url[0]).ignoreContentType(true).execute();
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
