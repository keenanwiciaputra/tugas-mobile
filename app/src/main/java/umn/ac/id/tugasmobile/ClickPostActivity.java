package umn.ac.id.tugasmobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ClientCertRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ClickPostActivity extends AppCompatActivity {
    private ImageView editImage;
    private TextView editDesc, editFullname, editUsername;
    private Button btnEdit, btnDelete;

    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;

    private String PostKey, currentUserID, databaseUserID, desc, image, fullname, username;
    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        editFullname = findViewById(R.id.edit_fullname);
        editUsername = findViewById(R.id.edit_username);
        editImage = findViewById(R.id.edit_image);
        editDesc = findViewById(R.id.edit_description);
        btnEdit = findViewById(R.id.btnEditPost);
        btnDelete = findViewById(R.id.btnDeletePost);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostKey = getIntent().getExtras().get("PostKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        btnEdit.setVisibility(View.INVISIBLE);
        btnDelete.setVisibility(View.INVISIBLE);

        registerForContextMenu(editImage);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                DeleteCurrentPost();
            }
        });

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    desc = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();
                    fullname = dataSnapshot.child("fullname").getValue().toString();
                    username = dataSnapshot.child("username").getValue().toString();
                    databaseUserID = dataSnapshot.child("uid").getValue().toString();

                    if(currentUserID.equals(databaseUserID))
                    {
                        btnEdit.setVisibility(View.VISIBLE);
                        btnDelete.setVisibility(View.VISIBLE);
                    }

                    editFullname.setText(fullname);
                    editUsername.setText("@" + username);
                    editDesc.setText(desc);
                    Picasso.get().load(image).into(editImage);

                    btnEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            EditCurrentPost(desc);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void EditCurrentPost(String desc)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post: ");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(desc);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                clickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post updated successfully.", Toast.LENGTH_SHORT).show();
                SendUserToMainActivity();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.color.tosca);
    }

    private void DeleteCurrentPost()
    {
        clickPostRef.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this, "Post has been deleted.", Toast.LENGTH_SHORT).show();
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_save,menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.save_gallery:
            {
                //to get the image from the ImageView (say iv)
//                BitmapDrawable draw = (BitmapDrawable) editImage.getDrawable();
//                Bitmap bitmap = ((BitmapDrawable) editImage.getDrawable()).getBitmap();
//
//                File filepath = Environment.getExternalStorageDirectory();
//                File dir = new File(filepath.getAbsolutePath()+"/Pictures/");
//                dir.mkdir();
//                File file = new File(dir, System.currentTimeMillis()+".jpg");
//                try {
//                    outputStream = new FileOutputStream(file);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//                Toast.makeText(this, "Image Saved ", Toast.LENGTH_SHORT).show();
//                try {
//                    outputStream.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    outputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
            return true;
        }

        return super.onContextItemSelected(item);
    }
}
