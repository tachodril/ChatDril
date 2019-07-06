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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Button create_ac_button;
    private EditText muser_email,muser_password;
    private TextView already_a_user_link;

    private FirebaseAuth mauth;
    private DatabaseReference rootreference;

    private ProgressDialog loading_bar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initialise();
        already_a_user_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendusertologinactivity();
            }
        });
        create_ac_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createnewaccount();
            }
        });


        mauth=FirebaseAuth.getInstance();
        rootreference= FirebaseDatabase.getInstance().getReference();
    }

    private void createnewaccount() {
        String email=muser_email.getText().toString();
        String password=muser_password.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email...",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter password...",Toast.LENGTH_SHORT).show();
        }
        else{
            loading_bar.setTitle("Creating new account");
            loading_bar.setMessage("Please wait while we create new account for you");
            loading_bar.setCanceledOnTouchOutside(true);
            loading_bar.show();
            mauth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {

                                String deviceToken= FirebaseInstanceId.getInstance().getToken();

                                String currentuserid=mauth.getCurrentUser().getUid();
                                rootreference.child("Users").child(currentuserid).setValue("");

                                rootreference.child("Users").child(currentuserid).child("device_token")
                                        .setValue(deviceToken);

                                sendusertoMainactivity();
                                Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                loading_bar.dismiss();
                            }
                            else{
                                String msg=task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error :"+msg, Toast.LENGTH_SHORT).show();
                                loading_bar.dismiss();
                            }
                        }
                    });
        }
    }

    private void sendusertoMainactivity() {
        Intent MainIntent=new Intent(RegisterActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void initialise() {

        create_ac_button=findViewById(R.id.register_button);
        muser_email=findViewById(R.id.register_email);
        muser_password=findViewById(R.id.register_password);
        already_a_user_link=findViewById(R.id.already_have_ac);
        loading_bar=new ProgressDialog(this,R.style.MyAlertDialogStyle);
    }
    private void sendusertologinactivity() {
        Intent loginIntent=new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }
}
