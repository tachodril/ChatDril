package com.example.ritik.chatdril;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewholder>
{
    private List<Messages> usermsgslist;
    private FirebaseAuth mauth;
    private DatabaseReference usersref;

    public MessageAdapter(List<Messages> usermsgslist)
    {
        this.usermsgslist=usermsgslist;
    }

    public class MessageViewholder extends RecyclerView.ViewHolder
    {
        public TextView sendermsgtext,receivermsgtext;
        public CircleImageView receiverprofileimage;

        public MessageViewholder(@NonNull View itemView) {
            super(itemView);

            sendermsgtext=itemView.findViewById(R.id.sender_msg_text);
            receivermsgtext=itemView.findViewById(R.id.receiver_msg_text);
            receiverprofileimage=itemView.findViewById(R.id.msg_profile_image);

        }
    }

    @NonNull
    @Override
    public MessageViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view= LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout,viewGroup,false);
        mauth= FirebaseAuth.getInstance();
        return new MessageViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewholder messageViewholder, int i)
    {
        String msgsenderid=mauth.getCurrentUser().getUid();
        Messages messages=usermsgslist.get(i);

        String fromuserid=messages.getFrom();
        String frommsgtype=messages.getType();

        usersref= FirebaseDatabase.getInstance().getReference().child("Users").child(fromuserid);

        usersref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild("image"))
                {
                    String receiverimage=dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverimage).placeholder(R.drawable.profile_image).into(messageViewholder.receiverprofileimage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(frommsgtype.equals("text"))
        {
            messageViewholder.receivermsgtext.setVisibility(View.INVISIBLE);
            messageViewholder.receiverprofileimage.setVisibility(View.INVISIBLE);
            messageViewholder.sendermsgtext.setVisibility(View.INVISIBLE);

            if(fromuserid.equals(msgsenderid))
            {
                messageViewholder.sendermsgtext.setVisibility(View.VISIBLE);
                messageViewholder.sendermsgtext.setBackgroundResource(R.drawable.sender_message_layout);
                messageViewholder.sendermsgtext.setText(messages.getMessage());
            }
            else
            {
                messageViewholder.receivermsgtext.setVisibility(View.VISIBLE);
                messageViewholder.receiverprofileimage.setVisibility(View.VISIBLE);

                messageViewholder.receivermsgtext.setBackgroundResource(R.drawable.receiver_message_layout);
                messageViewholder.receivermsgtext.setText(messages.getMessage());
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return usermsgslist.size();
    }



}
