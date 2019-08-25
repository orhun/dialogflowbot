package com.k3.dialogflowbot;

import android.content.Intent;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.widget.ProgressBar;

public class SpeechRecognizerListener implements android.speech.RecognitionListener {

    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private ProgressBar progressBarRms;
    interface RecognizerInterface {
        void onResult(String result);
        void onError(int errorCode);
    }
    private RecognizerInterface recognizerInterface;
    SpeechRecognizerListener(SpeechRecognizer speechRecognizer,
                                     Intent recognizerIntent,
                                     ProgressBar progressBarRms,
                                     RecognizerInterface recognizerInterface) {
        this.speechRecognizer = speechRecognizer;
        this.recognizerIntent = recognizerIntent;
        this.progressBarRms = progressBarRms;
        this.recognizerInterface = recognizerInterface;

    }
    @Override
    public void onBeginningOfSpeech() {
        progressBarRms.setIndeterminate(false);
        progressBarRms.setMax(10);
    }

    @Override
    public void onRmsChanged(float v) {
        progressBarRms.setProgress((int) v);
    }

    @Override
    public void onEndOfSpeech() {
        progressBarRms.setIndeterminate(true);
        speechRecognizer.stopListening();
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onResults(Bundle bundle) {
        try {
            recognizerInterface.onResult(bundle
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        speechRecognizer.startListening(recognizerIntent);
    }

    @Override
    public void onEvent(int i, Bundle bundle) { }

    @Override
    public void onBufferReceived(byte[] bytes) { }

    @Override
    public void onPartialResults(Bundle bundle) { }

    @Override
    public void onError(int i) {
        recognizerInterface.onError(i);
    }
}