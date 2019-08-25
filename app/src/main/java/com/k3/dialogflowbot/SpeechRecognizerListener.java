package com.k3.dialogflowbot;

import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.widget.ProgressBar;

public class SpeechRecognizerListener implements android.speech.RecognitionListener {

    private SpeechRecognizer speechRecognizer;
    private ProgressBar progressBarRms;
    public interface RecognizerInterface {
        void onResult(String result);
        void onError(int errorCode);
    }
    private RecognizerInterface recognizerInterface;
    SpeechRecognizerListener(SpeechRecognizer speechRecognizer,
                                     ProgressBar progressBarRms,
                                     RecognizerInterface recognizerInterface) {
        this.speechRecognizer = speechRecognizer;
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

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResults(Bundle bundle) {
        try {
            recognizerInterface.onResult(bundle
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));
        }catch (NullPointerException e){
            e.printStackTrace();
            recognizerInterface.onResult(null);
        }
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
    
    public static String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service is busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
        }
        return null;
    }
}