package com.example.myapplication;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import android.os.SystemClock;

import com.camerakit.CameraKitView;

import java.io.IOException;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private CameraKitView cameraView;
    private Button mButton;
    private int mInterval = 3000; // 5 seconds by default, can be changed later
    private Handler mHandler;
    private boolean mTickerStopped = false;
    private Runnable mTicker;
    Calendar mCalendar;
    private Bitmap bitmap;
    String url = "http://serwer.fablestyl.pl/uploads";
    HttpClient httpclient;
    HttpPost httpPost;
    HttpEntity entity;
    private byte[] mImage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = findViewById(R.id.camera);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(this);

        mHandler = new Handler();
        mTickerStopped = false;

        /**
         * requests a tick on the next hard-second boundary
         */
        mTicker = new Runnable() {
            public void run() {
                if (mTickerStopped) return;
                if (mCalendar == null) {
                    mCalendar = Calendar.getInstance();
                }
                mCalendar.setTimeInMillis(System.currentTimeMillis());

                doAwesomePhoto();
                Log.i("AA", "logs1");
                long now = SystemClock.uptimeMillis();
                long next = now + (mInterval - now % mInterval);
                mHandler.postAtTime(mTicker, next);
            }
        };
    }

    protected void onStart() {
        super.onStart();
        cameraView.onStart();
    }

    public void toggleTickerState() {
        mTickerStopped = !mTickerStopped;
        if (!mTickerStopped) {
            mTicker.run();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
        mTickerStopped = false;

        mTicker.run();
        startBackgroundSending();
    }

    @Override
    protected void onPause() {
        cameraView.onPause();
        mTickerStopped = true;
//        stopBackgroundSending();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraView.onStop();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        toggleTickerState();
        doAwesomePhoto();
    }

    public void doAwesomePhoto() {
        cameraView.captureImage(new CameraKitView.ImageCallback() {
            @Override
            public void onImage(CameraKitView cameraKitView, final byte[] capturedImage) {

                mImage = capturedImage;

            }
        });
    }
    public void sendResponseToScreen(String message) {
        Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_LONG).show();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    Timer mTimer = new Timer();
    byte[] mmCurrentSendingImage;

    TimerTask mPictureSendingTask = new TimerTask() {

        @Override
        public void run() {
            if (mmCurrentSendingImage != mImage && mImage != null) {
                mmCurrentSendingImage = mImage;

                Log.i("AAA", "Sending new picture");
                try {
                    AwesomeApi api = new AwesomeApi();
                    api.sendPicture(mmCurrentSendingImage, new AwesomeApi.onPictureRecognize() {
                        @Override
                        public void onResponse(final String clazz, final String pred) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    sendResponseToScreen(clazz + ":" + pred);
                                }
                            });
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    public void startBackgroundSending() {
        mTimer.schedule(mPictureSendingTask, 0, mInterval / 2);
    }

    public void stopBackgroundSending() {
        mTimer.cancel();
        mTimer.purge();
    }

}