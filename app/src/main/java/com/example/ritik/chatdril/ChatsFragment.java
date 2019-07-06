package com.example.ritik.chatdril;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
public class ChatsFragment extends Fragment {

    private View privatechatsview;
    private RecyclerView chatslist;

    private DatabaseReference chatref,usersref;
    private FirebaseAuth mauth;

    private String currentuserid;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privatechatsview= inflater.inflate(R.layout.fragment_chats, container, false);

        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();
        chatref= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentuserid);

        usersref=FirebaseDatabase.getInstance().getReference().child("Users");

        chatslist=privatechatsview.findViewById(R.id.chats_list);
        chatslist.setLayoutManager(new LinearLayoutManager(getContext()));


        return privatechatsview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatref,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,chatsviewholder> adapter=
                new FirebaseRecyclerAdapter<Contacts, chatsviewholder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final chatsviewholder holder, int position, @NonNull Contacts model)
                    {
                        final String userids=getRef(position).getKey();
                        final String[] retimage = new String[1];

                        usersref.child(userids).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    if(dataSnapshot.hasChild("image"))
                                    {
                                        retimage[0] =dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retimage[0]).placeholder(R.drawable.profile_image).into(holder.mprofileImage);
                                    }

                                    final String retname=dataSnapshot.child("name").getValue().toString();
                                    final String retstatus=dataSnapshot.child("status").getValue().toString();

                                    holder.muserName.setText(retname);
                                    holder.muserStatus.setText("Last seen :"+"\n"+"Date "+"Time");

                                    if(dataSnapshot.child("userState").hasChild("state"))
                                    {
                                        String state= dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date= dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time= dataSnapshot.child("userState").child("time").getValue().toString();

                                        if(state.equals("online"))
                                        {
                                            holder.muserStatus.setText("online");
                                        }
                                        else if(state.equals("offline"))
                                        {
                                            holder.muserStatus.setText("Last seen: "+date+" "+time);
                                        }
                                    }
                                    else
                                    {
                                        holder.muserStatus.setText("offline");
                                    }

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            Intent chatintent=new Intent(getContext(),ChatActivity.class);
                                            chatintent.putExtra("visit_user_id",userids);
                                            chatintent.putExtra("visit_user_name",retname);
                                            chatintent.putExtra("visit_image", retimage[0]);

                                            startActivity(chatintent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public chatsviewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        return new chatsviewholder(view);
                    }
                };

        chatslist.setAdapter(adapter);
        adapter.startListening();
    }

    public static class chatsviewholder extends RecyclerView.ViewHolder
    {

        TextView muserName,muserStatus;
        CircleImageView mprofileImage;

        public chatsviewholder(@NonNull View itemView) {
            super(itemView);

            mprofileImage=itemView.findViewById(R.id.users_profile_image);
            muserName=itemView.findViewById(R.id.user_profile_name);
            muserStatus=itemView.findViewById(R.id.user_status);
        }
    }
}
