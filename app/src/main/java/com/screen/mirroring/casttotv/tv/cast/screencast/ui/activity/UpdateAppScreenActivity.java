package com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.screen.mirroring.casttotv.tv.cast.screencast.R;

import java.util.Timer;
import java.util.TimerTask;

public class UpdateAppScreenActivity extends AppCompatActivity {

    TextView skipTextView;
    int countSecs = 6;
    TextView descriptionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_app_screen);

        final TextView installBtn = findViewById(R.id.installBtn);
        installBtn.setText("Download");
        skipTextView = findViewById(R.id.skipTextView);
        skipTextView.setOnClickListener(v -> finish());

        descriptionText = findViewById(R.id.descriptionText);
        descriptionText.setText(getString(R.string.app_name) + " is deprecated. So please download latest version application.");

        installBtn.setOnClickListener(v -> {
            try {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getPlayUrl())));
            } catch (android.content.ActivityNotFoundException anfe) {
                anfe.printStackTrace();
            }
        });

        try {
            startTime();
        } catch (Exception ex) {
            ex.printStackTrace();
            finish();
        }
    }

//    public String getPlayUrl() {
//        return (FirebaseRemoteConfig.getInstance().getString(Constants.PLAY_URL_KEY));
//    }

    private void startTime() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                countSecs--;
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        skipTextView.setText("Skip " + countSecs + "s");
                        skipTextView.setEnabled(false);
                        if (countSecs == 0) {
                            skipTextView.setText("Skip >>");
                            skipTextView.setEnabled(true);
                            timer.cancel();
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    @Override
    public void onBackPressed() {

    }
}
