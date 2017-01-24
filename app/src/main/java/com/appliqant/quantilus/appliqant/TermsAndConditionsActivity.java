package com.appliqant.quantilus.appliqant;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import constants.Methods;

import static constants.Constants.OBJECTIVE_QUESTION_PARAM;
import static constants.Constants.TERMS_OF_SERVICE;
import static constants.Constants.TIMER_PARAM;

/**
 * Created by eashaan on 1/6/17.
 */

public class TermsAndConditionsActivity extends Activity {

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms_and_conditions);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        assignDataToUi();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void assignDataToUi(){
        TextView termCondLabelField = (TextView) findViewById(R.id.termCondLabelField);

        termCondLabelField.setText(Methods.fromHtml(TERMS_OF_SERVICE));
        termCondLabelField.setMovementMethod(LinkMovementMethod.getInstance());

        Button jobToolBarBackButton = (Button) findViewById(R.id.termCondToolBarCancelButton);
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

        Button termCondIAcceptButton = (Button) findViewById(R.id.termCondIAcceptButton);
        termCondIAcceptButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN ) {
                    Intent i = new Intent(TermsAndConditionsActivity.this, PreObjectiveQActivity.class);
                    startActivity(i);
                    return true;
                }
                return false;
            }
        });

    }
}
