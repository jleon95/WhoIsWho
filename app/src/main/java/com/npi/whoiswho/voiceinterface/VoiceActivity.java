package com.npi.whoiswho.voiceinterface;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public abstract class VoiceActivity extends Activity implements RecognitionListener, OnInitListener{

    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 22;
    private SpeechRecognizer myASR;
    private TextToSpeech myTTS;
    Activity ctx;

    private static final String LOGTAG = "VOICEACTIVITY";

    public void initSpeechInputOutput(Activity ctx) {

        this.ctx = ctx;
        PackageManager packManager = ctx.getPackageManager();

        setTTS();

        // Find out whether speech recognition is supported
        List<ResolveInfo> intActivities = packManager.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (intActivities.size() != 0 || "generic".equals(Build.BRAND.toLowerCase(Locale.US))) {
            myASR = SpeechRecognizer.createSpeechRecognizer(ctx);
            myASR.setRecognitionListener(this);
        }
        else
            myASR = null;
    }

    public void checkASRPermission() {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // If  an explanation is required, show it
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) ctx, Manifest.permission.RECORD_AUDIO))
                showRecordPermissionExplanation();

            // Request the permission.
            ActivityCompat.requestPermissions((Activity) ctx, new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO); //Callback in "onRequestPermissionResult"
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(LOGTAG, "Record audio permission granted");
            } else {
                Log.i(LOGTAG, "Record audio permission denied");
                onRecordAudioPermissionDenied();
            }
        }
    }

    public abstract void showRecordPermissionExplanation();
    public abstract void onRecordAudioPermissionDenied();

    public void listen(final Locale language, final String languageModel, final int maxResults) throws Exception
    {
        checkASRPermission();

        if((languageModel.equals(RecognizerIntent.LANGUAGE_MODEL_FREE_FORM) || languageModel.equals(RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)) && (maxResults>=0))
        {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            // Specify the calling package to identify the application
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, ctx.getPackageName());
            //Caution: be careful not to use: getClass().getPackage().getName());

            // Specify language model
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);

            // Specify how many results to receive. Results listed in order of confidence
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxResults);

            // Specify recognition language
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);

            myASR.startListening(intent);

        }
        else {
            Log.e(LOGTAG, "Invalid params to listen method");
            throw new Exception("Invalid params to listen method"); //If the input parameters are not valid, it throws an exception
        }

    }

    public void stopListening(){
        myASR.stopListening();
    }

    @SuppressLint("InlinedApi")
	/*
	 * (non-Javadoc)
	 *
	 * Invoked when the ASR provides recognition results
	 *
	 * @see android.speech.RecognitionListener#onResults(android.os.Bundle)
	 */
    @Override
    public void onResults(Bundle results) {
        if(results!=null){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {  //Checks the API level because the confidence scores are supported only from API level 14:
                //http://developer.android.com/reference/android/speech/SpeechRecognizer.html#CONFIDENCE_SCORES
                //Processes the recognition results and their confidences
                processAsrResults(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION), results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES));
                //											Attention: It is not RecognizerIntent.EXTRA_RESULTS, that is for intents (see the ASRWithIntent app)
            }
            else {
                //Processes the recognition results and their confidences
                processAsrResults(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION), null);
            }
        }
        else
            //Processes recognition errors
            processAsrError(SpeechRecognizer.ERROR_NO_MATCH);
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        processAsrReadyForSpeech();
    }

    @Override
    public void onError(int errorCode) {
        processAsrError(errorCode);
    }

    @Override
    public void onBeginningOfSpeech() {	}

    @Override
    public void onBufferReceived(byte[] buffer) { }

    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onEvent(int arg0, Bundle arg1) {}

    @Override
    public void onPartialResults(Bundle arg0) {}

    @Override
    public void onRmsChanged(float arg0) {}

    public abstract void processAsrResults(ArrayList<String> nBestList, float [] nBestConfidences);

    public abstract void processAsrReadyForSpeech();

    public abstract void processAsrError(int errorCode);

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void setTTS()
    {
        myTTS = new TextToSpeech(ctx,(OnInitListener) this);

		/*
		 * The listener for the TTS events varies depending on the Android version used:
		 * the most updated one is UtteranceProgressListener, but in SKD versions
		 * 15 or earlier, it is necessary to use the deprecated OnUtteranceCompletedListener
		 */

        if (Build.VERSION.SDK_INT >= 15)
        {
            myTTS.setOnUtteranceProgressListener(new UtteranceProgressListener()
            {
                @Override
                public void onDone(String utteranceId) //TTS finished synthesizing
                {
                    onTTSDone(utteranceId);
                }

                @Override
                public void onError(String utteranceId) //TTS encountered an error while synthesizing
                {
                    onTTSError(utteranceId);
                }

                @Override
                public void onStart(String utteranceId) //TTS has started synthesizing
                {
                    onTTSStart(utteranceId);
                }
            });
        }
        else
        {
            myTTS.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener()
            {
                @Override
                public void onUtteranceCompleted(final String utteranceId)
                {
                    onTTSDone(utteranceId);			//Earlier SDKs only consider the onTTSDone event
                }
            });
        }
    }

    public abstract void onTTSDone(String uttId);
    public abstract void onTTSError(String uttId);
    public abstract void onTTSStart(String uttId);

    public void setLocale(String languageCode, String countryCode) throws Exception{
        if(languageCode==null)
        {
            setLocale();
            throw new Exception("Language code was not provided, using default locale");
        }
        else{
            if(countryCode==null)
                setLocale(languageCode);
            else {
                Locale lang = new Locale(languageCode, countryCode);
                if (myTTS.isLanguageAvailable(lang) == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE )
                    myTTS.setLanguage(lang);
                else
                {
                    setLocale();
                    throw new Exception("Language or country code not supported, using default locale");
                }
            }
        }
    }

    public void setLocale(String languageCode) throws Exception{
        if(languageCode==null)
        {
            setLocale();
            throw new Exception("Language code was not provided, using default locale");
        }
        else {
            Locale lang = new Locale(languageCode);
            if (myTTS.isLanguageAvailable(lang) != TextToSpeech.LANG_MISSING_DATA && myTTS.isLanguageAvailable(lang) != TextToSpeech.LANG_NOT_SUPPORTED)
                myTTS.setLanguage(lang);
            else
            {
                setLocale();
                throw new Exception("Language code not supported, using default locale");
            }
        }
    }

    public void setLocale(){
        myTTS.setLanguage(Locale.getDefault());
    }

    public void speak(String text, String languageCode, String countryCode, Integer id) throws Exception{
        setLocale(languageCode, countryCode);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id.toString());
        myTTS.speak(text, TextToSpeech.QUEUE_ADD, params);
    }

    public void speak(String text, String languageCode, Integer id) throws Exception{
        setLocale(languageCode);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id.toString());
        myTTS.speak(text, TextToSpeech.QUEUE_ADD, params);
    }

    public void speak(String text, Integer id){
        setLocale();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id.toString());
        myTTS.speak(text, TextToSpeech.QUEUE_ADD, params);
    }

    public void stop(){
        if(myTTS.isSpeaking())
            myTTS.stop();
    }

    public void shutdown(){
        myTTS.stop();
        myTTS.shutdown();
        myTTS=null;			/*
		 						This is necessary in order to force the creation of a new TTS instance after shutdown.
		 						It is useful for handling runtime changes such as a change in the orientation of the device,
		 						as it is necessary to create a new instance with the new context.
		 						See here: http://developer.android.com/guide/topics/resources/runtime-changes.html
							*/
        myASR.stopListening();
        myASR.destroy();
        myASR=null;
    }

    @Override
    public void onInit(int status) {
        if(status != TextToSpeech.ERROR){
            setLocale();
        }
        else
        {
            Log.e(LOGTAG, "Error creating the TTS");
        }

    }
}
