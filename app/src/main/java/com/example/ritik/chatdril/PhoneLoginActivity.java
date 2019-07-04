package com.example.ritik.chatdril;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendverificationcodebutton,verifybutton;
    private EditText inputphonenumber,inputverificationcode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mauth;

    private ProgressDialog loadingbar;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        sendverificationcodebutton=findViewById(R.id.send_ver_code_button);
        verifybutton=findViewById(R.id.verify_button);
        inputphonenumber=findViewById(R.id.phone_number_input);
        inputverificationcode=findViewById(R.id.verification_code_input);

        loadingbar=new ProgressDialog(this,R.style.MyAlertDialogStyle);
        mauth=FirebaseAuth.getInstance();

        sendverificationcodebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                String phonenumber=inputphonenumber.getText().toString();
                if(TextUtils.isEmpty(phonenumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter phone number first...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingbar.setTitle("Phone Verification");
                    loadingbar.setMessage("Please wait, while we are authenticating your phone...");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phonenumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);
                }
            }
        });

        verifybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendverificationcodebutton.setVisibility(View.INVISIBLE);
                inputphonenumber.setVisibility(View.INVISIBLE);

                String verificationcode=inputverificationcode.getText().toString();
                if(TextUtils.isEmpty(verificationcode)){
                    Toast.makeText(PhoneLoginActivity.this, "Please enter verification code...", Toast.LENGTH_SHORT).show();
                }
                else {

                    loadingbar.setTitle("Verification Code");
                    loadingbar.setMessage("Please wait, while we are verifying verification code...");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationcode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });


        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingbar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number...", Toast.LENGTH_SHORT).show();

                sendverificationcodebutton.setVisibility(View.VISIBLE);
                inputphonenumber.setVisibility(View.VISIBLE);

                verifybutton.setVisibility(View.INVISIBLE);
                inputverificationcode.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingbar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent, please check and verify...", Toast.LENGTH_SHORT).show();


                sendverificationcodebutton.setVisibility(View.INVISIBLE);
                inputphonenumber.setVisibility(View.INVISIBLE);

                verifybutton.setVisibility(View.VISIBLE);
                inputverificationcode.setVisibility(View.VISIBLE);
            }
        };
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mauth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            loadingbar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulations, you're logged in successfuly...", Toast.LENGTH_SHORT).show();
                            sendusertoMainactivity();
                        }
                        else
                        {
                            String mesg=task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error :"+mesg, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void sendusertoMainactivity() {
        Intent MainIntent=new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(MainIntent);
        finish();
    }
}
