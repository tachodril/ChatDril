package com.example.ritik.chatdril;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String msgreceiverid,msgreceivername,msgreceiverimage,msgsenderid;
    private TextView userName,userLastseen;
    private CircleImageView userImage;

    private Toolbar chattoolbar;
    private ImageButton sendmsgbutton,sendfilesbutton;
    private EditText msginputtext;

    private FirebaseAuth mauth;
    private DatabaseReference rootref;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private RecyclerView usermessagelist;

    private String savecurrenttime,savecurrentdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mauth=FirebaseAuth.getInstance();
        msgsenderid=mauth.getCurrentUser().getUid();

        rootref= FirebaseDatabase.getInstance().getReference();

        msgreceiverid=getIntent().getExtras().get("visit_user_id").toString();
        msgreceivername=getIntent().getExtras().get("visit_user_name").toString();
        msgreceiverimage=getIntent().getExtras().get("visit_image").toString();



        initialise();
        Displaylastseen();

        userName.setText(msgreceivername);
        Picasso.get().load(msgreceiverimage).placeholder(R.drawable.profile_image).into(userImage);


        sendmsgbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmessage();
            }
        });
    }

    private void initialise() {


        chattoolbar=findViewById(R.id.chat_toolbar);
        setSupportActionBar(chattoolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarview=layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionbarview);

        sendmsgbutton=findViewById(R.id.send_msg_btn);
        sendfilesbutton=findViewById(R.id.send_files_btn);
        msginputtext=findViewById(R.id.input_msg);

        userImage=findViewById(R.id.custom_profile_image);
        userName=findViewById(R.id.custom_profile_name);
        userLastseen=findViewById(R.id.custom_user_last_seen);

        messageAdapter=new MessageAdapter(messagesList);
        usermessagelist=findViewById(R.id.private_msg_list_of_users);
        linearLayoutManager=new LinearLayoutManager(this);
        usermessagelist.setLayoutManager(linearLayoutManager);
        usermessagelist.setAdapter(messageAdapter);



        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentdate =  new SimpleDateFormat("MMM dd, yyyy");
        savecurrentdate=currentdate.format(calendar.getTime());

        SimpleDateFormat currenttime =  new SimpleDateFormat("hh:mm a");
        savecurrenttime=currenttime.format(calendar.getTime());



    }

    private void Displaylastseen()
    {
        rootref.child("Users").child(msgreceiverid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state= dataSnapshot.child("userState").child("state").getValue().toString();
                            String date= dataSnapshot.child("userState").child("date").getValue().toString();
                            String time= dataSnapshot.child("userState").child("time").getValue().toString();

                            if(state.equals("online"))
                            {
                                userLastseen.setText("online");
                            }
                            else if(state.equals("offline"))
                            {
                                userLastseen.setText("Last seen: "+date+" "+time);
                            }
                        }
                        else
                        {
                            userLastseen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        rootref.child("Messages").child(msgsenderid).child(msgreceiverid)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Messages messages=dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        usermessagelist.smoothScrollToPosition(usermessagelist.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendmessage()
    {
        String msgtext=msginputtext.getText().toString();
        if(TextUtils.isEmpty(msgtext))
        {
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String msgsenderref="Messages/"+ msgsenderid+"/"+msgreceiverid;
            String msgreceiverref="Messages/"+msgreceiverid+"/"+msgsenderid;

            DatabaseReference usermsgkeyref=rootref.child("Messages")
                    .child(msgsenderid).child(msgreceiverid).push();

            String msgpushid=usermsgkeyref.getKey();

            Map msgtextbody=new HashMap();
            msgtextbody.put("message",msgtext);
            msgtextbody.put("type","text");
            msgtextbody.put("from",msgsenderid);
            msgtextbody.put("to",msgreceiverid);
            msgtextbody.put("messageID",msgpushid);
            msgtextbody.put("time",savecurrenttime);
            msgtextbody.put("date",savecurrentdate);

            Map msgbodydetails=new HashMap();
            msgbodydetails.put(msgsenderref+"/"+msgpushid,msgtextbody);
            msgbodydetails.put(msgreceiverref+"/"+msgpushid,msgtextbody);

            rootref.updateChildren(msgbodydetails)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(ChatActivity.this, "Message sent successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                            msginputtext.setText("");
                        }
                    });
        }
    }
}
