package com.rfsserver.rfstudiocontroller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Iterator;

/**
 * Created by Raveen on 10/10/2015.
 */
public class HelperClass {
    private static HelperClass uniqueInstance;
    private Context tempContext;

    public static final String LOG_TAG = "RFStudio-Controller";

    public SharedPreferences preferences;

    private HelperClass() {

    }

    public static HelperClass getInstance() {
        if(uniqueInstance==null) {
            uniqueInstance=new HelperClass();
        }
        return uniqueInstance;
    }

    public boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public void sendMessage(String message, Context context)
    {
        tempContext = context;
        SendMessage asyncMessage = new SendMessage(message);
        asyncMessage.execute();
    }

    private void messageResponse(String response) {
        if(response.startsWith("10001"))
        {
            Toast.makeText(tempContext, "Invalid Account Info", Toast.LENGTH_LONG).show();
        } else if(response.startsWith("10002"))
        {
            Toast.makeText(tempContext, "Invalid Message Format", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(tempContext, response,Toast.LENGTH_LONG).show();
        }
    }

    private class SendMessage extends AsyncTask<Void, Void, Void> {
        String accountDetails = preferences.getString("username", "")+":"+preferences.getString("password", "")+"@";
        String message = "";
        String response = "";

        SendMessage(String msg){
            message = msg;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Socket socket = null;
            try {
                socket = new Socket(preferences.getString("url", "192.168.1.100"), preferences.getInt("port", 27015));

                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                out.println(accountDetails+message);

                ByteArrayOutputStream byteArrayOutputStream =
                        new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                int bytesRead;
                InputStream inputStream = socket.getInputStream();
                while ((bytesRead = inputStream.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }finally{
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //textResponse.setText(response);
            //Log.d(LOG_TAG, response);
            messageResponse(response);
            super.onPostExecute(result);
        }

    }



}
