package com.npi.whoiswho;

import android.os.Bundle;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.npi.whoiswho.voiceinterface.VoiceActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.npi.whoiswho.pandora.PandoraConnection;
import com.npi.whoiswho.pandora.PandoraErrorCode;
import com.npi.whoiswho.pandora.PandoraException;
//import conversandroid.pandora.PandoraResultProcessor;
//import com.npi.whoiswho.R;

public class MainActivity extends VoiceActivity {

    private static final String LOGTAG = "TALKBOT";

    private static Integer ID_PROMPT_QUERY = 0;    //Id chosen to identify the prompts that involve posing questions to the user
    private static Integer ID_PROMPT_INFO = 1;    //Id chosen to identify the prompts that involve only informing the user
    private long startListeningTime = 0; // To skip errors (see processAsrError method)

    //TODO: USE YOUR OWN PARAMETERS TO MAKE IT WORK
    private String host = "aiaas.pandorabots.com";
    private String userKey = "YOUR USER KEY HERE";
    private String appId = "YOUR APP ID HERE";
    private String botName = "YOUR BOT NAME HERE";
    PandoraConnection pandoraConnection = new PandoraConnection(host, appId, userKey, botName);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set layout
        setContentView(R.layout.activity_main);

        //Initialize the speech recognizer and synthesizer
        initSpeechInputOutput(this);

