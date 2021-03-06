package com.comp.iitb.vialogue.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.comp.iitb.vialogue.Network.LokavidyaSso.Apis.LogIn;
import com.comp.iitb.vialogue.Network.LokavidyaSso.Apis.ResetPassword;
import com.comp.iitb.vialogue.R;
import com.comp.iitb.vialogue.coordinators.OnDoneResetPassword;

public class ResetPasswordActivity extends AppCompatActivity {

    //ui Elements
    TextView mEnterNewPasswordTextView;
    TextView mReEnterToConfirmTextView;
    EditText mEnterNewPasswordEditText;
    EditText mReEnterToConfirmEditText;
    Button mConfirmButton;

    //variables
    String mNewPassword;
    String mConfirmedPassword;
    Context mContext;

    private String mResponseString;
    String mOtp;
    String mRegistrationData;

    ResetPassword.RegistrationType mResetRegistrationType;

    LogIn.RegistrationType mLoginRegistrationType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mContext = ResetPasswordActivity.this;

        Bundle args = getIntent().getExtras();
        String registrationData = args.getString(getResources().getString(R.string.registrationData));
        String registrationType = args.getString(getResources().getString(R.string.registrationType));
        mOtp = args.getString(getResources().getString(R.string.otp));

        //dummy data
        //String registrationData = "k.omkar357@gmail.com";
        //String registrationType = getResources().getString(R.string.email);

        mRegistrationData = registrationData;

        mLoginRegistrationType = (registrationType.equals(getResources().getString(R.string.email))) ?
                LogIn.RegistrationType.EMAIL_ID : LogIn.RegistrationType.PHONE_NUMBER;

        mResetRegistrationType = (registrationType.equals(getResources().getString(R.string.email))) ?
                ResetPassword.RegistrationType.EMAIL_ID : ResetPassword.RegistrationType.PHONE_NUMBER;

        //initialize ui elements
        mEnterNewPasswordTextView = (TextView) findViewById(R.id.tv_enter_new_password);
        mEnterNewPasswordEditText = (EditText) findViewById(R.id.et_enter_new_password);
        mReEnterToConfirmTextView = (TextView) findViewById(R.id.tv_re_enter_to_confirm);
        mReEnterToConfirmEditText = (EditText) findViewById(R.id.et_re_enter_to_confirm);
        mConfirmButton = (Button) findViewById(R.id.btn_confirm);

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewPassword = mEnterNewPasswordEditText.getText().toString();
                if(mNewPassword.length() == 0) {
                    mEnterNewPasswordEditText.requestFocus();
                    mEnterNewPasswordTextView.setTextColor(ContextCompat.getColor(mContext, android.R.color.holo_red_light));
                    return;
                } else if(mNewPassword.length() < 8) {
                    Toast.makeText(mContext, "Passwords should contain minimum 8 characters", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    mEnterNewPasswordTextView.setTextColor(ContextCompat.getColor(mContext, android.R.color.tab_indicator_text));
                }
                mConfirmedPassword = mReEnterToConfirmEditText.getText().toString();
                if(mConfirmedPassword.length() == 0) {
                    mReEnterToConfirmEditText.requestFocus();
                    mReEnterToConfirmTextView.setTextColor(ContextCompat.getColor(mContext, android.R.color.holo_red_light));
                    return;
                } else if(mConfirmedPassword.length() < 8) {
                    Toast.makeText(mContext, "Passwords should contain minimum 8 characters", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    mReEnterToConfirmTextView.setTextColor(ContextCompat.getColor(mContext, android.R.color.tab_indicator_text));
                }

                if(mConfirmedPassword.equals(mNewPassword)) {
                    resetPassword(mContext, mResetRegistrationType, mRegistrationData, mOtp, mNewPassword);
                }

            }
        });
    }

    private void resetPassword(Context context, ResetPassword.RegistrationType registrationType, String registrationData, String otp, String newPassword) {

        ResetPassword.resetPasswordInBackground(
                context,
                registrationType,
                registrationData,
                otp,
                newPassword,
                new OnDoneResetPassword() {
                    @Override
                    public void done(ResetPassword.ResetPasswordResponse resetPasswordResponse) {
                        switch (resetPasswordResponse.getResponseType()) {
                            case PASSWORD_RESET:
                                mResponseString = resetPasswordResponse.getResponseString();
                                LoginActivity.login(mContext, mLoginRegistrationType, mRegistrationData, newPassword);
                            case INVALID_OTP:
                                mResponseString = resetPasswordResponse.getResponseString();
                            case NETWORK_ERROR:
                                mResponseString = resetPasswordResponse.getResponseString();
                        }
                        Toast.makeText(context, mResponseString, Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
