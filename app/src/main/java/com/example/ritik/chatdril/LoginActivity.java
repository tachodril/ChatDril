package com.example.ritik.chatdril;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mauth;
    private ProgressDialog loadingbar;

    private Button login_button,phone_login_button;
    private EditText user_email,user_password;
    private TextView wneed_new_ac,forget_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialise();

        wneed_new_ac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendusertoRegisteractivity();
            }
        });
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Allowusertologin();
            }
        });
        phone_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneloginintent=new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(phoneloginintent);
            }
        });

        mauth=FirebaseAuth.getInstance();
    }

    private void Allowusertologin() {
        String email=user_email.getText().toString();
        String password=user_password.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email...",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter password...",Toast.LENGTH_SHORT).show();
        }
        else{
            loadingbar.setTitle("Signing In");
            loadingbar.setMessage("Please wait...");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();
            mauth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendusertoMainactivity();
                                Toast.makeText(LoginActivity.this, "Logged in successfully...", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                            else{
                                String msg=task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error :"+msg, Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });
        }
    }

    private void initialise() {
        login_button=findViewById(R.id.login_button);
        phone_login_button=findViewById(R.id.phone_login_button);
        user_email=findViewById(R.id.login_email);
        user_password=findViewById(R.id.login_password);
        wneed_new_ac=findViewById(R.id.need_new_ac);
        forget_password=findViewById(R.id.forget_password_link);

        loadingbar=new ProgressDialog(this,R.style.MyAlertDialogStyle);

    }

    private void sendusertoMainactivity() {
        Intent MainIntent=new Intent(LoginActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
    private void sendusertoRegisteractivity() {
        Intent regIntent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(regIntent);
    }
}
