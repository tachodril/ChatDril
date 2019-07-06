package com.example.ritik.chatdril;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ViewPager mviewpager;
    private TabLayout mtablayout;
    private ViewPagerAdapter mviewpageradap;

    private FirebaseAuth mauth;
    private DatabaseReference rootreference;

    private String currentuserid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mtoolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("ChatDril");

        mviewpager=findViewById(R.id.main_tabs_pager);
        mviewpageradap=new ViewPagerAdapter(getSupportFragmentManager());
        mviewpager.setAdapter(mviewpageradap);

        mtablayout=findViewById(R.id.main_tabs);
        mtablayout.setupWithViewPager(mviewpager);

        mauth=FirebaseAuth.getInstance();
        rootreference= FirebaseDatabase.getInstance().getReference();

        currentuserid=mauth.getCurrentUser().getUid();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentuser=mauth.getCurrentUser();
        if(currentuser==null)
        {
            sendusertologinactivity();
        }
        else{
            updateuserstatus("online");

            verifyuserexistance();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentuser=mauth.getCurrentUser();
        if(currentuser!=null)
        {
            updateuserstatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentuser=mauth.getCurrentUser();
        if(currentuser!=null)
        {
            updateuserstatus("offline");
        }
    }

    private void verifyuserexistance() {
        String currentuserid=mauth.getCurrentUser().getUid();
        rootreference.child("Users").child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists())
                {
                    //Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendusertosettingsactivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void Createnewgroup(final String groupname) {
        rootreference.child("Groups").child(groupname).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, groupname+" is created successfully !!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void requestnewgroup() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.MyAlertDialogStyle);
        builder.setTitle("Enter Group Name :");

        final EditText groupname_field=new EditText(MainActivity.this);
        groupname_field.setHint("e.g. Coding Cafe");
        builder.setView(groupname_field);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupname=groupname_field.getText().toString();

                if(TextUtils.isEmpty(groupname)){
                    Toast.makeText(MainActivity.this, "Please write group name.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Createnewgroup(groupname);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void sendusertologinactivity() {
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void sendusertosettingsactivity() {
        Intent setIntent=new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(setIntent);
    }


    private void sendusertofindfriendsactivity() {
        Intent findIntent=new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_option){
            updateuserstatus("offline");
            mauth.signOut();
            sendusertologinactivity();
        }
        if(item.getItemId()==R.id.main_create_group_option){
            requestnewgroup();
        }
        if(item.getItemId()==R.id.main_settings_option){
            sendusertosettingsactivity();
        }
        if(item.getItemId()==R.id.main_find_friend_option){
            sendusertofindfriendsactivity();
        }
        return true;
    }

    private void updateuserstatus(String state)
    {
        String savecurrenttime,savecurrentdate;

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentdate =  new SimpleDateFormat("MMM dd, yyyy");
        savecurrentdate=currentdate.format(calendar.getTime());

        SimpleDateFormat currenttime =  new SimpleDateFormat("hh:mm a");
        savecurrenttime=currenttime.format(calendar.getTime());

        HashMap<String,Object> onlinestate= new HashMap<>();
        onlinestate.put("time",savecurrenttime);
        onlinestate.put("date",savecurrentdate);
        onlinestate.put("state",state);

        rootreference.child("Users").child(currentuserid).child("userState")
                .updateChildren(onlinestate);

    }


}