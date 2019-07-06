package com.example.ritik.chatdril;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiveruserid,current_stats,senderuserid;

    private CircleImageView userprofileimage;
    private TextView userprofilename,userprofilestatus;
    private Button sendmeassgebutton,declinemsgrequestbutton;

    private DatabaseReference userref,chatrequestref,contactsref,notificationref;
    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userref= FirebaseDatabase.getInstance().getReference().child("Users");
        chatrequestref=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsref=FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationref=FirebaseDatabase.getInstance().getReference().child("Notifications");

        mauth=FirebaseAuth.getInstance();
        senderuserid=mauth.getCurrentUser().getUid();

        receiveruserid=getIntent().getExtras().get("visit_user_id").toString();
        Toast.makeText(this, "User ID :"+receiveruserid, Toast.LENGTH_SHORT).show();

        userprofileimage=findViewById(R.id.visit_profile_image);
        userprofilestatus=findViewById(R.id.visit_user_status);
        userprofilename=findViewById(R.id.visit_user_name);
        sendmeassgebutton=findViewById(R.id.send_msg_request_button);
        declinemsgrequestbutton=findViewById(R.id.decline_msg_request_button);
        current_stats="new";

        Retriveuserinfo();
    }

    private void Retriveuserinfo() {
        userref.child(receiveruserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("image")){
                    String userimg=dataSnapshot.child("image").getValue().toString();
                    String username=dataSnapshot.child("name").getValue().toString();
                    String userstatus=dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userimg).placeholder(R.drawable.profile_image).into(userprofileimage);
                    userprofilename.setText(username);
                    userprofilestatus.setText(userstatus);


                    Managechatrequests();
                }
                else{
                    String username=dataSnapshot.child("name").getValue().toString();
                    String userstatus=dataSnapshot.child("status").getValue().toString();

                    userprofilename.setText(username);
                    userprofilestatus.setText(userstatus);
                    Managechatrequests();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void Managechatrequests()
    {
        chatrequestref.child(senderuserid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild(receiveruserid))
                        {
                            String request_type=dataSnapshot.child(receiveruserid).child("request_type").getValue().toString();

                            if (request_type.equals("sent"))
                            {
                                current_stats="request_sent";
                                sendmeassgebutton.setText("Cancel Chat Request");
                            }
                            else if(request_type.equals("received"))
                            {
                                current_stats="request_received";
                                sendmeassgebutton.setText("Accept Chat Request");

                                declinemsgrequestbutton.setVisibility(View.VISIBLE);
                                declinemsgrequestbutton.setEnabled(true);

                                declinemsgrequestbutton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Cancelchatrequest();
                                    }
                                });

                            }
                        }
                        else
                        {
                            contactsref.child(senderuserid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot)
                                        {
                                            if(dataSnapshot.hasChild(receiveruserid))
                                            {
                                                current_stats="friends";
                                                sendmeassgebutton.setText("Remove this contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
        if(!senderuserid.equals(receiveruserid))
        {
            sendmeassgebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendmeassgebutton.setEnabled(false);
                    if(current_stats.equals("new"))
                    {
                        sendchatrequest();
                    }
                    if(current_stats.equals("request_sent"))
                    {
                        Cancelchatrequest();
                    }
                    if(current_stats.equals("request_received"))
                    {
                        Acceptchatrequest();
                    }
                    if(current_stats.equals("friends"))
                    {
                        Removespecificcontact();
                    }
                }
            });
        }
        else
        {
            sendmeassgebutton.setVisibility(View.INVISIBLE);
        }
    }

    private void Removespecificcontact()
    {
        contactsref.child(senderuserid).child(receiveruserid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            contactsref.child(receiveruserid).child(senderuserid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful()){
                                                sendmeassgebutton.setEnabled(true);
                                                current_stats="new";
                                                sendmeassgebutton.setText("Send Message");

                                                declinemsgrequestbutton.setVisibility(View.INVISIBLE);
                                                declinemsgrequestbutton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void Acceptchatrequest()
    {
        contactsref.child(receiveruserid).child(senderuserid)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            chatrequestref.child(senderuserid).child(receiveruserid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                chatrequestref.child(receiveruserid).child(senderuserid)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                sendmeassgebutton.setEnabled(true);
                                                                current_stats="friends";
                                                                sendmeassgebutton.setText("Remove this contact");

                                                                declinemsgrequestbutton.setVisibility(View.INVISIBLE);
                                                                declinemsgrequestbutton.setEnabled(false);
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void Cancelchatrequest()
    {
        chatrequestref.child(senderuserid).child(receiveruserid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            chatrequestref.child(receiveruserid).child(senderuserid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful()){
                                                sendmeassgebutton.setEnabled(true);
                                                current_stats="new";
                                                sendmeassgebutton.setText("Send Message");

                                                declinemsgrequestbutton.setVisibility(View.INVISIBLE);
                                                declinemsgrequestbutton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendchatrequest()
    {
        chatrequestref.child(senderuserid).child(receiveruserid)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            chatrequestref.child(receiveruserid).child(senderuserid)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                HashMap<String,String> chatnotificationmap=new HashMap<>();
                                                chatnotificationmap.put("from",senderuserid);
                                                chatnotificationmap.put("type","request");

                                                notificationref.child(receiveruserid).push()
                                                        .setValue(chatnotificationmap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    sendmeassgebutton.setEnabled(true);
                                                                    current_stats="request_sent";
                                                                    sendmeassgebutton.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });


                                                sendmeassgebutton.setEnabled(true);
                                                current_stats="request_sent";
                                                sendmeassgebutton.setText("Cancel Chat Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}