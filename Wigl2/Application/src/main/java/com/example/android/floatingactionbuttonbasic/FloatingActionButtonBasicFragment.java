/*
 * Copyright 2014, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.floatingactionbuttonbasic;

import com.example.android.common.logger.Log;

import android.app.Activity;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;


/**
 * This fragment inflates a layout with two Floating Action Buttons and acts as a listener to
 * changes on them.
 */
public class FloatingActionButtonBasicFragment extends Fragment implements FloatingActionButton.OnCheckedChangeListener{

    private final static String TAG = "FloatingActionButtonBasicFragment";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private boolean groupCreated = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fab_layout, container, false);

        // Make this {@link Fragment} listen for changes in both FABs.
        FloatingActionButton fab1 = (FloatingActionButton) rootView.findViewById(R.id.fab_1);
        fab1.setOnCheckedChangeListener(this);
        FloatingActionButton fab2 = (FloatingActionButton) rootView.findViewById(R.id.fab_2);
        fab2.setOnCheckedChangeListener(this);
        return rootView;
    }


    @Override
    public void onCheckedChanged(FloatingActionButton fabView, boolean isChecked) {
        // When a FAB is toggled, log the action.
        switch (fabView.getId()){
            case R.id.fab_1:
                Log.d(TAG, String.format("FAB 1 was %s.", isChecked ? "checked" : "unchecked"));
                if (groupCreated) {
                    mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "removed group");
                            groupCreated = false;
                        }

                        @Override
                        public void onFailure(int i) {
                            Log.d(TAG, "removing group failed: " + i);

                        }
                    });
                } else {
                    mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "created group");
                            groupCreated = true;
                        }

                        @Override
                        public void onFailure(int i) {
                            Log.d(TAG, "creating group failed: " + i);
                            if (i == 2)
                                groupCreated = true;
                        }
                    });
                }
                break;
            case R.id.fab_2:
                Log.d(TAG, String.format("FAB 2 was %s.", isChecked ? "checked" : "unchecked"));
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "discovering peers");
                        mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                            @Override
                            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                                Collection<WifiP2pDevice> devices = wifiP2pDeviceList.getDeviceList();
                                Log.d(TAG, devices.size() + " discovered peers");
                                for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                                    Log.d(TAG, device.toString());
                                    Log.d(TAG, device.deviceName);
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.d(TAG, "failed to discover peers: " + i);
                    }
                });
                break;
            default:
                break;
        }
    }

    public void useWifiManager(WifiP2pManager mManager) {
        this.mManager = mManager;
    }

    public void useChannel(WifiP2pManager.Channel mChannel) {
        this.mChannel = mChannel;
    }
}
