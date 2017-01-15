/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tbay.android.tcpclient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.tbay.android.common.logger.Log;
import com.tbay.android.common.logger.LogFragment;
import com.tbay.android.common.logger.LogWrapper;
import com.tbay.android.common.logger.MessageOnlyLogFilter;

import java.io.IOException;
import java.net.InetAddress;


/**
 * Sample application demonstrating how to test whether a device is connected,
 * and if so, whether the connection happens to be wifi or mobile (it could be
 * something else).
 *
 * This sample uses the logging framework to display log output in the log
 * fragment (LogFragment).
 */
public class MainActivity extends FragmentActivity {

    public static final String TAG = "TCP Client";
    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;

    // Reference to the fragment showing events, so we can clear it with a button
    // as necessary.
    private LogFragment mLogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_main);

        // Initialize text fragment that displays intro text.
        SimpleTextFragment introFragment = (SimpleTextFragment)
                    getSupportFragmentManager().findFragmentById(R.id.intro_fragment);
        introFragment.setText(R.string.intro_message);
        //introFragment.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);

        // Initialize the logging framework.
        initializeLogging();

        // Initialize the source IP string

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);

        try {
            int ip = wm.getConnectionInfo().getIpAddress();

            TextView SrcIp = (TextView)findViewById(R.id.textView6);

            String ip2 = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
            SrcIp.setText(ip2.toCharArray(),0,ip2.length());
        }
        catch(Exception e)
        {
            String S = e.toString();  // Just for debug
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // When the user clicks TEST, display the connection status.
            case R.id.test_action:
                checkNetworkConnection();
                return true;
            // Clear the log view fragment.
            case R.id.clear_action:
                mLogFragment.getLogView().setText("");
                return true;
            case R.id.stop_action:
                stopSending();
                return true;
            case R.id.start_action:
                startSending();
                return true;
        }
        return false;
    }

    /**
     * Stop sending data to the peer.
     */
    private void stopSending() {

        Log.i(TAG, "Sending stopped.");
        //mLogFragment.getLogView().appendToLog("stopped");
    }

    /**
     * Start sending data to the peer.
     */
    private void startSending() {

        EditText Ip = (EditText)findViewById(R.id.DestinationIP);
        EditText Port = (EditText)findViewById(R.id.PortNumber);

        EditText Txt = (EditText)findViewById(R.id.textView);

        EditText Delay = (EditText)findViewById(R.id.delay);
        EditText NumTx = (EditText)findViewById(R.id.number_of_tx);

        String[] args = new String[4];
        args[0] = Ip.getText().toString() + ':' +  Port.getText().toString();
        args[1] = Txt.getText().toString();
        args[2] = Delay.getText().toString();
        args[3] = NumTx.getText().toString();

        String IPStr = "Sending started at: ";

        IPStr += Ip.getText();

        Log.i(TAG, IPStr);

        new BackgroundActivity().execute(args);
    }

    /**
     * Check whether the device is connected, and if so, whether the connection
     * is wifi or mobile (it could be something else).
     */
    private void checkNetworkConnection() {
      // BEGIN_INCLUDE(connect)
      ConnectivityManager connMgr =
          (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
      if (activeInfo != null && activeInfo.isConnected()) {
          wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
          mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
          if(wifiConnected) {
              Log.i(TAG, getString(R.string.wifi_connection));

              EditText Ip = (EditText)findViewById(R.id.DestinationIP);

              String ged = Ip.getText().toString();

              try {
                  InetAddress address = InetAddress.getByName(Ip.getText().toString());
                  if (address.isReachable(2000))
                      Log.i(TAG, Ip.toString() + " is reachable");
                  else
                      Log.i(TAG, Ip.toString() + " is not reachable");
              }
              catch(IOException e) {
                  Log.i(TAG, e.getMessage());
              }
              catch(Exception e) {
                  Log.i(TAG, "Exception: " + e.getMessage());
              }


          } else if (mobileConnected){
              Log.i(TAG, getString(R.string.mobile_connection));
          }
      } else {
          Log.i(TAG, getString(R.string.no_wifi_or_mobile));
      }
      // END_INCLUDE(connect)
    }

    /** Create a chain of targets that will receive log data */
    public void initializeLogging() {

        // Using Log, front-end to the logging chain, emulates
        // android.util.log method signatures.

        // Wraps Android's native log framework
        LogWrapper logWrapper = new LogWrapper();
        Log.setLogNode(logWrapper);

        // A filter that strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        mLogFragment =
                (LogFragment) getSupportFragmentManager().findFragmentById(R.id.log_fragment);
        msgFilter.setNext(mLogFragment.getLogView());
    }
}
