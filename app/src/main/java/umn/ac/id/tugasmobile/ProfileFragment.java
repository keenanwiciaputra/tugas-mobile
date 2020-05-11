package umn.ac.id.tugasmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button btnLogout;
    private Button btnEditProfile;

    private TextView username, fullname, desc, location, join,friends;
    private CircleImageView profilepic;

    private DatabaseReference profileUserRef;
    private FirebaseAuth mAuth;

    private String currentUserId;



    public ProfileFragment() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView clickTextView = (TextView) v.findViewById(R.id.tvFriends);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        username = (TextView) v.findViewById(R.id.tvUserNameP);
        fullname = (TextView) v.findViewById(R.id.tvScreenNameP);
        desc = (TextView) v.findViewById(R.id.tvDescP);
        location = (TextView) v.findViewById(R.id.tvUserLocationP);
        join = (TextView) v.findViewById(R.id.tvJoinP);
        profilepic = (CircleImageView) v.findViewById(R.id.ProfileImage);

        profileUserRef.addValueEventListener(new ValueEventListener() {
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

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        clickTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), FriendsActivity.class);
                getActivity().startActivity(intent);
            }

        });

        btnLogout = v.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnLogout:
                        FirebaseAuth.getInstance().signOut();
                        SendUserToLoginActivity();
                        break;
                }
            }
        });

        return v;
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btnLogout:
//                FirebaseAuth.getInstance().signOut();
//                SendUserToLoginActivity();
//                break;
//        }
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }


}
