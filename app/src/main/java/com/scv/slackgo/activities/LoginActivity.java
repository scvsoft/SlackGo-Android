package com.scv.slackgo.activities;

/**
 * Created by ayelen@scvsoft.com
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.scv.slackgo.R;
import com.scv.slackgo.helpers.Constants;
import com.scv.slackgo.models.LocationsStore;

import java.io.InputStream;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout slackButton;
    private LocationsStore locationsStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        locationsStore = LocationsStore.getInstance();
        String slackCode = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE).getString(Constants.SLACK_TOKEN, null);

        if (slackCode == null) {
            slackButton = (LinearLayout) findViewById(R.id.slackButton);
            slackButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Uri slackUri = Uri.parse(String.format(getString(R.string.slack_link),
                            getString(R.string.slack_scope), getString(R.string.client_id), getString(R.string.redirect_oauth)));

                    Intent intent = new Intent(Intent.ACTION_VIEW, slackUri);

                    startActivity(intent);
                }
            });
        } else {
            Intent nextIntent;
            if (locationsStore.isLocationsListEmpty()) {
                nextIntent = new Intent(this, LocationMapActivity.class);
            } else {
                nextIntent = new Intent(this, LocationsListActivity.class);
            }
            startActivity(nextIntent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.slackButton:
                slackButton.callOnClick();
        }
    }
}

