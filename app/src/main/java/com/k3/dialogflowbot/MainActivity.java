package com.k3.dialogflowbot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;
import com.google.cloud.dialogflow.v2beta1.SessionsSettings;
import com.google.cloud.dialogflow.v2beta1.TextInput;

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
        initTTS();
        initSpeechRecognizer();
        speechRecognizer.startListening(recognizerIntent);
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

    public void initTTS(){
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    int setLanguage= textToSpeech.setLanguage(new Locale(languageCode,
                            languageCode.toUpperCase()));
                    // TODO: Update voice parameters.
                    if (setLanguage == TextToSpeech.LANG_MISSING_DATA ||
                            setLanguage == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.texttospeech_init_error),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String s) {
                            if(speechRecognizer != null)
                                speechRecognizer.stopListening();
                        }

                        @Override
                        public void onDone(String s) {
                            if (speechRecognizer != null)
                                speechRecognizer.startListening(recognizerIntent);
                        }

                        @Override
                        public void onError(String s) {
                            txvResult.setText(s);
                            resetSpeechRecognizer();
                            speechRecognizer.startListening(recognizerIntent);
                        }
                    });
                }
            }
        });
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
                pgbRms, new SpeechRecognizerListener.RecognizerInterface() {
            @Override
            public void onResult(String result) {
                if (result != null) {
                    txvResult.setText(result);
                    queryInput = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(result)
                            .setLanguageCode(languageCode)).build();
                    new DialogflowRequestTask(session, sessionsClient, queryInput, new DialogflowRequestTask.ResponseInterface() {
                        @Override
                        public void onResponse(DetectIntentResponse response) {
                            String fulfillmentText = response.getQueryResult().getFulfillmentText();
                            Toast.makeText(getApplicationContext(), fulfillmentText, Toast.LENGTH_LONG).show();
                            textToSpeech.speak(fulfillmentText, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }).execute();
                } else {
                    speechRecognizer.startListening(recognizerIntent);
                }
            }
            @Override
            public void onError(int errorCode) {
                if (SpeechRecognizerListener.getErrorMessage(errorCode) != null) {
                    txvResult.setText(SpeechRecognizerListener.getErrorMessage(errorCode));
                } else {
                    txvResult.setText(getString(R.string.error_code, errorCode));
                }
                resetSpeechRecognizer();
                speechRecognizer.startListening(recognizerIntent);
            }
        }));
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
