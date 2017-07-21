package com.nikhil.wear.comm;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Channel;
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

public class WearChannelAsync extends AsyncTask<String, Integer, String>
        implements GoogleApiClient.ConnectionCallbacks {

    private Context context;
    private Uri uri;
    private String channelName;
    private GoogleApiClient googleApiClient;

    public WearChannelAsync(Context context, Uri uri, String channelName) {
        this.context = context;
        this.uri = uri;
        this.channelName = channelName;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();

        Collection<String> nodes = getNodes();
        for (String node : nodes) {
            ChannelApi.OpenChannelResult result = Wearable.ChannelApi
                    .openChannel(googleApiClient, node, channelName).await();
            Channel channel = result.getChannel();
            channel.sendFile(googleApiClient, uri).await();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

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
