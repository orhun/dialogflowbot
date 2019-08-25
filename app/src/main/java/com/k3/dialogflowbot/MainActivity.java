package com.k3.dialogflowbot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.google.cloud.dialogflow.v2beta1.DetectIntentRequest;
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;
import com.google.cloud.dialogflow.v2beta1.SessionsSettings;

import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {

    private TextToSpeech textToSpeech;
    private SessionsClient sessionsClient;
    private SessionName session;
    private QueryInput queryInput;
    private String languageCode = "tr";
    private interface ResponseInterface {
        void onResponse(DetectIntentResponse response);
    }


    private TextView returnedText;
    private ProgressBar pgbRms;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        if (!initDialogflow()) {
            Toast.makeText(this, getString(R.string.dialogflow_init_error),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
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
        resetSpeechRecognizer();
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,  languageCode);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        speechRecognizer.startListening(recognizerIntent);
    }

    private void initViews() {
        returnedText = findViewById(R.id.textView1);
        pgbRms =  findViewById(R.id.pgbRms);
        pgbRms.setIndeterminate(true);
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
                returnedText.setText(result);
            }
            @Override
            public void onError(int errorCode) {
                returnedText.setText(getString(R.string.error_code, errorCode));
                resetSpeechRecognizer();
                speechRecognizer.startListening(recognizerIntent);
            }
        }));
    }

    @Override
    public void onResume() {
        super.onResume();
        resetSpeechRecognizer();
        speechRecognizer.startListening(recognizerIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        speechRecognizer.stopListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    private static class RequestTask extends AsyncTask<Void, Void, DetectIntentResponse> {
        private SessionName session;
        private SessionsClient sessionsClient;
        private QueryInput queryInput;
        private ResponseInterface responseInterface;
        private RequestTask(SessionName session, SessionsClient sessionsClient,
                            QueryInput queryInput, ResponseInterface responseInterface) {
            this.session = session;
            this.sessionsClient = sessionsClient;
            this.queryInput = queryInput;
            this.responseInterface = responseInterface;
        }
        @Override
        protected DetectIntentResponse doInBackground(Void... voids) {
            try {
                DetectIntentRequest detectIntentRequest =
                        DetectIntentRequest.newBuilder()
                                .setSession(session.toString())
                                .setQueryInput(queryInput)
                                .build();
                return sessionsClient.detectIntent(detectIntentRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(DetectIntentResponse response) {
            responseInterface.onResponse(response);
        }
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
}
