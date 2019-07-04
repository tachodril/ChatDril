package com.example.ritik.chatdril;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private RecyclerView findfriendsrecyclerlist;
    private DatabaseReference usersref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        usersref= FirebaseDatabase.getInstance().getReference().child("Users");


        findfriendsrecyclerlist=findViewById(R.id.find_frinds_recycler_list);
        findfriendsrecyclerlist.setLayoutManager(new LinearLayoutManager(this));


        mtoolbar=findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(usersref,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,FindfriendsViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, FindfriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindfriendsViewHolder holder, final int position, @NonNull Contacts model)
            {
                holder.username.setText(model.getName());
                holder.userstatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileimage);



                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_uer_id=getRef(position).getKey();

                        Intent profileintent=new Intent(FindFriendsActivity.this,ProfileActivity.class);
                        profileintent.putExtra("visit_user_id",visit_uer_id);
                        startActivity(profileintent);
                    }
                });
            }

            @NonNull
            @Override
            public FindfriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                FindfriendsViewHolder viewHolder=new FindfriendsViewHolder(view);
                return viewHolder;
            }
        };


        findfriendsrecyclerlist.setAdapter(adapter);

        adapter.startListening();
    }




    public static class FindfriendsViewHolder extends RecyclerView.ViewHolder
    {

        TextView username,userstatus;
        CircleImageView profileimage;

        public FindfriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            username=itemView.findViewById(R.id.user_profile_name);
            userstatus=itemView.findViewById(R.id.user_status);
            profileimage=itemView.findViewById(R.id.users_profile_image);
        }
    }
}
