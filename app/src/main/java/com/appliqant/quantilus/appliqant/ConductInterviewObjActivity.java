package com.appliqant.quantilus.appliqant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import static constants.Constants.OBJECTIVE_QUESTION_PARAM;
import static constants.Constants.TIMER_PARAM;

/**
 * Created by eashaan on 1/16/17.
 */

public class ConductInterviewObjActivity extends Activity {

    private final String TAG = "ConductingInterviewActivity";

    private SurfaceView surface_view;
    private Camera mCamera;
    private TextView objectiveTimer;
    private Button objectiveNextButton;
    private TextView objectiveQuestionLabel;
    private TextView objectiveQuestionTitleLabel;

    private int maxTimeAllowed = -1;
    private ObjectiveQuestion[] questions;
    private int currentQuestionIndex;

    private CountDownTimer countDownTimer;

    SurfaceHolder surface_holder        = null;
    SurfaceHolder.Callback sh_callback  = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conduct_interview_obj);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        surface_view = (SurfaceView) findViewById(R.id.surfaceViewCam);
        objectiveNextButton = (Button) findViewById(R.id.objectiveNextButton);
        objectiveTimer = (TextView) findViewById(R.id.objectiveTimer);
        objectiveQuestionLabel = (TextView) findViewById(R.id.objectiveQuestionLabel);
        objectiveQuestionTitleLabel = (TextView) findViewById(R.id.objectiveQuestionTitleLabel);

        extractData();

        if (surface_holder == null) {
            surface_holder = surface_view.getHolder();
        }

        sh_callback = my_callback();
        surface_holder.addCallback(sh_callback);
    }

    private void extractData(){
        Bundle extras = getIntent().getExtras();
        String[] questionStrings = extras.getStringArray(OBJECTIVE_QUESTION_PARAM);
        int[] times = extras.getIntArray(TIMER_PARAM);
        questions = parseQuestions(questionStrings, times);

        next();

        objectiveNextButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == android.view.MotionEvent.ACTION_UP) {
                    next();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Run by tree possible events:
     * 1) User presses "Next" button
     * 2) Time runs out
     * 3) Called at the start, right after data was pulled from bundle
     * TODO: Reset timer, update question with new one
     */
    private void next() {
        objectiveTimer.setText("0");
        if(currentQuestionIndex >= questions.length){
            finishedAllObjectiveQuestions();
            return;
        }
        objectiveQuestionLabel.setText(questions[currentQuestionIndex].question);
        objectiveQuestionTitleLabel.setText("Question " + (currentQuestionIndex+1));
        maxTimeAllowed = questions[currentQuestionIndex].maxTime;

        if(countDownTimer != null)
            countDownTimer.cancel();

        countDownTimer = new CountDownTimer(maxTimeAllowed * 1000, 1000){
            public void onTick(long millisUntilFinished) {
                objectiveTimer.setText( (millisUntilFinished / 1000) + "");
            }

            public void onFinish() {
                next();
            }
        }.start();

        currentQuestionIndex++;
    }

    /**
     * Called when all objective questions have been answered.
     * Move to Pre-Subjective Screen
     */
    private void finishedAllObjectiveQuestions(){
        System.out.println("All Done with Objective questions!");
    }

    /**
     * Manages Camera Rendering
     * @return SurfaceHolder that contains the methods for camera to render on SurfaceView.
     */
    SurfaceHolder.Callback my_callback() {
        SurfaceHolder.Callback ob1 = new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                int frontCamID = getFrontFacingCameraId();
                if(frontCamID == -1){
                    new AlertDialog.Builder(ConductInterviewObjActivity.this)
                            .setTitle("No Camera Found.")
                            .setMessage("Appliqant could not find a front-facing camera on this device. Interview has been stopped.")
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finishAfterTransition();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                mCamera = Camera.open(getFrontFacingCameraId());
                findPreviewSize();

                try {
                    mCamera.setPreviewDisplay(holder);
                    setCameraDisplayOrientation(mCamera);
                    //System.out.println(Device.);

                } catch (IOException exception) {
                    mCamera.release();
                    mCamera = null;
                }
            }

            private void findPreviewSize(){
                Camera.Parameters parameters = mCamera.getParameters();
                List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
                float screenWidth = (float)Resources.getSystem().getDisplayMetrics().widthPixels;
                float screenHeight = (float) Resources.getSystem().getDisplayMetrics().heightPixels;
                float targetAspectRatio =  screenWidth/screenHeight;
                       ;

                Camera.Size targetSelectedSize = previewSizes.get(0);
                for(int i = 1; i < previewSizes.size(); i++){
                    Camera.Size current = previewSizes.get(i);
                    float currentAspectRatio = (float)current.width/current.height;
                    float targetSelectedAspectRatio = (float)targetSelectedSize.width/targetSelectedSize.height;

                    float currError = Math.abs((currentAspectRatio - targetAspectRatio)/targetAspectRatio);
                    float targetSelectedError = Math.abs((targetSelectedAspectRatio - targetAspectRatio)/targetAspectRatio);

                    if(currError == targetSelectedError){
                        continue;
                    }else if(currError < targetSelectedError){
                        targetSelectedSize = current;
                    }
                }
                Log.i(TAG, "Screen ratio: " + targetAspectRatio + ". Selected: " + (float)targetSelectedSize.width/targetSelectedSize.height);
                Camera.Size previewSize = targetSelectedSize;
                parameters.setPreviewSize(previewSize.width, previewSize.height);
                mCamera.setParameters(parameters);
                //Adjust Surface View
                 screenWidth *= 1; //We want full width of the screen to be taken up by camera
                //screenHeight *= 0.75f;
                float videoProportion = (float)previewSize.width/previewSize.height;
                android.view.ViewGroup.LayoutParams lp = surface_view.getLayoutParams();
                lp.width = (int) screenWidth;
                lp.height = (int) (screenWidth / videoProportion);
              /*  if (videoProportion > targetAspectRatio) {
                    lp.width = (int) screenWidth;
                    lp.height = (int) (screenWidth / videoProportion);
                } else {
                    lp.width = (int) (videoProportion * screenHeight);
                    lp.height = (int) screenHeight;
                }*/
                // Commit the layout parameters
                surface_view.setLayoutParams(lp);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mCamera.startPreview();
            }
        };
        return ob1;
    }

    /**
     * Gets the ID of the front facing camera
     * @return id of front facing camera, -1 if none found
     */
    private int getFrontFacingCameraId() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {

                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    /**
     * Corrects Camera's orientation relative to the device.
     * On devices that have the camera on the side rather than top,
     * this method fixes the issue of the surface view rendering camera
     * feed appearing side-ways.
     * @param camera The camera to rotate
     */
    private void setCameraDisplayOrientation(android.hardware.Camera camera) {
        //Camera.Parameters parameters = camera.getParameters();

        android.hardware.Camera.CameraInfo camInfo =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(getFrontFacingCameraId(), camInfo);


        Display display = ((WindowManager) ConductInterviewObjActivity.this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (camInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (camInfo.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private class ObjectiveQuestion{
        public int maxTime;
        public String question;
        public ObjectiveQuestion(String q, int t){
            this.question = q;
            this.maxTime = t;
        }

    }

    /**
     * Assumes that the length of rawQs and rawTs are same. Called by extractData()
     * @param rawQs List of questions
     * @param rawTs List of times
     * @return Parsed list of questions+time put into one object
     */
    private ObjectiveQuestion[] parseQuestions(String[] rawQs, int[] rawTs){
        ObjectiveQuestion[] ans = new ObjectiveQuestion[rawQs.length];
        for(int i = 0; i < ans.length; i++){
            ans[i] = new ObjectiveQuestion(rawQs[i], rawTs[i]);
        }
        return ans;
    }
}
