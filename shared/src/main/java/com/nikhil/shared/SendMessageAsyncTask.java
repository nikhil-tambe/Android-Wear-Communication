package com.nikhil.shared;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Nikhil on 25/7/17.
 */

public class SendMessageAsyncTask extends AsyncTask<String, Void, Void> {

    public static final String TAG = "nikhil " + SendMessageAsyncTask.class.getSimpleName();
    Context context;
    GoogleApiClient googleApiClient;
    String path;

    public SendMessageAsyncTask(Context context, String path){
        this.context = context;
        this.path = path;
        googleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API).build();
        googleApiClient.connect();
    }

    @Override
    protected Void doInBackground(String... args) {
        Collection<String> nodes = getNodes();
        String message = args[0];
        for (String node : nodes) {
            sendStartActivityMessage(node, message);
        }
        return null;
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

        for (Node node : nodes.getNodes()) {
            //Log.d(TAG, "getNodes: " + node.getId() + ": " + node.getDisplayName() + ": " + node.isNearby());
            results.add(node.getId());
        }

        return results;
    }

    private void sendStartActivityMessage(final String node, final String message) {

        byte[] messageArray = message.getBytes(); //Charset.forName("UTF-8"));

        Wearable.MessageApi
                .sendMessage(googleApiClient, node, path, messageArray)
                .setResultCallback(
                        new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                if (!sendMessageResult.getStatus().isSuccess()) {
                                    Log.e(TAG, "Failed to send message with status code: "
                                            + sendMessageResult.getStatus().getStatusCode());
                                } else {
                                    Log.d(TAG, "onResult: message: \'" + message + "\' sent to " + node);
                                }
                            }
                        }
                );
    }

}
