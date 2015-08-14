package com.example.guest.wigl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.widget.TextView;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private static Logger logger = Logger.getLogger(WiFiDirectBroadcastReceiver.class.getSimpleName());
    private TextView text;

    private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       MainActivity activity) {
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        logger.info("Got a broadcast");
        String action = intent.getAction();
        logger.info("Intent action: " + action);

        final TextView text = (TextView) mActivity.findViewById(R.id.peer_list);

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                logAndSetText("Wifi P2P is enabled");
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        logAndSetText("Successfully discovered peers");
                        requestPeers();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        logAndSetText("Failed discovered peers");
                    }
                });
            } else {
                logAndSetText("Wifi P2P is disabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            logger.info("Peers changed; about to call requestPeers()");
            requestPeers();
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            logger.info("Can respond to new connecetion or disconnections");
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            logger.info("This device's wifi state changed");
        }
    }

    private void requestPeers() {
        PeerListListener myPeerListListener = new PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                try {
                    logger.info("About to display peers");
                    displayPeers(peers, text);
                } catch (InterruptedException e) {
                    logAndSetText(e.getLocalizedMessage());
                }

                Iterator<WifiP2pDevice> iter = peers.getDeviceList().iterator();
//                connectToPeer(iter.next(), text);
            }
        };
        if (mManager != null) {
            logger.info("requesting peers");
            mManager.requestPeers(mChannel, myPeerListListener);
        }
    }

    private void displayPeers(WifiP2pDeviceList peers, TextView text) throws InterruptedException {
        for (WifiP2pDevice wifiP2pDevice : peers.getDeviceList()) {
            logAndSetText(wifiP2pDevice.toString());
            Thread.sleep(1000l);
        }
    }

    private void connectToPeer(final WifiP2pDevice device, final TextView text) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                logAndSetText("Successfully connected to " + device.toString());
            }

            @Override
            public void onFailure(int reason) {
                logAndSetText("Failed connected to " + device.toString());
            }
        });
    }

    private void logAndSetText(String message) {
        logger.info(message);
//        text.setText(message);
    }
}
