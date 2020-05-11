package umn.ac.id.tugasmobile;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ContentFrameLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ImageView homeImage;

    Boolean LikeChecker = false;

    private RecyclerView postList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, postsRef,likesRef,commentsRef;
    private FirebaseRecyclerAdapter adapter;
    String currentUserID;

    public HomeFragment() {
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
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        commentsRef = FirebaseDatabase.getInstance().getReference().child("Comments");

        postList = (RecyclerView) v.findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        DisplayAllUsersPosts();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        adapter.stopListening();
//    }

    //
    private void DisplayAllUsersPosts()
    {
        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(postsRef, Posts.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            public PostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_post_layout, parent, false);

                return new PostsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(PostsViewHolder viewHolder, int position, Posts model) {
                final String PostKey = getRef(position).getKey();

                viewHolder.setUsername(model.getUsername());
                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setProfileimage(model.getProfileimage());
                viewHolder.setPostimage(model.getPostimage());

                viewHolder.setLikeButtonStatus(PostKey);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent();
                        clickPostIntent.setClass(getActivity(), ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", PostKey);
                        getActivity().startActivity(clickPostIntent);
                    }
                });

                viewHolder.CommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent();
                        commentsIntent.setClass(getActivity(), CommentsActivity.class);
                        commentsIntent.putExtra("PostKey", PostKey);
                        getActivity().startActivity(commentsIntent);
                    }
                });
                viewHolder.LikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        LikeChecker = true;

                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                               if (LikeChecker.equals(true))
                               {
                                   if(dataSnapshot.child(PostKey).hasChild(currentUserID))
                                   {
                                       likesRef.child(PostKey).child(currentUserID).removeValue();
                                       LikeChecker = false;
                                   }
                                   else
                                   {
                                       likesRef.child(PostKey).child(currentUserID).setValue(true);
                                       LikeChecker = false;
                                   }
                               }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        };
        adapter.startListening();
        postList.setAdapter(adapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        ImageButton LikeButton, CommentButton;
        TextView LikesCount;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;
        FirebaseAuth mAuth;

        public PostsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;

            LikeButton = (ImageButton) mView.findViewById(R.id.btnLikes);
            CommentButton = (ImageButton) mView.findViewById(R.id.btnComment);
            LikesCount = (TextView) mView.findViewById(R.id.tvLikesCount);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        public void setLikeButtonStatus(final String postKey)
        {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.child(postKey).hasChild(currentUserId))
                    {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        LikeButton.setImageResource(R.drawable.like);
                        LikesCount.setText(Integer.toString(countLikes));
                    }
                    else
                    {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        LikeButton.setImageResource(R.drawable.dislike);
                        LikesCount.setText(Integer.toString(countLikes));
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError){

                }
            });
        }



        public void setUsername(String username)
        {
            TextView userName = (TextView) mView.findViewById(R.id.post_profile_username);
            userName.setText("@" + username);
        }

        public void setFullname(String fullname)
        {
            TextView fullName = (TextView) mView.findViewById(R.id.post_profile_fullname);
            fullName.setText(fullname);
        }

        public void setProfileimage(String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).into(image);
        }

        public void setTime(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("    " + time);
        }

        public void setDate(String date)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("  -  " + date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(String postimage)
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.get().load(postimage).into(PostImage);
        }


    }

}