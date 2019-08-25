package com.k3.dialogflowbot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;
import com.google.cloud.dialogflow.v2beta1.SessionsSettings;

import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {

    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private SessionsClient sessionsClient;
    private SessionName session;
    private QueryInput queryInput;
    private String languageCode;
    private TextView txvResult;
    private ProgressBar pgbRms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        new ConnectionCheckerTask(getString(R.string.connection_address),
                new ConnectionCheckerTask.ConnectionCheckerInterface() {
            @Override
            public void onCheck(boolean connected) {
                if (!connected) {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.connection_error),
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else if (!initDialogflow()) {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.dialogflow_init_error),
                                Toast.LENGTH_SHORT).show();
                        finish();
                }
            }
        }).execute();
        initSpeechRecognizer();
        speechRecognizer.startListening(recognizerIntent);
        /*String testQuery = "test";
        queryInput = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(testQuery)
                .setLanguageCode(languageCode)).build();
        new RequestTask(session, sessionsClient, queryInput, new ResponseInterface() {
            @Override
            public void onResponse(DetectIntentResponse response) {
                String fulfillmentText = response.getQueryResult().getFulfillmentText();
                speakText(getApplicationContext(), languageCode, fulfillmentText);
                Toast.makeText(getApplicationContext(), fulfillmentText, Toast.LENGTH_LONG).show();
            }
        }).execute();*/
    }

    private void initViews() {
        txvResult = findViewById(R.id.txvResult);
        pgbRms =  findViewById(R.id.pgbRms);
        pgbRms.setIndeterminate(true);
        languageCode = getString(R.string.language_code);
        try {
            getActionBar().hide();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void initSpeechRecognizer() {
        resetSpeechRecognizer();
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,  languageCode);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
    }

    private boolean initDialogflow() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.dialogflow_credentials);
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(inputStream);
            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.
                    setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials)).
                    build();
            sessionsClient = SessionsClient.create(sessionsSettings);
            session = SessionName.of(((ServiceAccountCredentials) googleCredentials).getProjectId(),
                    UUID.randomUUID().toString());
            return true;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void resetSpeechRecognizer() {
        if(speechRecognizer != null)
            speechRecognizer.destroy();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        if(!SpeechRecognizer.isRecognitionAvailable(this))
            finish();
        speechRecognizer.setRecognitionListener(new SpeechRecognizerListener(speechRecognizer,
                recognizerIntent, pgbRms, new SpeechRecognizerListener.RecognizerInterface() {
            @Override
            public void onResult(String result) {
                txvResult.setText(result);
            }
            @Override
            public void onError(int errorCode) {
                txvResult.setText(getString(R.string.error_code, errorCode));
                resetSpeechRecognizer();
                speechRecognizer.startListening(recognizerIntent);
            }
        }));
    }

    public void speakText(final Context context, final String languageCode, final String text){
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    int setLanguage= textToSpeech.setLanguage(new Locale(languageCode,
                            languageCode.toUpperCase()));
                    // TODO: Update voice parameters.
                    if (setLanguage == TextToSpeech.LANG_MISSING_DATA ||
                            setLanguage == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(context, context.getString(R.string.texttospeech_init_error),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(speechRecognizer != null) {
            resetSpeechRecognizer();
            speechRecognizer.startListening(recognizerIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(speechRecognizer != null)
            speechRecognizer.stopListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (speechRecognizer != null)
            speechRecognizer.destroy();
    }
}
