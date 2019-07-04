package com.example.ritik.chatdril;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ImageButton sendmessagebutton;
    private EditText usermsginput;
    private ScrollView mscrollview;
    private TextView displaytextmessages;

    private FirebaseAuth mauth;
    private DatabaseReference userref,groupnameref,groupmsgkeyref;

    private String currentgroupname,currentuserid,currentusername,currenttime,currentdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        currentgroupname=getIntent().getExtras().get("groupName").toString();
        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();
        userref= FirebaseDatabase.getInstance().getReference().child("Users");
        groupnameref=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentgroupname);

        //Toast.makeText(GroupChatActivity.this, currentgroupname, Toast.LENGTH_SHORT).show();

        initialise();

        Getuserinfo();
        sendmessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Savemsginfotodatabase();

                usermsginput.setText("");
                mscrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupnameref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                    mscrollview.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                    mscrollview.fullScroll(ScrollView.FOCUS_DOWN);
                }
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


    private void initialise() {
        mtoolbar=findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentgroupname);

        sendmessagebutton=findViewById(R.id.send_message_button);
        usermsginput=findViewById(R.id.input_group_msg);
        displaytextmessages=findViewById(R.id.group_chat_text_display);
        mscrollview=findViewById(R.id.myscrollview);


    }


    private void Getuserinfo() {
        userref.child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentusername=dataSnapshot.child("name").getValue().toString();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void Savemsginfotodatabase() {
        String message=usermsginput.getText().toString();
        String messagekey=groupnameref.push().getKey();
        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        }
        else{
            Calendar calForDate=Calendar.getInstance();
            SimpleDateFormat currentdateformat=new SimpleDateFormat("MMM dd, yyyy");
            currentdate=currentdateformat.format(calForDate.getTime());


            Calendar calForTime=Calendar.getInstance();
            SimpleDateFormat currenttimeformat=new SimpleDateFormat("hh:mm a");
            currenttime=currenttimeformat.format(calForTime.getTime());

            HashMap<String,Object> groupmessagekey=new HashMap<>();
            groupnameref.updateChildren(groupmessagekey);

            groupmsgkeyref=groupnameref.child(messagekey);

            HashMap<String,Object> messageinfomap=new HashMap<>();
            messageinfomap.put("name",currentusername);
            messageinfomap.put("message",message);
            messageinfomap.put("date",currentdate);
            messageinfomap.put("time",currenttime);
            groupmsgkeyref.updateChildren(messageinfomap);
        }
    }


    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator=dataSnapshot.getChildren().iterator();

        while (iterator.hasNext())
        {
            String chatdate= (String) ((DataSnapshot)iterator.next()).getValue();
            String chatmessage= (String) ((DataSnapshot)iterator.next()).getValue();
            String chatname= (String) ((DataSnapshot)iterator.next()).getValue();
            String chattime= (String) ((DataSnapshot)iterator.next()).getValue();

            displaytextmessages.append(chatname+" :\n"+chatmessage+"\n"+chattime+"   "+chatdate+"\n\n\n");

        }
    }

}
