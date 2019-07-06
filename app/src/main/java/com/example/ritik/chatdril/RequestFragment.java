package com.example.ritik.chatdril;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment
{


    private View Requestsfragmentview;
    private RecyclerView myrequestlist;

    private DatabaseReference chatreqref,userref,Contactsref;
    private FirebaseAuth mauth;
    private String currentuserid;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Requestsfragmentview=inflater.inflate(R.layout.fragment_request, container, false);

        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();

        userref=FirebaseDatabase.getInstance().getReference().child("Users");

        chatreqref= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        Contactsref=FirebaseDatabase.getInstance().getReference().child("Contacts");

        myrequestlist=Requestsfragmentview.findViewById(R.id.chat_request_list);
        myrequestlist.setLayoutManager(new LinearLayoutManager(getContext()));

        return Requestsfragmentview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatreqref.child(currentuserid),Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,Requestviewholder> adapter=
                new FirebaseRecyclerAdapter<Contacts, Requestviewholder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final Requestviewholder holder, int position, @NonNull Contacts model)
                    {
                        holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);

                        final String list_user_id=getRef(position).getKey();

                        DatabaseReference gettyperef=getRef(position).child("request_type").getRef();

                        gettyperef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.exists())
                                {
                                    String type=dataSnapshot.getValue().toString();
                                    if(type.equals("received")){
                                        userref.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("image"))
                                                {

                                                    final String requestprofileimage=dataSnapshot.child("image").getValue().toString();


                                                    Picasso.get().load(requestprofileimage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                                                }

                                                final String requestusername=dataSnapshot.child("name").getValue().toString();
                                                final String requestuserstatus=dataSnapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestusername);
                                                holder.userStatus.setText("Wants to connect with you.");

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v)
                                                    {
                                                        CharSequence options[]=new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"
                                                                };
                                                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext(),R.style.MyAlertDialogStyle);
                                                        builder.setTitle(requestusername+" Chat Request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int i)
                                                            {
                                                                if(i==0)
                                                                {
                                                                    Contactsref.child(currentuserid).child(list_user_id).child("Contacts")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                Contactsref.child(list_user_id).child(currentuserid).child("Contacts")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        if(task.isSuccessful())
                                                                                        {
                                                                                            chatreqref.child(currentuserid).child(list_user_id)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                        {
                                                                                                            if(task.isSuccessful())
                                                                                                            {
                                                                                                                chatreqref.child(list_user_id).child(currentuserid)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                                                            {
                                                                                                                                if(task.isSuccessful())
                                                                                                                                {
                                                                                                                                    Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show();

                                                                                                                                }
                                                                                                                            }
                                                                                                                        });
                                                                                                            }
                                                                                                        }
                                                                                                    });

                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                                if(i==1)
                                                                {
                                                                    chatreqref.child(currentuserid).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        chatreqref.child(list_user_id).child(currentuserid)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                    {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext(), "Request deleted", Toast.LENGTH_SHORT).show();

                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                                        builder.show();


                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }

                                    else if(type.equals("sent"))
                                    {
                                        Button request_sent_btn=holder.itemView.findViewById(R.id.request_accept_button);
                                        request_sent_btn.setText("Request Sent");

                                        holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.INVISIBLE);

                                        userref.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("image"))
                                                {

                                                    final String requestprofileimage=dataSnapshot.child("image").getValue().toString();


                                                    Picasso.get().load(requestprofileimage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                                                }

                                                final String requestusername=dataSnapshot.child("name").getValue().toString();
                                                final String requestuserstatus=dataSnapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestusername);
                                                holder.userStatus.setText("You have sent a request to "+requestusername);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v)
                                                    {
                                                        CharSequence options[]=new CharSequence[]
                                                                {
                                                                        "Cancel chat request"
                                                                };
                                                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext(),R.style.MyAlertDialogStyle);
                                                        builder.setTitle("Already Sent Request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int i)
                                                            {
                                                                if(i==0)
                                                                {
                                                                    chatreqref.child(currentuserid).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        chatreqref.child(list_user_id).child(currentuserid)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                    {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext(), "You have cancelled chat request", Toast.LENGTH_SHORT).show();

                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                                        builder.show();


                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public Requestviewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        Requestviewholder holder=new Requestviewholder(view);
                        return holder;
                    }
                };

        myrequestlist.setAdapter(adapter);
        adapter.startListening();
    }

    public static class Requestviewholder extends RecyclerView.ViewHolder
    {

        TextView userName,userStatus;
        CircleImageView profileImage;
        Button AcceptButton,CancelButton;

        public Requestviewholder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            AcceptButton=itemView.findViewById(R.id.request_accept_button);
            CancelButton=itemView.findViewById(R.id.request_cancel_button);
        }
    }
}
