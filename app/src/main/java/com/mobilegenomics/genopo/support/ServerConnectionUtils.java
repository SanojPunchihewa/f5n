package com.mobilegenomics.genopo.support;

import android.util.Log;

import com.mobilegenomics.genopo.R;
import com.mobilegenomics.genopo.activity.MinITActivity;
import com.mobilegenomics.genopo.dto.State;
import com.mobilegenomics.genopo.dto.WrapperObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnectionUtils {

    private final static String TAG = "ServerConnectionUtils";

    private static String serverAddress;

    private static StringBuilder logMessage = new StringBuilder();

    private static WrapperObject receivedWrapperObject;

    public static void connectToServer(final State state, final MinITActivity context, final ServerCallback serverCallback) {
        final LogHandler handler = new LogHandler(context);
        clearLogMessage();
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Socket socket = new Socket(serverAddress.trim(), 6677);
                    ObjectOutputStream objectOutStream = new ObjectOutputStream(socket.getOutputStream());
                    WrapperObject initialMessage = new WrapperObject();
                    initialMessage.setState(state);
                    objectOutStream.writeObject(initialMessage);
                    objectOutStream.flush();
                    Log.d(TAG, connectionLog(ConnectionMessages.TO_SERVER, initialMessage.toString()));

                    ObjectInputStream objectInStream = new ObjectInputStream(socket.getInputStream());
                    WrapperObject receivedObjectMessage = (WrapperObject) objectInStream.readObject();
                    Log.d(TAG, connectionLog(ConnectionMessages.FROM_SERVER, receivedObjectMessage.toString()));

                    if (state == State.REQUEST) {
                        if (receivedObjectMessage.getState().equals(State.ACK)) {
                            //isConnected = true;
                            Log.d(TAG, connectionLog(ConnectionMessages.NONE,
                                    ConnectionMessages.CONN_SUCCESSS.getMessage()));
                            WrapperObject receivedJobMessage = (WrapperObject) objectInStream.readObject();
                            receivedWrapperObject = receivedJobMessage;
                            Log.d(TAG, connectionLog(ConnectionMessages.NONE,
                                    ConnectionMessages.JOB_RECV_SUCCESS.getMessage()));
                            Log.d(TAG, connectionLog(ConnectionMessages.FROM_SERVER,
                                    receivedJobMessage.toStringPretty()));
                            serverCallback.onSuccess(receivedJobMessage);
                        } else {
                            //isConnected = false;
                            Log.d(TAG, connectionLog(ConnectionMessages.NONE,
                                    ConnectionMessages.CONN_FAILED.getMessage()));
                            // TODO add serverCallback#onError
                        }
                    } else if (state == State.COMPLETED) {
                        if (receivedObjectMessage.getState().equals(State.ACK)) {
                            //isConnected = true;
                            Log.d(TAG, connectionLog(ConnectionMessages.NONE,
                                    ConnectionMessages.RECONN_SUCCESS.getMessage()));
                            Log.d(TAG, connectionLog(ConnectionMessages.NONE,
                                    ConnectionMessages.RESULT_SEND_SUCCESS.getMessage()));
                            receivedWrapperObject.setState(State.SUCCESS);
                            objectOutStream.writeObject(receivedWrapperObject);
                            Log.d(TAG, connectionLog(ConnectionMessages.TO_SERVER,
                                    receivedWrapperObject.toStringPretty()));
                            serverCallback.onSuccess(receivedWrapperObject);
                        } else {
                            //isConnected = false;
                            Log.d(TAG, connectionLog(ConnectionMessages.NONE,
                                    ConnectionMessages.RECONN_FAILED.getMessage()));
                            // TODO add serverCallback#onError
                        }
                    }
                    objectInStream.close();
                    objectOutStream.close();
                    socket.close();
                    MinITActivity.logHandler(handler);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static String connectionLog(ConnectionMessages connectionMessages, String logInstance) {
        String strInstance;
        if (connectionMessages == ConnectionMessages.TO_SERVER) {
            strInstance = "To server: " + logInstance;
            logMessage.append(strInstance);
        } else if (connectionMessages == ConnectionMessages.FROM_SERVER) {
            strInstance = "From server: " + logInstance;
            logMessage.append(strInstance);
        } else {
            strInstance = logInstance;
            logMessage.append(strInstance);
        }
        logMessage.append("\n");
        return strInstance;
    }

    public static void setServerAddress(String serverAddress) {
        ServerConnectionUtils.serverAddress = serverAddress;
        PreferenceUtil.setSharedPreferenceString(R.string.id_server_ip, serverAddress);
    }

    public static String getServerAddress() {
        if (serverAddress != null)
            return serverAddress;
        else
            return PreferenceUtil.getSharedPreferenceString(R.string.id_server_ip);
    }

    public static StringBuilder getLogMessage() {
        return logMessage;
    }

    public static void clearLogMessage() {
        logMessage.delete(0, logMessage.length());
    }

    public static void setResultToWrapperObject(String resultSummery) {
        receivedWrapperObject.setResultSummery(resultSummery);
    }

    enum ConnectionMessages {
        CONN_SUCCESSS("Connection to the server is successful"),
        CONN_FAILED("Connection to the server is failed. Try again"),
        RECONN_SUCCESS("Re-connection to the server is successful"),
        RECONN_FAILED("Re-connection to the server is failed. Try again"),
        JOB_RECV_SUCCESS("Job received successfully"),
        RESULT_SEND_SUCCESS("Result send successfully"),
        CONN_CLOSED("Server connection closed"),
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

    public static WrapperObject getWrapperObject() {
        return receivedWrapperObject;
    }


    public static void setWrapperObject(WrapperObject wrapperObject) {
        receivedWrapperObject = wrapperObject;
    }

}
