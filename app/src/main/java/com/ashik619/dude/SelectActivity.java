package com.ashik619.dude;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ashik619.dude.custom_views.IconTextView;
import com.ashik619.dude.init.DudeApplication;
import com.ashik619.dude.io.HttpServerBackend;
import com.ashik619.dude.io.RestAdapter;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;
import com.splunk.mint.Mint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;

public class SelectActivity extends AppCompatActivity {
    int SELECT_CONTACT_REQ = 1;
    @BindView(R.id.continueButton)
    RelativeLayout selectButton;
    @BindView(R.id.loadingLayout)
    RelativeLayout loadingLayout;
    @BindView(R.id.mainLayout)
    FrameLayout mainLayout;
    @BindView(R.id.reqMsg)
    IconTextView reqMsg;
    @BindView(R.id.requestedLayout)
    LinearLayout requestedLayout;
    @BindView(R.id.inviteMsg)
    IconTextView inviteMsg;
    @BindView(R.id.inviteButton)
    RelativeLayout inviteButton;
    @BindView(R.id.inviteLayout)
    LinearLayout inviteLayout;
    private String selectedContactNo = null;
    private String selectedContactName = null;
    private String playerId = null;
    private String friendName = null;
    private String myPhoneNum = null;

    private String userName = null;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.initAndStartSession(SelectActivity.this, "74083949");
        setContentView(R.layout.activity_select);
        ButterKnife.bind(this);
        DudeApplication application = (DudeApplication)getApplication();
        mTracker = application.getDefaultTracker();
        myPhoneNum = DudeApplication.getLocalPrefInstance().getNumber();
        userName = DudeApplication.getLocalPrefInstance().getName();
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectContact();
            }
        });
        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inviteFriend(userName);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("Home Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    void selectContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        startActivityForResult(intent, SELECT_CONTACT_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Uri uri = data.getData();

                        if (uri != null) {
                            Cursor c = null;
                            try {
                                c = getContentResolver().query(uri, new String[]{
                                                ContactsContract.CommonDataKinds.Phone.NUMBER,
                                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                                        null, null, null);

                                if (c != null && c.moveToFirst()) {
                                    selectedContactNo = c.getString(0);
                                    selectedContactName = c.getString(1);

                                }
                            } finally {
                                if (c != null) {
                                    c.close();
                                }
                            }
                        }
                    }
                    removeCountryCode();
                    getUserDetailApiCall();
                }
                break;

        }

    }

    void removeCountryCode() {
        selectedContactNo = selectedContactNo.replace(" ", "");
        selectedContactNo = selectedContactNo.replace("+91", "");
        selectedContactNo = selectedContactNo.replace("-", "");
    }

    void getUserDetailApiCall() {
        Call<JsonObject> call = new RestAdapter().getRestInterface().getuser(selectedContactNo);
        mainLayout.setVisibility(View.INVISIBLE);
        loadingLayout.setVisibility(View.VISIBLE);
        new HttpServerBackend(SelectActivity.this).getData(call, new HttpServerBackend.ResponseListener() {
            @Override
            public void onReturn(boolean success, JsonObject data, int message) {
                super.onReturn(success, data, message);

                if (success) {
                    if (data.get("success").getAsBoolean()) {
                        JsonArray userArray = data.getAsJsonArray("user");
                        if (userArray.size() > 0) {
                            JsonObject user = userArray.get(0).getAsJsonObject();
                            playerId = user.get("playerId").getAsString();
                            friendName = user.get("name").getAsString();
                            sendNotification();
                        } else {
                            showInviteDialog();
                        }

                    }
                } else {
                    showFailureMessage(message);
                }
            }
        });
    }

    void sendNotification() {
        try {
            String message = userName + " wants to find you " + friendName;
            JSONObject contents = new JSONObject();
            contents.put("en", message);
            JSONObject data = new JSONObject();
            data.put("type", 0);
            data.put("phone_number", myPhoneNum);
            JSONArray players = new JSONArray();
            players.put(playerId);


            JSONObject root = new JSONObject();
            root.put("contents", contents);
            root.put("data", data);
            root.put("include_player_ids", players);

            OneSignal.postNotification(root,
                    new OneSignal.PostNotificationResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            showSuccessMessage();
                          //  Log.i("OneSignalExample", "postNotification Success: " + response.toString());
                        }

                        @Override
                        public void onFailure(JSONObject response) {
                            showFailureMessage(404);
                           // Log.e("OneSignalExample", "postNotification Failure: " + response.toString());
                        }
                    });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void showSuccessMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingLayout.setVisibility(View.GONE);
                mainLayout.setVisibility(View.INVISIBLE);
                requestedLayout.setVisibility(View.VISIBLE);
                reqMsg.setText("We have Notified " + friendName);
            }
        });

    }

    void showFailureMessage(final int msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingLayout.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
                if(msg == -50){
                    showSnackBar("Please connect to Network...",true);
                }else showSnackBar("Oops!! Something Went Wrong...",true);
            }
        });

    }
    void showSnackBar(String message, final boolean flag){
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), message , Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (flag) {
                            recreate();
                        }
                    }
                });
        snackbar.show();
    }


    void showInviteDialog() {
        loadingLayout.setVisibility(View.GONE);
        inviteLayout.setVisibility(View.VISIBLE);
        inviteMsg.setText("Invite Your Friend "+selectedContactName);

    }
    @JavascriptInterface
    void inviteFriend(String friendName) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        //sendIntent.putExtra(Intent.EXTRA_TEXT,"Da");
       // sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey"+friendName+"Invited you to download this App");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey "+friendName+" Invited you to download this App https://play.google.com/store/apps/details?id=com.ashik619.dude");
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        startActivity(Intent.createChooser(sendIntent,"Choose"));
    }
}
