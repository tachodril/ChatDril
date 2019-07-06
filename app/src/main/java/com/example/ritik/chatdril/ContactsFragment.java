package com.example.ritik.chatdril;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
public class ContactsFragment extends Fragment {

    private View contactsview;
    private RecyclerView mycontactlist;

    private DatabaseReference contactsref,usersref;
    private FirebaseAuth mauth;
    private String currentuserid;


    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsview= inflater.inflate(R.layout.fragment_contacts, container, false);

        mycontactlist=contactsview.findViewById(R.id.contacts_list);
        mycontactlist.setLayoutManager(new LinearLayoutManager(getContext()));

        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();

        contactsref= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentuserid);

        usersref=FirebaseDatabase.getInstance().getReference().child("Users");





        return contactsview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsref,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,Contactsviewholder> adapter
        =new FirebaseRecyclerAdapter<Contacts, Contactsviewholder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final Contactsviewholder holder, int position, @NonNull Contacts model)
            {
                String userids=getRef(position).getKey();

                usersref.child(userids).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {

                            if(dataSnapshot.child("userState").hasChild("state"))
                            {
                                String state= dataSnapshot.child("userState").child("state").getValue().toString();
                                String date= dataSnapshot.child("userState").child("date").getValue().toString();
                                String time= dataSnapshot.child("userState").child("time").getValue().toString();

                                if(state.equals("online"))
                                {
                                    holder.onlineicon.setVisibility(View.VISIBLE);
                                }
                                else if(state.equals("offline"))
                                {
                                    holder.onlineicon.setVisibility(View.INVISIBLE);
                                }
                            }
                            else
                            {
                                holder.onlineicon.setVisibility(View.INVISIBLE);
                            }

                            if(dataSnapshot.hasChild("image"))
                            {
                                String userImage=dataSnapshot.child("image").getValue().toString();
                                String profileName=dataSnapshot.child("name").getValue().toString();
                                String profileStatus=dataSnapshot.child("status").getValue().toString();

                                holder.username.setText(profileName);
                                holder.userstatus.setText(profileStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileimage);

                            }
                            else
                            {
                                String profileName=dataSnapshot.child("name").getValue().toString();
                                String profileStatus=dataSnapshot.child("status").getValue().toString();

                                holder.username.setText(profileName);
                                holder.userstatus.setText(profileStatus);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public Contactsviewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                Contactsviewholder viewholder=new Contactsviewholder(view);
                return viewholder;
            }
        };

        mycontactlist.setAdapter(adapter);
        adapter.startListening();
    }


    public static class Contactsviewholder extends RecyclerView.ViewHolder
    {

        TextView username,userstatus;
        CircleImageView profileimage;
        ImageView onlineicon;

        public Contactsviewholder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_profile_name);
            userstatus=itemView.findViewById(R.id.user_status);
            profileimage=itemView.findViewById(R.id.users_profile_image);
            onlineicon=itemView.findViewById(R.id.user_online_status);
        }
    }
}
