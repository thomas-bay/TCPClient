package com.tbay.android.tcpclient;

import android.os.AsyncTask;
import android.net.LocalSocket;
import com.tbay.android.common.logger.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.*;


/**
 * Created by Thomas on 12-Apr-15.
 */
public class BackgroundActivity extends AsyncTask<String, Void, Void>{

    public static final String TAG = "TCP Client";

//    @Override
    protected Void doInBackground(String... ip) {

        int j;
        String PortNum, IpAdr;
        byte[] InBuf = new byte[1000];

        //create a socket to make the connection with the server
        try {
            j = ip[0].indexOf(':');
            if (j > 0) {
                PortNum = ip[0].substring(j + 1);
                IpAdr = ip[0].substring(0,j);
            }
            else {
                PortNum = String.valueOf(35000);
                IpAdr = ip[0];
            }

            Socket socket = new Socket(IpAdr, Integer.parseInt(PortNum));
            Log.i(TAG, "Connection opened to " + ip[0]);

            OutputStream Out = socket.getOutputStream();
            InputStream In = socket.getInputStream();

            for (int i = 0; i < 10; i++) {
                Log.i(TAG, "Tx: " + ip[1]);

                Out.write(ip[1].getBytes(), 0, ip[1].length());
                In.read(InBuf);
                Log.i(TAG, "Rx: " + new String(InBuf));
                Thread.sleep(5);
            }
            socket.close();
        }
        catch (IOException e)
        {
            Log.i(TAG, "Error! Cannot connect to remote");
        }
        catch (InterruptedException e) {
            Log.i(TAG, "Error!");
        }

        return null;
    }

}
