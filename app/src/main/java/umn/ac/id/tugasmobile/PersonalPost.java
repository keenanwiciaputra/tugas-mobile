package umn.ac.id.tugasmobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalPost extends AppCompatActivity {

    private RecyclerView postList2;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, postsRef,likesRef,commentsRef;
    private FirebaseRecyclerAdapter adapter;
    Boolean LikeChecker = false;
    private String currentUserID, databaseUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        commentsRef = FirebaseDatabase.getInstance().getReference().child("Comments");

        postList2 = (RecyclerView) findViewById(R.id.all_users_post_list2);
        postList2.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList2.setLayoutManager(linearLayoutManager);

        DisplayAllUsersPosts();
    }

    private void DisplayAllUsersPosts()
    {
        Query SortPostDecending = postsRef.orderByChild("counter");

        Query selfPost = postsRef.orderByChild("uid").equalTo(currentUserID);

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(selfPost, Posts.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Posts, HomeFragment.PostsViewHolder>(options) {
            @Override
            public HomeFragment.PostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_post_layout, parent, false);

                return new HomeFragment.PostsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(HomeFragment.PostsViewHolder viewHolder, int position, Posts model) {
                final String PostKey = getRef(position).getKey();

                viewHolder.setUsername(model.getUsername());
                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setProfileimage(model.getProfileimage());
                viewHolder.setPostimage(model.getPostimage());
                viewHolder.setLocation(model.getLocation());

                viewHolder.setLikeButtonStatus(PostKey);


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent();
                        clickPostIntent.setClass(PersonalPost.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", PostKey);
                        startActivity(clickPostIntent);
                    }
                });

                viewHolder.CommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent();
                        commentsIntent.setClass(PersonalPost.this, CommentsActivity.class);
                        commentsIntent.putExtra("PostKey", PostKey);
                        startActivity(commentsIntent);
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
        postList2.setAdapter(adapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        ImageButton LikeButton, CommentButton;
        TextView LikesCount;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;
        FirebaseAuth mAuth;

        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            LikeButton = (ImageButton) mView.findViewById(R.id.btnLikes);
            CommentButton = (ImageButton) mView.findViewById(R.id.btnComment);
            LikesCount = (TextView) mView.findViewById(R.id.tvLikesCount);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        public void setLikeButtonStatus(final String postKey) {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(currentUserId)) {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        LikeButton.setImageResource(R.drawable.like);
                        LikesCount.setText(Integer.toString(countLikes));
                    } else {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        LikeButton.setImageResource(R.drawable.dislike);
                        LikesCount.setText(Integer.toString(countLikes));
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setUsername(String username) {
            TextView userName = (TextView) mView.findViewById(R.id.post_profile_username);
            userName.setText("@" + username);
        }

        public void setFullname(String fullname) {
            TextView fullName = (TextView) mView.findViewById(R.id.post_profile_fullname);
            fullName.setText(fullname);
        }

        public void setProfileimage(String profileimage) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).into(image);
        }

        public void setTime(String time) {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("    " + time);
        }

        public void setDate(String date) {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("  -  " + date);
        }

        public void setDescription(String description) {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(String postimage)
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.get().load(postimage).into(PostImage);
        }

        public void setLocation(String location) {
            TextView PostLocation = (TextView) mView.findViewById(R.id.post_location);
            if(location!=null)
            {
                PostLocation.setText("Posted from " + location);
            }
        }

    }


}
