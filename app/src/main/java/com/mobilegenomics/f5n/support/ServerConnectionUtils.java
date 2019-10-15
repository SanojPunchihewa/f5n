package com.mobilegenomics.f5n.support;

import android.app.ProgressDialog;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mobilegenomics.f5n.activity.MinITActivity;
import com.mobilegenomics.f5n.dto.State;
import com.mobilegenomics.f5n.dto.WrapperObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnectionUtils {

    private final static String TAG = "ServerConnectionUtils";

    enum ConnectionMessages{
        CONN_SUCCESSS("Connection to the server is successful"),
        CONN_FAILED("Connection to the server is failed. Trying again..."),
        RECONN_SUCCESS("Re-connection to the server is successful"),
        RECONN_FAILED("Re-connection to the server is failed. Trying again..."),
        JOB_RECV_SUCCESS("Job received successfully"),
        TO_SERVER("To server"),
        FROM_SERVER("From server"),
        NONE("None");

        String message;

        ConnectionMessages(String msg) {
            message = msg;
        }

        public String getMessage() {
            return message;
        }
    }

    private static String serverAddress;
    private static StringBuilder logMessage = new StringBuilder();
    private static WrapperObject receivedWrapperObject;

    public static void connectToServer() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Socket socket = new Socket(serverAddress.trim(), 6677);
                    ObjectOutputStream objectOutStream = new ObjectOutputStream(socket.getOutputStream());
                    WrapperObject initialMessage = new WrapperObject();
                    initialMessage.setState(State.CONNECT);
                    objectOutStream.writeObject(initialMessage);
                    objectOutStream.flush();
                    Log.d(TAG, connectionLog(ConnectionMessages.TO_SERVER, initialMessage.toString()));

                    ObjectInputStream objectInStream = new ObjectInputStream(socket.getInputStream());
                    WrapperObject receivedObjectMessage = (WrapperObject) objectInStream.readObject();
                    Log.d(TAG, connectionLog(ConnectionMessages.FROM_SERVER, receivedObjectMessage.toString()));

                    if (receivedObjectMessage.getState().equals(State.ACK)) {
                        //isConnected = true;
                        Log.d(TAG,connectionLog(ConnectionMessages.NONE, ConnectionMessages.CONN_SUCCESSS.getMessage()));
                        WrapperObject receivedJobMessage = (WrapperObject) objectInStream.readObject();
                        receivedWrapperObject = receivedJobMessage;
                        Log.d(TAG,connectionLog(ConnectionMessages.NONE, ConnectionMessages.JOB_RECV_SUCCESS.getMessage()));
                        Log.d(TAG,connectionLog(ConnectionMessages.FROM_SERVER, receivedJobMessage.toStringPretty()));
                    } else {
                        //isConnected = false;
                        Log.d(TAG, connectionLog(ConnectionMessages.NONE, ConnectionMessages.CONN_FAILED.getMessage()));
                    }
                    objectInStream.close();
                    objectOutStream.close();
                    socket.close();

                    //process message
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    MinITActivity.logHandler(handler);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void sendResult() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Socket socket = new Socket(serverAddress.trim(), 6677);

                    ObjectOutputStream objectOutStream = new ObjectOutputStream(socket.getOutputStream());
                    WrapperObject initialMessage = new WrapperObject();
                    initialMessage.setState(State.SUCCESS);
                    objectOutStream.writeObject(initialMessage);
                    objectOutStream.flush();
                    Log.d(TAG, connectionLog(ConnectionMessages.TO_SERVER, initialMessage.toString()));

                    ObjectInputStream objectInStream = new ObjectInputStream(socket.getInputStream());
                    WrapperObject receivedObjectMessage = (WrapperObject) objectInStream.readObject();
                    Log.d(TAG,connectionLog(ConnectionMessages.FROM_SERVER, receivedObjectMessage.toString()));

                    if (receivedObjectMessage.getState().equals(State.ACK)) {
                        //isConnected = true;
                        Log.d(TAG,connectionLog(ConnectionMessages.NONE, ConnectionMessages.RECONN_SUCCESS.getMessage()));

                        receivedWrapperObject.setState(State.SUCCESS);
                        objectOutStream.writeObject(receivedWrapperObject);
                        Log.d(TAG,connectionLog(ConnectionMessages.TO_SERVER, receivedWrapperObject.toStringPretty()));
                    } else {
                        //isConnected = false;
                        Log.d(TAG,connectionLog(ConnectionMessages.NONE, ConnectionMessages.RECONN_FAILED.getMessage()));
                    }
                    MinITActivity.logHandler(handler);
                    objectInStream.close();
                    objectOutStream.close();
                    socket.close();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static String connectionLog(ConnectionMessages connectionMessages, String logInstance) {
        String strInstance;
        if(connectionMessages == ConnectionMessages.TO_SERVER) {
            strInstance = "To server: " + logInstance;
            logMessage.append("\n").append(strInstance);
        } else if(connectionMessages == ConnectionMessages.FROM_SERVER) {
            strInstance = "From server: " + logInstance;
            logMessage.append("\n").append(strInstance);
        } else {
            strInstance = logInstance;
            logMessage.append("\n").append(strInstance);
        }
        return strInstance;
    }

    public static void setServerAddress(String serverAddress) {
        ServerConnectionUtils.serverAddress = serverAddress;
    }

    public static StringBuilder getLogMessage() {
        return logMessage;
    }
}
