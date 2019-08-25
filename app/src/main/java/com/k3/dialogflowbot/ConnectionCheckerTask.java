package com.k3.dialogflowbot;

import android.os.AsyncTask;

import java.net.InetAddress;

public class ConnectionCheckerTask extends AsyncTask<Void, Void, Boolean> {
    private String connectionAddress;
    public interface ConnectionCheckerInterface {
        void onCheck(boolean connected);
    }
    private ConnectionCheckerInterface connectionCheckerInterface;
    ConnectionCheckerTask(String connectionAddress,
                                  ConnectionCheckerInterface connectionCheckerInterface){
        this.connectionAddress = connectionAddress;
        this.connectionCheckerInterface = connectionCheckerInterface;
    }
    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            InetAddress address = InetAddress.getByName(connectionAddress);
            return !address.getHostAddress().equals("");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean connected) {
        connectionCheckerInterface.onCheck(connected);
    }
}
