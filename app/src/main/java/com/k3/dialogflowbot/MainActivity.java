package com.k3.dialogflowbot;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.google.cloud.dialogflow.v2beta1.TextInput;

import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends Activity {

    private SessionsClient sessionsClient;
    private SessionName session;
    private QueryInput queryInput;
    private String languageCode = "tr";
    private interface ResponseInterface {
        void onResponse(DetectIntentResponse response);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!initDialogflow()) {
            Toast.makeText(this, "Failed to initialize Dialogflow.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        String testQuery = "test";
        queryInput = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(testQuery)
                .setLanguageCode(languageCode)).build();
        new RequestTask(session, sessionsClient, queryInput, new ResponseInterface() {
            @Override
            public void onResponse(DetectIntentResponse response) {
                //
            }
        }).execute();
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

}
