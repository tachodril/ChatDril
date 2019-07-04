package com.example.ritik.chatdril;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {


    private View groupfragmentview;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> listofgroups=new ArrayList<>();

    private DatabaseReference groupref;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        groupfragmentview= inflater.inflate(R.layout.fragment_groups, container, false);

        groupref= FirebaseDatabase.getInstance().getReference().child("Groups");


        initialise();

        RetriveandDisplaygroups();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentgroupname=parent.getItemAtPosition(position).toString();

                Intent groupchatintent=new Intent(getContext(),GroupChatActivity.class);
                groupchatintent.putExtra("groupName",currentgroupname);
                startActivity(groupchatintent);
            }
        });
        return groupfragmentview;
    }


    private void initialise() {
        listView=groupfragmentview.findViewById(R.id.list_view);
        arrayAdapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,listofgroups);
        listView.setAdapter(arrayAdapter);

    }

    private void RetriveandDisplaygroups() {
        groupref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Set<String> set=new HashSet<>();
                Iterator iterator=dataSnapshot.getChildren().iterator();

                while(iterator.hasNext())
                {
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }

                listofgroups.clear();
                listofgroups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
