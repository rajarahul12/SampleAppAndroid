package com.demo.push.d;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.worklight.wlclient.api.*;
import com.worklight.wlclient.auth.AccessToken;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushException;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPSimplePushNotification;
import com.worklight.ibmmobilefirstplatformfoundationliveupdate.LiveUpdateManager;
import com.worklight.ibmmobilefirstplatformfoundationliveupdate.api.Configuration;
import com.worklight.ibmmobilefirstplatformfoundationliveupdate.api.ConfigurationListener;
import com.worklight.common.WLAnalytics;
import com.worklight.common.WLAnalytics.DeviceEvent;
import com.worklight.common.Logger;
import com.worklight.common.Logger.LEVEL;

public class MainApplication extends AppCompatActivity {

    private MFPPush push;
    private WLClient client;    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        client = WLClient.createInstance(this);

        mfConnect();
        mfPushInit();
        mfLiveUpdateInit();
        mfAnalyticsInit();
    }
    private void mfAnalyticsInit() {
        WLAnalytics.init(this.getApplication());
        WLAnalytics.addDeviceEventListener(DeviceEvent.LIFECYCLE);
        WLAnalytics.addDeviceEventListener(DeviceEvent.NETWORK);
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("customDataKey", "customDataValue");
            WLAnalytics.log("App started successfully...", json);
            WLAnalytics.send();                               
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        Logger logger = Logger.getInstance("SampleAppAndroid");
        logger.setLevel(LEVEL.DEBUG);
        logger.debug("App started successfully...");
        logger.send();
    }

    private void mfLiveUpdateInit() {
        LiveUpdateManager.getInstance(this).obtainConfiguration(false, new LiveUpdateListener());
    }

    private void mfPushInit() {
        push = MFPPush.getInstance();
        push.initialize(this);
        push.registerDevice(null, new PushListener());
        push.listen(new PushListener());
    }  

    @Override
    public void onPause() {
        super.onPause();
        if (push != null) {
            push.hold();
        }
    }  

    @Override
    public void onResume() {
        super.onResume();
        if (push != null) {
            push.listen(new PushListener());
        }
    }

    
    private void mfConnect() {
        WLAuthorizationManager.getInstance().obtainAccessToken(null, new ConnectListener());
    }

    /* Sample placeholder listeners.  Replace with your own listeners and handling logic */
    class BaseListener {
        private android.widget.TextView connectionStatusLabel;

        protected void changeStatusMessage(final String text) {
            if (connectionStatusLabel == null) {
                connectionStatusLabel = (android.widget.TextView) findViewById(R.id.connection_status_id);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectionStatusLabel.setText(text);
                }
            });
        }
    }

    class ConnectListener extends BaseListener implements WLAccessTokenListener {
        @Override
        public void onSuccess(AccessToken token) {
            System.out.println("Received the following access token value: " + token);
            changeStatusMessage("Connected to MobileFirst Server");
        }
        @Override
        public void onFailure(WLFailResponse wlFailResponse) {
            System.out.println("Did not receive an access token from server: " + wlFailResponse.getErrorMsg());
            changeStatusMessage("Failed to connect to MobileFirst Server");
        } 
    }

    class PushListener extends BaseListener implements MFPPushResponseListener<String>, MFPPushNotificationListener {
        @Override
        public void onSuccess(String s) {
            changeStatusMessage("Device successfully registered for push");
        }

        @Override
        public void onSuccess(org.json.JSONObject jobj) {
            changeStatusMessage("Device successfully registered for push");
        }

        @Override
        public void onFailure(MFPPushException e) {
            changeStatusMessage("Failed to register the device for push");
        }

        @Override
        public void onReceive(MFPSimplePushNotification mfpSimplePushNotification) {
            changeStatusMessage("Received notification : " + mfpSimplePushNotification.getAlert());
        }
    }
   
    class LiveUpdateListener extends BaseListener implements ConfigurationListener {
        @Override
        public void onSuccess(final Configuration configuration) {
            try {                                                        
                org.json.JSONObject luObject = new org.json.JSONObject();
                luObject.put("enableButton", configuration.isFeatureEnabled("enableButton"));
                luObject.put("titleColour", configuration.getProperty("titleColour"));
                changeStatusMessage("Live update schema:" + luObject.toString(2));                                                        
            } catch (org.json.JSONException e) {                                                        
                changeStatusMessage("Failed to obtain live update configuration");                                                        
            }  
        }

        @Override
        public void onFailure(WLFailResponse wlFailResponse) {
            changeStatusMessage("Failed to obtain live update configuration");                                   
        }
    }
}