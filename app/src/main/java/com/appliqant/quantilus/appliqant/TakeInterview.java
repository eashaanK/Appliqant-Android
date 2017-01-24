package com.appliqant.quantilus.appliqant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import fileLoader.FileUtils;

import static constants.Constants.JOB_DETAILS_PARAM;
import static constants.Constants.REQUEST_CODE_PICK_FILE;
import static constants.Constants.URL_PARAM;

/**
 * Created by eashaan on 1/3/17.
 */

public class TakeInterview extends Activity {

    //data
    private String url;
    private String jobID, candidateID;
    //ui
    private TextView emailInputLabel;
    private EditText firstnameTxtField;
    private EditText lastnameTxtField;
    private Button jobDetailsButton;
    private Button takeInterviewButton;
    private Button pickResumeButton;
    private TextView pickResumeLabel;

    private static final String TAG = "TakeInterviewActivity";


    // ...
    /**
     * Ran when screen appears
     * @param savedInstanceState its bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_interview);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        emailInputLabel = (TextView) findViewById(R.id.emailInputLabel);
        firstnameTxtField = (EditText) findViewById(R.id.firstnameTxtField);
        lastnameTxtField = (EditText) findViewById(R.id.lastnameTxtField);
        jobDetailsButton = (Button) findViewById(R.id.jobDetailsButton);
        takeInterviewButton = (Button) findViewById(R.id.takeInterviewButton);
        pickResumeButton = (Button) findViewById(R.id.pickResumeButton);
        pickResumeLabel = (TextView) findViewById(R.id.pickResumeLabel);
        emailInputLabel.setText("Fetching...");
        firstnameTxtField.setText("Fetching...");
        lastnameTxtField.setText("Fetching...");
        jobDetailsButton.setEnabled(false);
        takeInterviewButton.setEnabled(false);

        //Hide keyboard from start
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        extractData();

        /*MongoReceiver mongoReceiver = new MongoReceiver();
        Thread t = new Thread(mongoReceiver);
        t.start();*/

        //new GetDataTask().execute("http://" + RESTFUL_API_HOST + ":" + RESTFUL_API_PORT + "/api/status/");

        pickResumeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == android.view.MotionEvent.ACTION_UP) {
                    Intent target = FileUtils.createGetContentIntent();
                    Intent intent = Intent.createChooser(
                            target, getString(R.string.chooser_title));
                    try {
                        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
                    } catch (ActivityNotFoundException e) {
                    }
                }
                return true;
            }

        });

        //TODO: Remove this when connection to MongoDB works
        takeInterviewButton.setEnabled(true);
        takeInterviewButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN ) {
                    Intent i = new Intent(TakeInterview.this, TermsAndConditionsActivity.class);
                    startActivity(i);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_CODE_PICK_FILE:
                if (resultCode == RESULT_OK) {
                    managePickFileRequestCode(data);
                }
                break;
        }

    }

    public void getResumeFromDrive(Intent returnIntent, Uri uri) throws FileNotFoundException {
        Uri returnUri = returnIntent.getData();
        String mimeType = getContentResolver().getType(returnUri);
        Cursor returnCursor =
                getContentResolver().query(returnUri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        String sizeOfSomething = Long.toString(returnCursor.getLong(sizeIndex));

       /* Scanner scanner = new Scanner(getContentResolver().openInputStream(uri));
        while(scanner.hasNext()){
            System.out.println(scanner.nextLine());
        }*/
        Toast.makeText(TakeInterview.this,
                "File Selected: " + name, Toast.LENGTH_LONG).show();
        pickResumeLabel.setText(name);

    }

    private void managePickFileRequestCode(Intent data){
        if (data != null) {
            // Get the URI of the selected file
            final Uri uri = data.getData();
            Log.i(TAG, "Uri Path = " + data.toString());

            //Drive
            if (uri.toString().contains("com.google.android.apps.docs.storage/document")) {

                try {
                    getResumeFromDrive(data, uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            } else { //On System file
                try {
                    // Get the file path from the URI
                   // final String path = FileUtils.getPath(this, uri);
                    File f = new File(new URI(uri.toString()));
                    Toast.makeText(TakeInterview.this,
                            "File Selected: " + f.getName(), Toast.LENGTH_LONG).show();
                    this.pickResumeLabel.setText(f.getName());
                } catch (Exception e) {
                    Log.e("FileSelectorTestActivity", "File select error", e);
                }
            }

        }

    }

    /**
     * Extracts Candidate and Job ID from URL link that opened this screen
     * TODO: Look up Mongo table for "id"
     */
    private void extractData(){
        Bundle extras = getIntent().getExtras();
        url = extras.getString(URL_PARAM);
       /* String[] separationSplit = url.split("&");
        String[] jobIDSplit = separationSplit[0].split("=");
        String[] candidateSplit = separationSplit[1].split("=");
        this.jobID = jobIDSplit[1];
        this.candidateID = candidateSplit[1];*/
        //System.out.println("JobID: " + jobID + " Candidate: " + candidateID);
    }

    /**
     * Called from the thread connected to MongoDB.Fills in candidate's info, or displays error if none found.
     * Allows job info to be sent to JobDetailsActivity if job was found. Otherwise, displays error.
     * @param candidateDetails candidate's information
     * @param jobDetails information about the job that the candidate is getting interviewed for
     */
    private void addDataFromMongo(final CandidateDetails candidateDetails, final JobDetails jobDetails){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //Check candidate details
                if(candidateDetails != null) {
                    emailInputLabel.setText(candidateDetails.email);
                    firstnameTxtField.setText(candidateDetails.fname);
                    lastnameTxtField.setText(candidateDetails.lname);
                }
                else{
                    new AlertDialog.Builder(TakeInterview.this)
                            .setTitle("No Candidate Found.")
                            .setMessage("Your details could not be found in our database. Please contact your interviewer immediately.")
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    firstnameTxtField.setEnabled(false);
                                    lastnameTxtField.setEnabled(false);
                                    emailInputLabel.setText("No email found");
                                    firstnameTxtField.setText("Not Found");
                                    lastnameTxtField.setText("Not Found");
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

                //Check Job Details
                if(jobDetails != null) {
                    //System.out.println(jobDetails.jobDescription);
                    jobDetailsButton.setEnabled(true);
                    jobDetailsButton.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN ) {
                                Bundle extras = new Bundle();
                                extras.putParcelable(JOB_DETAILS_PARAM, jobDetails);
                                Intent i = new Intent(TakeInterview.this, JobDetailsActivity.class);
                                i.putExtras(extras);
                                startActivity(i);
                                return true;
                            }
                            return false;
                        }
                    });
                }
                else{
                    new AlertDialog.Builder(TakeInterview.this)
                            .setTitle("No Job Found.")
                            .setMessage("We could not match you to the job you are interviewing for in our database. It is an error on our part and we apologize for the inconvenience." +
                                    "Please contact you interviewer immediately.")
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    jobDetailsButton.setEnabled(false);
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

                if(jobDetails != null && candidateDetails != null){
                    takeInterviewButton.setEnabled(true);
                    takeInterviewButton.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN ) {
                                Intent i = new Intent(TakeInterview.this, TermsAndConditionsActivity.class);
                                startActivity(i);
                                return true;
                            }
                            return false;
                        }
                    });
                }
            }
        });
    }

    /**
     * Connects to MongoDB and attempts to read data
     */
    /*private class MongoReceiver implements Runnable{

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            MongoClient mongoClient = new MongoClient(PRODUCTION_DB_HOST, 27017);

            MongoDatabase candidateDetailsDB = mongoClient.getDatabase(CANDIDATE_DB_COLLECTION_NAME);
            MongoDatabase jobListDB = mongoClient.getDatabase(JOBLIST_DB_COLLECTION_NAME);

            MongoCollection candidateCollection = candidateDetailsDB.getCollection(CANDIDATE_DB_COLLECTION_NAME);
            MongoCollection jobListCollection = jobListDB.getCollection(JOBLIST_DB_COLLECTION_NAME);

            //Search for candidate
            MongoCursor cursor = candidateCollection.find().iterator();
            CandidateDetails candidateDetails = null;

            while (cursor.hasNext()) {
                Document doc = (Document)cursor.next();
                if(doc.get("_id").toString().equals(candidateID)){
                    candidateDetails = new CandidateDetails((String)doc.get("email"), (String)doc.get("fname"), (String)doc.get("lname"));
                    break;
                }
            }

            //Search for jobInfo
            cursor = jobListCollection.find().iterator();
            JobDetails jobDetails = null;
            while (cursor.hasNext()) {
                Document doc = (Document)cursor.next();
                if(doc.get("_id").toString().equals(jobID)){
                    Document location = (Document)doc.get("location");
                    String htmlDescription = (String)doc.get("description");

                    jobDetails = new JobDetails((String)doc.get("title"), (String)doc.get("Client"),
                            (String)location.get("formatted_address"),  Methods.fromHtml(htmlDescription));
                    break;
                }
            }

            addDataFromMongo(candidateDetails, jobDetails);
            mongoClient.close();
        }
    }*/

    class GetDataTask extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(TakeInterview.this);
            progressDialog.setMessage("Loading data...");
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return getData(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "Network error!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            System.out.println(result);
            if(progressDialog != null){
                progressDialog.dismiss();
            }
        }

        private String getData(String urlPath) throws IOException{
            BufferedReader bufferedReader = null;
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urlPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");//set header
                urlConnection.connect();

                //Read data from server
                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            } finally{
                if(bufferedReader != null){
                    bufferedReader.close();
                }
            }
            return result.toString();
        }

    }

    /**
     * Contains details about the Candidate
     */
    private class CandidateDetails{
        public final String email, fname, lname;
        public CandidateDetails(String e, String f, String l){
            this.email = e;
            this.fname = f;
            this.lname = l;
        }
    }

    /**
     * Contains details about the Job assigned to the Candidate.
     * Parceable allows it to be sent to a different activity as an Object.
     */
    public static class JobDetails implements Parcelable{
        public final String jobTitle, client, location, jobDescription;
        public JobDetails(String jT, String cl, String loc, String des) {
            this.jobTitle = jT;
            this.client = cl;
            this.location = loc;
            this.jobDescription = des;
        }

        public JobDetails(Parcel in){
            String[] data = new String[4];
            in.readStringArray(data);
            this.jobTitle = data[0];
            this.client = data[1];
            this.location = data[2];
            this.jobDescription = data[3];
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeStringArray(new String[] {this.jobTitle,
                                                this.client,
                                                this.location,
                                                this.jobDescription});
        }
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public JobDetails createFromParcel(Parcel in) {
                return new JobDetails(in);
            }

            public JobDetails[] newArray(int size) {
                return new JobDetails[size];
            }
        };
    }


}
