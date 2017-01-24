package com.appliqant.quantilus.appliqant;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static constants.Constants.OBJECTIVE_QUESTION_PARAM;
import static constants.Constants.TIMER_PARAM;

/**
 * Created by eashaan on 1/18/17.
 * The purpose of this class is to make an HTTP request and get the objective questions data
 */

public class PreSubjectiveQActivity extends Activity {

    private TextView preSubjQInstructionLabel;
    private Button preSubjQStartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_subjective_q);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        preSubjQStartButton = (Button) findViewById(R.id.preSubjQStartButton);
        preSubjQInstructionLabel = (TextView) findViewById(R.id.preSubjQInstructionLabel);

        preSubjQInstructionLabel.setText("You are done with the open-ended questions.\nPress Next to move onto the multiple choice");
        preSubjQStartButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    Bundle extras = new Bundle();
                    extras.putStringArray(OBJECTIVE_QUESTION_PARAM, new String[]{
                            "Hello,\nThis is an objective question. Can you see it?",
                            "a\na\na\na\n\na\na\n\na\n12\nfinish",
                            "This is question\tthree with a \ttab?"
                    });
                    extras.putIntArray(TIMER_PARAM, new int[]{
                            5, 5, 5
                    });

                    Intent i = new Intent(PreSubjectiveQActivity.this, ConductInterviewObjActivity.class);
                    i.putExtras(extras);
                    startActivity(i);
                    return true;
                }
                return false;
            }
        });
    }
}
