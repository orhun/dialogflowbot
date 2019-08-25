package com.k3.dialogflowbot;

import android.os.AsyncTask;

import com.google.cloud.dialogflow.v2beta1.DetectIntentRequest;
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;

public class DialogflowRequestTask extends AsyncTask<Void, Void, DetectIntentResponse> {
    private SessionName session;
    private SessionsClient sessionsClient;
    private QueryInput queryInput;
    private interface ResponseInterface {
        void onResponse(DetectIntentResponse response);
    }

    private ResponseInterface responseInterface;
    private DialogflowRequestTask(SessionName session, SessionsClient sessionsClient,
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