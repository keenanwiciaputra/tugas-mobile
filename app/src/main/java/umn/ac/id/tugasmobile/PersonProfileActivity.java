package umn.ac.id.tugasmobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView username, fullname, desc, location, join;
    private CircleImageView profilepic;
    private Button cancel, SendRequest,DeclineRequest;

    private DatabaseReference FriendRequestRef,UsersRef,FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        cancel = findViewById(R.id.cancelbutton);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();


        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");


        InitializeFields();

        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String myprofilepic = dataSnapshot.child("profileimage").getValue().toString();
                    String myjoin = dataSnapshot.child("date").getValue().toString();
                    String mylocation = dataSnapshot.child("location").getValue().toString();
                    String mydesc = dataSnapshot.child("desc").getValue().toString();
                    String myfullname = dataSnapshot.child("fullname").getValue().toString();
                    String myusername = dataSnapshot.child("username").getValue().toString();

                    Picasso.get().load(myprofilepic).placeholder(R.drawable.profile).into(profilepic);

                    username.setText("@" + myusername);
                    fullname.setText(myfullname);
                    desc.setText(mydesc);
                    location.setText(mylocation);
                    join.setText("Joined " + myjoin);

                    MaintananceOfButton();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DeclineRequest.setVisibility(View.INVISIBLE);
        DeclineRequest.setEnabled(false);

        if(!senderUserId.equals(receiverUserId))
        {
            SendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendRequest.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends"))
                    {
                        SendFriendRequestToAPerson();
                    }
                    if(CURRENT_STATE.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends"))
                    {
                        UnFriendAnExistingFriend();
                    }


                }
            });
        }
        else
        {
            DeclineRequest.setVisibility(View.INVISIBLE);
            SendRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void UnFriendAnExistingFriend() {
        FriendsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendRequest.setText("Send Friend Request");
                                                SendRequest.setBackgroundResource(R.drawable.border_style);

                                                DeclineRequest.setVisibility(View.INVISIBLE);
                                                DeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest()
    {
        Calendar getDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(getDate.getTime());

        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                FriendRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    FriendRequestRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        SendRequest.setEnabled(true);
                                                                                        CURRENT_STATE = "friends";
                                                                                        SendRequest.setText("Unfriend");

                                                                                        DeclineRequest.setVisibility(View.INVISIBLE);
                                                                                        DeclineRequest.setEnabled(false);
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


    private void CancelFriendRequest()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    FriendRequestRef.child(receiverUserId).child(senderUserId)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendRequest.setEnabled(true);
                                CURRENT_STATE = "not_friends";
                                SendRequest.setText("Send Friend Request");
                                SendRequest.setBackgroundResource(R.drawable.border_style);

                                DeclineRequest.setVisibility(View.INVISIBLE);
                                DeclineRequest.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void MaintananceOfButton ()
    {
        FriendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserId))
                        {
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                            if(request_type.equals("sent"))
                            {
                                CURRENT_STATE ="request_sent";
                                SendRequest.setText("Cancel Friend Request");
                                SendRequest.setBackgroundResource(R.drawable.logout_button);

                                DeclineRequest.setVisibility(View.INVISIBLE);
                                DeclineRequest.setEnabled(false);
                            }
                            else if (request_type.equals("received"))
                            {
                                CURRENT_STATE = "request_received";
                                SendRequest.setText("Accept Friend Request");

                                DeclineRequest.setVisibility(View.VISIBLE);
                                DeclineRequest.setEnabled(true);

                                DeclineRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CancelFriendRequest();
                                    }
                                });
                        }
                        }
                        else {
                            FriendsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserId))
                                            {
                                                CURRENT_STATE = "friends";
                                                SendRequest.setText("Unfriend");

                                                DeclineRequest.setVisibility(View.INVISIBLE);
                                                DeclineRequest.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendFriendRequestToAPerson()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendRequest.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                SendRequest.setText("Cancel Friend Request");
                                                SendRequest.setBackgroundResource(R.drawable.logout_button);

                                                DeclineRequest.setVisibility(View.INVISIBLE);
                                                DeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void InitializeFields() {
        username = (TextView) findViewById(R.id.tvUserNameP);
        fullname = (TextView) findViewById(R.id.tvScreenNameP);
        desc = (TextView) findViewById(R.id.tvDescP);
        location = (TextView) findViewById(R.id.tvUserLocationP);
        join = (TextView) findViewById(R.id.tvJoinP);
        profilepic = (CircleImageView) findViewById(R.id.ProfileImage);

        SendRequest = (Button) findViewById(R.id.btnSendReq);
        DeclineRequest = (Button) findViewById(R.id.btnDeclineReq);

        CURRENT_STATE = "not_friends";

    }
}
