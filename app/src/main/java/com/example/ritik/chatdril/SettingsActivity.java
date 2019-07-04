package com.example.ritik.chatdril;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateaccountsettings;
    private EditText username,userstatus;
    private CircleImageView userprofileimage;

    private String currentuserid;
    private FirebaseAuth mauth;
    private DatabaseReference rootref;

    private static final int gallerypick=1;
    private  StorageReference userprofileimgref;

    private ProgressDialog loadingbar;
    private boolean image_before_upload_flag =false;
    private String before_update_image_url;

    private Toolbar settingstoolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userprofileimgref= FirebaseStorage.getInstance().getReference().child("Profile Images");


        initialise();



        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();
        rootref= FirebaseDatabase.getInstance().getReference();

        updateaccountsettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatesettings();
            }
        });

        Retriveuserinfo();

        userprofileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryintent=new Intent();
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/*");
                startActivityForResult(galleryintent,gallerypick);
            }
        });
    }



    private void initialise() {
        updateaccountsettings=findViewById(R.id.update_settings_button);
        username=findViewById(R.id.set_user_name);
        userstatus=findViewById(R.id.set_profile_status);
        userprofileimage=findViewById(R.id.profile_image);

        settingstoolbar=findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingstoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

        loadingbar=new ProgressDialog(this,R.style.MyAlertDialogStyle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode==gallerypick && resultCode==RESULT_OK && data!=null)
        {
            Uri imageuri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK)
            {
                loadingbar.setTitle("Set profile image");
                loadingbar.setMessage("Please wait your profile image is updating...");
                loadingbar.setCanceledOnTouchOutside(false);
                loadingbar.show();

                Uri resulturi=result.getUri();

                StorageReference filepath=userprofileimgref.child(currentuserid + ".jpg");
                 filepath.putFile(resulturi).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                     @Override
                     public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                         if(task.isSuccessful()){
                             Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();
                             final String downloadurl = task.getResult().getDownloadUrl().toString();
                             before_update_image_url=downloadurl;
                             rootref.child("Users").child(currentuserid).child("image")
                                     .setValue(downloadurl)
                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Void> task) {
                                             if(task.isSuccessful())
                                             {
                                                 Toast.makeText(SettingsActivity.this, "Image saved in database successfully.", Toast.LENGTH_SHORT).show();

                                                 image_before_upload_flag=true;
                                                 loadingbar.dismiss();
                                             }
                                             else
                                             {

                                                 String msg=task.getException().toString();
                                                 Toast.makeText(SettingsActivity.this, "Error :"+msg, Toast.LENGTH_SHORT).show();
                                                 loadingbar.dismiss();
                                             }
                                         }
                                     });
                         }
                         else{
                             String msg=task.getException().toString();
                             Toast.makeText(SettingsActivity.this, "Error :"+msg, Toast.LENGTH_SHORT).show();
                             loadingbar.dismiss();
                         }
                     }
                 });
            }
        }
    }

    private void updatesettings() {
        String setusername=username.getText().toString();
        String setuserstatus=userstatus.getText().toString();

        if(TextUtils.isEmpty(setusername))
        {
            Toast.makeText(this, "Please write your user name.", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setuserstatus))
        {
            Toast.makeText(this, "Please write your profile status.", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String,Object> profilemap=new HashMap<>();


            profilemap.put("uid",currentuserid);
            profilemap.put("name",setusername);
            profilemap.put("status",setuserstatus);
            if(image_before_upload_flag==true){
                profilemap.put("image",before_update_image_url);

            }
            rootref.child("Users").child(currentuserid).updateChildren(profilemap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendusertoMainactivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated successfully.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String msg=task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error :"+msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void sendusertoMainactivity() {
        Intent MainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
    private void Retriveuserinfo() {
        rootref.child("Users").child(currentuserid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists())&& (dataSnapshot.hasChild("name")&&dataSnapshot.hasChild("image"))){
                            String retriveusername=dataSnapshot.child("name").getValue().toString();
                            String retriveuserstatus=dataSnapshot.child("status").getValue().toString();
                            String retriveuserimage=dataSnapshot.child("image").getValue().toString();

                            username.setText(retriveusername);
                            userstatus.setText(retriveuserstatus);
                            Picasso.get().load(retriveuserimage).placeholder(R.drawable.profile_image).into(userprofileimage);
                            image_before_upload_flag=true;
                            before_update_image_url=retriveuserimage;
                        }
                        else if((dataSnapshot.exists())&& (dataSnapshot.hasChild("name"))){
                            String retriveusername=dataSnapshot.child("name").getValue().toString();
                            String retriveuserstatus=dataSnapshot.child("status").getValue().toString();

                            username.setText(retriveusername);
                            userstatus.setText(retriveuserstatus);
                        }
                        else{
                            Toast.makeText(SettingsActivity.this, "Update your profile information", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
