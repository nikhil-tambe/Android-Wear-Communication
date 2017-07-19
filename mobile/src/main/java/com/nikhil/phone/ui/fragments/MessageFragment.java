package com.nikhil.phone.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nikhil.phone.R;

import butterknife.BindView;

/**
 * Created by Nikhil on 17/7/17.
 */

public class MessageFragment extends Fragment {

    public MessageFragment(){

    }

    public static MessageFragment newInstance(String arg){
        MessageFragment fragment = new MessageFragment();
        Bundle bundle = new Bundle();
        bundle.putString("arg1", arg);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_message, container, false);

        //

        return rootView;
    }


}
