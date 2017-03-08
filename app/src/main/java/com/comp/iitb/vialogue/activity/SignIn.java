package com.comp.iitb.vialogue.activity;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.comp.iitb.vialogue.GlobalStuff.Master;
import com.comp.iitb.vialogue.MainActivity;
import com.comp.iitb.vialogue.R;
import com.comp.iitb.vialogue.coordinators.OnOtpReceived;
import com.comp.iitb.vialogue.coordinators.OnOtpSent;
import com.comp.iitb.vialogue.coordinators.OnPhoneNumberValidityChanged;
import com.comp.iitb.vialogue.coordinators.OnSignedOut;
import com.comp.iitb.vialogue.helpers.SharedPreferenceHelper;
import com.comp.iitb.vialogue.library.SendOtpAsync;
import com.comp.iitb.vialogue.listeners.PhoneNumberEditTextValidityListener;
import com.comp.iitb.vialogue.listeners.SmsOtpListener;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.Random;


public class SignIn extends AppCompatActivity implements
        View.OnClickListener, BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static Activity mSignInActivity;

    // constants
    private static final int SMS_READ_PERMISSION = 1235;
    private static final int SMS_RECEIVE_PERMISSION = 1236;
    private static final String TAG = SignIn.class.getSimpleName();
    private static final int RC_SIGN_IN = 007;
    private static final int PASSWORD_NUM_CHARACTERS = 36;
    private static final String PASSWORD = "Gwwla`U1uFJ;x_Khp%Wy>^cK61+[^kqXkX>HO=He";

    // variables
    private ArrayList<Integer> mOtp;
    private String mPhoneNumber = null;
    private String mPersonName = null;
    private String mProfilePictureUrl = null;
    private String mEmail = null;
    public static GoogleApiClient mGoogleApiClient;

    // UI Elements
    private SignInButton btnSignIn;
    private Button btnCancel;
    private EditText mPhoneNumberEditText;
    private Button mGenerateOtpButton;
    private EditText mOtpEditText;
    private Button mVerifyOtpButton;

    // Others
    private PhoneNumberEditTextValidityListener mPhoneNumberEditTextValidityListener;
    private static ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        mSignInActivity = this;

        // Initialize UI Components
        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
        btnCancel = (Button) findViewById(R.id.cancel);
        mPhoneNumberEditText = (EditText) findViewById(R.id.phone_number_edit_text);
        mGenerateOtpButton = (Button) findViewById(R.id.generate_otp_button);
        mOtpEditText = (EditText) findViewById(R.id.enter_otp_edit_text);
        mVerifyOtpButton = (Button) findViewById(R.id.verify_otp_button);
        mOtp = new ArrayList<Integer>();

        // Add Listeners
        btnSignIn.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        mGenerateOtpButton.setOnClickListener(this);
        mVerifyOtpButton.setOnClickListener(this);

        mPhoneNumberEditTextValidityListener = new PhoneNumberEditTextValidityListener(
                mPhoneNumberEditText,
                new OnPhoneNumberValidityChanged() {
                    @Override
                    public void onValidityChanged(boolean isValid) {
                        mGenerateOtpButton.setEnabled(isValid);
                    }
                }
        );

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        btnSignIn.setSize(SignInButton.SIZE_STANDARD);
        btnSignIn.setScopes(gso.getScopeArray());

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            // SIGN IN
            case R.id.btn_sign_in:
                signIn();
                break;

            // CANCEL
            case R.id.cancel:
                Toast.makeText(SignIn.this,
                        "Sign In to upload your projects.", Toast.LENGTH_SHORT).show();
                finish();
                break;

            // GENERATE OTP
            case R.id.generate_otp_button :
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    // RECEIVE SMS permission
                    if (getApplicationContext().checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
                        // permission granted
                    } else {
                        // permission not granted, ask for permission
                        ActivityCompat.requestPermissions(SignIn.this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_RECEIVE_PERMISSION);
                    }

                    // READ SMS permission
                    if (getApplicationContext().checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                        // permission granted
                    } else {
                        // permission not granted, ask for permission
                        ActivityCompat.requestPermissions(SignIn.this, new String[]{Manifest.permission.READ_SMS}, SMS_READ_PERMISSION);
                    }
                }

                Toast.makeText(getBaseContext(), "verifying OTP", Toast.LENGTH_SHORT).show();
                mPhoneNumber = "+91" + mPhoneNumberEditText.getText().toString();
                verifyOtp(mPhoneNumber);
                mOtpEditText.setVisibility(View.VISIBLE);
                mVerifyOtpButton.setVisibility(View.VISIBLE);
                break;

            // VERIFY OTP
            case R.id.verify_otp_button :
                try {
                    Integer otp = Integer.parseInt(mOtpEditText.getText().toString());
                    for(Integer generatedOtp: mOtp) {
                        if(otp.equals(generatedOtp)) {
                            onOtpVerified();
                            return;
                        }
                    }
                } catch (Exception e) {}
                Toast.makeText(SignIn.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public static void signOut(Context context, final OnSignedOut onSignedOut) {
        final Context context_ = context;
        mProgressDialog = ProgressDialog.show(context, "Logging Out", "Please wait ...");
//        try {
//            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
//                    new ResultCallback<Status>() {
//                        @Override
//                        public void onResult(Status status) {
//                        }
//                    });
//            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
//                    new ResultCallback<Status>() {
//                        @Override
//                        public void onResult(Status status) {}
//                    });
//        } catch (Exception e) {}

        ParseUser.getCurrentUser().logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    onLoggedOut(context_);
                } else {
                    onCouldNotLogOut(context_);
                }
                onSignedOut.done(e);
            }
        });
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            mPersonName = acct.getDisplayName();
            mEmail = acct.getEmail();
            try {
                mProfilePictureUrl = acct.getPhotoUrl().toString();
            } catch (NullPointerException e) {
                mProfilePictureUrl = "";
                e.printStackTrace();
            }

            // sign out of Google
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                        }
                    });
            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {}
                    });

            // sign into Parse
            signInParseUser();
        } else {
            onCouldNotSignIn();
        }
    }

    public void verifyOtp(String phoneNumber) {

        new SendOtpAsync(SignIn.this, new OnOtpSent() {
            @Override
            public void onDone(Object object, ParseException e) {
                if(e == null) {
                    // otp generated successfully
                    mOtp.add((Integer) object);
                } else {
                    // otp could not be generated
                    Toast.makeText(SignIn.this, "Could not generate OTP, please try again in some time.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }).execute(phoneNumber);

        SmsOtpListener.bindListener(new OnOtpReceived() {
            @Override
            public void onDone(Integer otp) {
                for(Integer generatedOtp: mOtp) {
                    if(otp.equals(generatedOtp)) {
                        onOtpVerified();
                    }
                }
            }
        });
    }

    public void onOtpVerified() {
        Toast.makeText(SignIn.this, "OTP Verified", Toast.LENGTH_SHORT).show();
        signInParseUser();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void signInParseUser() {
        mProgressDialog = ProgressDialog.show(SignIn.this, "Signing In", "Please Wait ...");
        String userName = null;
        if(mEmail == null) {
            // SIGN IN WITH PHONE NUMBER
            userName = mPhoneNumber;
        } else {
            // SIGN IN WITH EMAIL ID
            userName = mEmail;
        }

        ParseUser user = new ParseUser();
        user.setUsername(userName);
        if(mEmail != null) {
            user.setEmail(mEmail);
        }
        user.setPassword(PASSWORD);
        if(mPersonName != null) {
            user.put("name", mPersonName);
        }
        if(mProfilePictureUrl != null) {
            user.put("profile_picture_uri", mProfilePictureUrl);
        }
        if(mPhoneNumber != null) {
            user.put("phone_number", mPhoneNumber);
        }

        // Try signing up th user
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    // ERROR SIGNING UP
                    switch(e.getCode()) {
                        case ParseException.USERNAME_TAKEN :
                            // User already exists in the database, so just log him in
                            logInParseUser();
                    }
                } else {
                    onSignedIn();
                }

            }
        });
    }

    public void logInParseUser() {
        String userName = null;
        if(mEmail == null) {
            // SIGN IN WITH PHONE NUMBER
            userName = mPhoneNumber;
        } else {
            // SIGN IN WITH EMAIL ID
            userName = mEmail;
        }

        ParseUser user = new ParseUser();
        user.logInInBackground(userName, PASSWORD, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                onSignedIn();
            }
        });
    }

    public void onSignedIn() {
        String userName = null;
        if(mEmail == null) {
            // SIGN IN WITH PHONE NUMBER
            userName = mPhoneNumber;
            Toast.makeText(SignIn.this, "Successfully signed in with user mobile number : " + userName, Toast.LENGTH_LONG).show();
        } else {
            // SIGN IN WITH EMAIL ID
            Toast.makeText(SignIn.this, "Successfully signed in with Email ID : " + userName, Toast.LENGTH_LONG).show();
            userName = mEmail;
        }
        try {
            mProgressDialog.dismiss();
        } catch (Exception e) {}
        finish();
    }

    public void onCouldNotSignIn() {
        Toast.makeText(SignIn.this, "An error occurred while signing in", Toast.LENGTH_LONG).show();
        try {
            mProgressDialog.dismiss();
        } catch (Exception e) {}
    }

    private static void onLoggedOut(Context context) {
        Toast.makeText(context, "Successfully logged out", Toast.LENGTH_LONG).show();
        try {
            mProgressDialog.dismiss();
        } catch (Exception e) {}
    }

    private static void onCouldNotLogOut(Context context) {
        Toast.makeText(context, "An error occured while signing out", Toast.LENGTH_LONG).show();
        try {
            mProgressDialog.dismiss();
        } catch (Exception e) {}
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case SMS_READ_PERMISSION :
                if(!(grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(SignIn.this, "Permission to read SMS is required to automatically read OTP", Toast.LENGTH_LONG).show();
                }
            case SMS_RECEIVE_PERMISSION :
                if(!(grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(SignIn.this, "Permission to receive SMS is required to automatically read OTP", Toast.LENGTH_LONG).show();
                }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