        //Set up the speech button and progress circle
        setSpeakButton();
        showProgressBar(false);
    }

    private void setSpeakButton() {
        this.setDefaultButtonAppearance();
        // gain reference to speak button
        Button speak = (Button) findViewById(R.id.speech_btn);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start ASR
                indicateListening();
                startListening();
            }
        });
    }

    public void showRecordPermissionExplanation(){
        Toast.makeText(getApplicationContext(), "TalkBot needs your permission to access the microphone in order to perform speech recognition", Toast.LENGTH_SHORT).show();
    }

    public void onRecordAudioPermissionDenied(){
        Toast.makeText(getApplicationContext(), "Sorry, TalkBot cannot work without accessing the microphone", Toast.LENGTH_SHORT).show();
        System.exit(0);
    }

    private void startListening(){

        if(deviceConnectedToInternet()){
            try {

				/*Start listening, with the following default parameters:
					* Recognition model = Free form,
					* Number of results = 1 (we will use the best result to perform the search)
					*/
                startListeningTime = System.currentTimeMillis();
                listen(Locale.ENGLISH, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM, 1); //Start listening
            } catch (Exception e) {
                Log.e(LOGTAG, e.getMessage());
            }
        } else {
            Log.e(LOGTAG, "Device not connected to Internet");
        }
    }

    private void indicateListening() {
        Button button = (Button) findViewById(R.id.speech_btn); //Obtains a reference to the button
        button.setText(getResources().getString(R.string.speechbtn_listening)); //Changes the button's message to the text obtained from the resources folder
        button.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.speechbtn_listening),PorterDuff.Mode.MULTIPLY); //Changes the button's background to the color obtained from the resources folder
    }

    private void setDefaultButtonAppearance(){
        Button button = (Button) findViewById(R.id.speech_btn); //Obtains a reference to the button
        button.setText(getResources().getString(R.string.speechbtn_default)); //Changes the button's message to the text obtained from the resources folder
        button.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.speechbtn_default),PorterDuff.Mode.MULTIPLY);	//Changes the button's background to the color obtained from the resources folder
    }

    private void showProgressBar(Boolean show){
        if(show)
            findViewById(R.id.listeningCircle).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.listeningCircle).setVisibility(View.GONE);
    }

    @Override
    public void processAsrError(int errorCode) {
        setDefaultButtonAppearance();
        showProgressBar(false);

        //Possible bug in Android SpeechRecognizer: NO_MATCH errors even before the ASR
        // has even tried to recognized. We have adopted the solution proposed in:
        // http://stackoverflow.com/questions/31071650/speechrecognizer-throws-onerror-on-the-first-listening
        long duration = System.currentTimeMillis() - startListeningTime;
        if (duration < 500 && errorCode == SpeechRecognizer.ERROR_NO_MATCH) {
            Log.e(LOGTAG, "Doesn't seem like the system tried to listen at all. duration = " + duration + "ms. Going to ignore the error");
            stopListening();
        }
        else {
            String errorMsg = "";
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    errorMsg = "Audio recording error";
                case SpeechRecognizer.ERROR_CLIENT:
                    errorMsg = "Unknown client side error";
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    errorMsg = "Insufficient permissions";
                case SpeechRecognizer.ERROR_NETWORK:
                    errorMsg = "Network related error";
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    errorMsg = "Network operation timed out";
                case SpeechRecognizer.ERROR_NO_MATCH:
                    errorMsg = "No recognition result matched";
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    errorMsg = "RecognitionService busy";
                case SpeechRecognizer.ERROR_SERVER:
                    errorMsg = "Server sends error status";
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    errorMsg = "No speech input";
                default:
                    errorMsg = ""; //Another frequent error that is not really due to the ASR, we will ignore it
            }
            if (errorMsg != "") {
                this.runOnUiThread(new Runnable() { //Toasts must be in the main thread
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Speech recognition error", Toast.LENGTH_LONG).show();
                    }
                });

                Log.e(LOGTAG, "Error when attempting to listen: " + errorMsg);
                try { speak(errorMsg,"EN", ID_PROMPT_INFO); } catch (Exception e) { Log.e(LOGTAG, "TTS not accessible"); }
            }
        }

    }

    @Override
    public void processAsrReadyForSpeech() {}

    @Override
    public void processAsrResults(ArrayList<String> nBestList, float[] nBestConfidences) {
        setDefaultButtonAppearance();
        runOnUiThread(new Runnable() {
            public void run() {
                showProgressBar(true); //the app has started to process the user input, so it shows the progress circle
            }
        });

        if(nBestList!=null){
            if(nBestList.size()>0){
                String userQuery = nBestList.get(0); //We will use the best result

                try {
                    String response = pandoraConnection.talk(userQuery); //Query to pandorabots
                    processBotResults(response); //Process the bot response
                } catch (PandoraException e) {
                    processBotErrors(e.getErrorCode());
                }
            }
        }

    }

    private void processBotErrors(PandoraErrorCode errorCode){
        String errormsg="";

        switch(errorCode)
        {
            case ID: //Problem with app id, user key or robot name (parameters required by Pandorabots)
                errormsg = getResources().getString(R.string.iderror_prompt);
                break;

            case NOMATCH: //There is no response available for the query
                errormsg =  getResources().getString(R.string.iderror_prompt);
                break;

            case IDORHOST: //Problem with ID or the host name indicted for the Pandora service
                errormsg =  getResources().getString(R.string.iderror_prompt);
                break;

            case CONNECTION: //Internet connection error
                errormsg = getResources().getString(R.string.connectionerror_prompt);
                break;

            case PARSE: //Error parsing the robot response
                errormsg = getResources().getString(R.string.iderror_prompt);
                break;

            default:
                errormsg = getResources().getString(R.string.connectionerror_prompt);
                break;
        }

        try {
            speak(errormsg, "EN", ID_PROMPT_INFO);
            Log.e(LOGTAG, getResources().getString(R.string.connectionerror_prompt));
        } catch (Exception e) {
            Log.e(LOGTAG, "The message '" + errormsg + "' could not be synthesized");
        }

    }

    public void processBotResults(String result){

        Log.d(LOGTAG, "Response, contents of that: "+result);

        result = removeTags(result);
        try {
            speak(result,"EN",ID_PROMPT_INFO);
        } catch (Exception e) {
            Log.e(LOGTAG, "The message '"+result+"' could not be synthesized");
            showProgressBar(false);
        }
    }

    private String removeTags(String string) {
        Pattern REMOVE_TAGS = Pattern.compile("<.+?>");

        if (string == null || string.length() == 0) {
            return string;
        }

        Matcher m = REMOVE_TAGS.matcher(string);
        return m.replaceAll("");
    }

    public boolean deviceConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shutdown();
    }

    @Override
    public void onTTSDone(String uttId) {
        if(uttId.equals(ID_PROMPT_QUERY.toString()))
            startListening();

    }

    @Override
    public void onTTSError(String uttId) {
        Log.e(LOGTAG, "TTS error");
    }

    @Override
    public void onTTSStart(String uttId) {
        runOnUiThread(new Runnable() {
            public void run() {
                showProgressBar(false); //the app has finished processing the user input, so it hides the progress circle
            }
        });
        Log.e(LOGTAG, "TTS starts speaking");
    }
}
