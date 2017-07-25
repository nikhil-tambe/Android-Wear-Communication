package com.nikhil.wear.comm;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.ChannelApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;

import static com.nikhil.wear.comm.WearDataLayerListenerService.TAG;

/**
 * Created by Nikhil on 21/7/17.
 */

public class WearChannelAsync extends AsyncTask<String, Integer, String> {

    private Context context;
    private Uri uri;
    private String channelName;
    private GoogleApiClient googleApiClient;

    public WearChannelAsync(Context context, Uri uri, String channelName) {
        this.context = context;
        this.uri = uri;
        this.channelName = channelName;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        Collection<String> nodes = getNodes();
        Log.d(TAG, "doInBackground: called");
        for (String node : nodes) {
            ChannelApi.OpenChannelResult result = Wearable.ChannelApi
                    .openChannel(googleApiClient, node, channelName).await();
            result.getChannel()
                    .sendFile(googleApiClient, uri).setResultCallback(new ResultCallback<com.google.android.gms.common.api.Status>() {
                @Override
                public void onResult(@NonNull com.google.android.gms.common.api.Status status) {
                    Log.d(TAG, "onResult: " + status.getStatus() + ": " + status.toString());
                }
            });
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

        for (Node node : nodes.getNodes()) {
            Log.d(TAG, "getNodes: " + node.getId()
                    + ": " + node.getDisplayName() + ": " + node.isNearby());
            results.add(node.getId());
        }

        return results;
    }

}
