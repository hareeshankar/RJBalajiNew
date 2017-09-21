package com.rjbalaji;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rjbalaji.Common.Common;
import com.rjbalaji.db.dbHelper;
import com.rjbalaji.interfaces.Constants;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

import h.thunderbird.phoenix.rjbalaji.R;

public class ScreenSplash extends Activity {
    private String DROPBOX_FILE_PATH = "https://www.dropbox.com/s/aplwu4zgx0lfoe3/links1.txt?dl=1";
    int currentPos = 0;
    private dbHelper db;
    Handler handleRedirect = new Handler();
    Handler loader_handle = new Handler();
    int maxPos = 5;
    Runnable run = new Runnable() {
        public void run() {
            ScreenSplash.this.redirectToMainScreen();
        }
    };
    Runnable runnable = new Runnable() {
        public void run() {
            if (ScreenSplash.this.currentPos >= ScreenSplash.this.maxPos) {
                ScreenSplash.this.currentPos = 0;
            }
            for (int i = 0; i < ScreenSplash.this.maxPos; i++) {
                int drawable;
                ImageView img = (ImageView) ScreenSplash.this.findViewById(i);
                if (i == ScreenSplash.this.currentPos) {
                    drawable = R.drawable.white_dot;
                } else {
                    drawable = R.drawable.white_light_dot;
                }
                if (img != null) {
                    img.setImageResource(drawable);
                }
            }
            ScreenSplash screenSplash = ScreenSplash.this;
            screenSplash.currentPos++;
            ScreenSplash.this.loader_handle.postDelayed(ScreenSplash.this.runnable, 1000);
        }
    };

    private class readFileFromServer extends AsyncTask<String, Integer, Boolean> {
        String link = "";

        public readFileFromServer(String link) {
            this.link = link;
        }

        protected void onPreExecute() {
        }

        protected Boolean doInBackground(String... fsParams) {
            try {
                dbHelper loDb = new dbHelper(ScreenSplash.this);
                HttpGet httpGet = new HttpGet(this.link);
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
                HttpConnectionParams.setSoTimeout(httpParameters, 5000);
                BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedHttpEntity(new DefaultHttpClient(httpParameters).execute(httpGet).getEntity()).getContent()));
                String date = String.valueOf(new Date());
                int count = 0;
                while (true) {
                    String line = r.readLine();
                    if (line == null) {
                        return Boolean.valueOf(true);
                    }
                    if (count == 0) {
                        int isDeleted = loDb.deleteMusicData();
                        count++;
                    }
                    Common.insertTrackInDb(line, loDb, date);
                }
            } catch (Exception e) {
                Log.e("Error", "Error:" + e);
                return Boolean.valueOf(false);
            }
        }

        protected void onPostExecute(Boolean result) {
            ScreenSplash.this.redirectToMainScreen();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        setContentView(R.layout.screen_splash);
        this.db = new dbHelper(this);
        addLoaderImg();
        if (this.db.getCount() > 0) {
            this.handleRedirect.postDelayed(this.run, 3000);
        } else {
            new readFileFromServer(this.DROPBOX_FILE_PATH).execute(new String[0]);
        }
    }

    private void addLoaderImg() {
        LinearLayout llLoaderImg = (LinearLayout) findViewById(R.id.llLoading);
        llLoaderImg.removeAllViews();
        for (int i = 0; i < this.maxPos; i++) {
            ImageView view = (ImageView) ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_loading, null);
            view.setId(i);
            view.setImageResource(R.drawable.white_light_dot);
            llLoaderImg.addView(view);
        }
        this.loader_handle.post(this.runnable);
    }

    public void onBackPressed() {
    }

    private void redirectToMainScreen() {
        this.loader_handle.removeCallbacks(this.runnable);
        if (this.db.getCount() > 0) {
            startActivity(new Intent(this, MainScreen.class));
            finish();
            return;
        }
        Toast.makeText(this, Constants.MSG_CONNECTION_ERROR_NEW, 0).show();
        finish();
    }
}
