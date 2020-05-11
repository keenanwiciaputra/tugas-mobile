package umn.ac.id.tugasmobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import org.w3c.dom.Comment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView CommentList;
    private DatabaseReference commentsRef, postsRef, usersRef;
    private FirebaseAuth mAUth;
    private ImageButton send_comment;
    private EditText comment_input;
    private Button cancel;

    private String Post_Key, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_Key = getIntent().getExtras().get("PostKey").toString();

        mAUth = FirebaseAuth.getInstance();
        currentUserId = mAUth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");


        CommentList = (RecyclerView) findViewById(R.id.comment_list);
        CommentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentList.setLayoutManager(linearLayoutManager);

        comment_input = (EditText) findViewById(R.id.comment_input);
        send_comment = (ImageButton) findViewById(R.id.post_comment);

        cancel = (Button) findViewById(R.id.cancelbutton);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        send_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            String userName = dataSnapshot.child("username").getValue().toString();
                            ValidateComment(userName);

                            comment_input.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });


    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(postsRef, Comments.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Comments,CommentsViewHolder>(options)
        {

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_comments_layout, parent, false);

                return new CommentsActivity.CommentsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder commentsViewHolder, int i, @NonNull Comments comments)
            {
                commentsViewHolder.setUsername(comments.getUsername());
                commentsViewHolder.setComment(comments.getComment());
                commentsViewHolder.setDate(comments.getDate());
                commentsViewHolder.setTime(comments.getTime());
            }

        };
        adapter.startListening();
        CommentList.setAdapter(adapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public CommentsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment(String comment) {
            TextView myComment = (TextView) mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }

        public void setDate(String date) {
            TextView commentsDate = (TextView) mView.findViewById(R.id.comment_date);
            commentsDate.setText("  Date: "+date);
        }

        public void setTime(String time) {
            TextView commentsTime = (TextView) mView.findViewById(R.id.comment_time);
            commentsTime.setText("  Time: "+time);
        }

        public void setUsername(String username) {
            TextView myUsername = (TextView) mView.findViewById(R.id.username);
            myUsername.setText("@"+username+"   ");
        }
    }



    private void ValidateComment(String userName)
    {
        String commentText = comment_input.getText().toString();

        if (TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "please write text to comment...",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar getDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(getDate.getTime());

            Calendar getTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(getTime.getTime());

            final String RandomKey = saveCurrentDate + saveCurrentTime + currentUserId;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid", currentUserId);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", saveCurrentTime);
            commentsMap.put("comment", commentText);
            commentsMap.put("username", userName);

            postsRef.child(RandomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(CommentsActivity.this, "You have commented",Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(CommentsActivity.this, "Error Occured, try again...",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }
}
