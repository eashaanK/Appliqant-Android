package com.appliqant.quantilus.appliqant;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static constants.Constants.JOB_DETAILS_PARAM;

/**
 * Created by eashaan on 1/5/17.
 */

public class JobDetailsActivity extends Activity {
    private TakeInterview.JobDetails jobDetails;

    /**
     * Initialzer
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_job_details);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        extractData();
        assignDataToUi();
    }

    /**
     * Get data from Parcel
     */
    private void extractData(){
        Bundle extras = getIntent().getExtras();
        this.jobDetails = extras.getParcelable(JOB_DETAILS_PARAM);
    }

    /**
     * Fill UI
     */
    private void assignDataToUi(){
        TextView jobClientLabelField = (TextView) findViewById(R.id.jobClientLabelField);
        TextView jobDescriptionLabelField = (TextView) findViewById(R.id.jobDescriptionLabelField);
        TextView jobLocationLabelField = (TextView) findViewById(R.id.jobLocationLabelField);
        TextView jobTitleLabelField = (TextView) findViewById(R.id.jobTitleLabelField);
        jobClientLabelField.setText(jobDetails.client);
        jobDescriptionLabelField.setText(jobDetails.jobDescription);
        jobLocationLabelField.setText(jobDetails.location);
        jobTitleLabelField.setText(jobDetails.jobTitle);

        Button jobToolBarBackButton = (Button) findViewById(R.id.jobToolBarBackButton);
        jobToolBarBackButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN ) {
                    finishAfterTransition();
                    return true;
                }
                return false;
            }
        });

    }
}
