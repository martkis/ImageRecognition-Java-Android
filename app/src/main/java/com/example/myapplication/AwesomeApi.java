package com.example.myapplication;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class AwesomeApi {

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public AwesomeApi() {
    }

    public interface onPictureRecognize{
        void onResponse(String clazz, String pred);
    }

    public static final String postURL = "http://final.martkis.nazwa.pl/predict";


    public void sendPicture(byte[] image, final onPictureRecognize listener) throws IOException, InterruptedException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "img.jpg", RequestBody.create(MediaType.parse("image/jpeg"), image))
                .build();

        Request request = new Request.Builder()
                .url(postURL)
                .post(requestBody)
                .build();

        //Response response;

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback(){

            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("API","Fail ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("API","Success: "+response.code());
                if (response.code() == 200) {
                    String responseData = response.body().string();
                    JSONObject json = null;
                    try {
                        json = new JSONObject(responseData);
                        final String clazz = json.getString("class");
                        final String pred = json.getString("pred");

                        listener.onResponse(clazz, pred);
                        System.out.println("ODPOWIEDZ1: " + response.toString() );

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                countDownLatch.countDown();
            }

        });

        countDownLatch.await();

    }

}
