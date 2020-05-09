package umn.ac.id.tugasmobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static umn.ac.id.tugasmobile.SetupActivity.Gallery_Pick;

public class EditProfileActivity extends AppCompatActivity {

    private Button cancel,save;

    private EditText fullname,username,desc,location;
    private CircleImageView profilepic;
    private ProgressDialog loadingBar;

    private DatabaseReference SettingUserRef;
    private FirebaseAuth mAuth;
    private StorageReference userProfileImageRef;

    private String currentUserId;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        cancel = findViewById(R.id.cancelbutton);
        save = findViewById(R.id.savebutton);

        fullname = (EditText) findViewById(R.id.editname);
        username = (EditText) findViewById(R.id.editUsername);
        desc = (EditText) findViewById(R.id.editbio);
        location = (EditText) findViewById(R.id.editlocation);
        profilepic = (CircleImageView) findViewById(R.id.ProfileImage);
        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        SettingUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateAccountInfo();
            }
        });

        SettingUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String myprofilepic = dataSnapshot.child("profileimage").getValue().toString();
                    String mylocation = dataSnapshot.child("location").getValue().toString();
                    String mydesc = dataSnapshot.child("desc").getValue().toString();
                    String myfullname = dataSnapshot.child("fullname").getValue().toString();
                    String myusername = dataSnapshot.child("username").getValue().toString();

                    Picasso.get().load(myprofilepic).placeholder(R.drawable.profile).into(profilepic);

                    username.setText(myusername);
                    fullname.setText(myfullname);
                    desc.setText(mydesc);
                    location.setText(mylocation);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // some conditions for the picture
        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();
            // crop the image
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        // Get the cropped image
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {       // store the cropped image into result
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri = result.getUri();

                final StorageReference filePath = userProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                SettingUserRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            onBackPressed();
                                            Toast.makeText(EditProfileActivity.this, "Image Stored", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(EditProfileActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                            }

                        });

                    }

                });
            }
            else
            {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }


    private void ValidateAccountInfo() {
        String myusername = username.getText().toString();
        String myfullname = fullname.getText().toString();
        String mydesc = desc.getText().toString();
        String mylocation = location.getText().toString();

        if (TextUtils.isEmpty(myusername))
        {
            Toast.makeText(this, "Please write your username...",Toast.LENGTH_SHORT).show();

        }
        else if (TextUtils.isEmpty(myfullname))
        {
            Toast.makeText(this, "Please write your Full name...",Toast.LENGTH_SHORT).show();

        }
        else if (TextUtils.isEmpty(mydesc))
        {
            Toast.makeText(this, "Please write your Bio...",Toast.LENGTH_SHORT).show();

        }
        else if (TextUtils.isEmpty(mylocation))
        {
            Toast.makeText(this, "Please write your Location...",Toast.LENGTH_SHORT).show();

        }
        else
        {
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we updating your profile image...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            UpdateAccountInfo(myfullname, myusername, mydesc, mylocation);
        }

    }

    private void UpdateAccountInfo(String myfullname, String myusername, String mydesc, String mylocation){
        HashMap userMap = new HashMap();
        userMap.put("username", myusername);
        userMap.put("fullname", myfullname);
        userMap.put("desc", mydesc);
        userMap.put("location", mylocation);

        SettingUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful())
                {
                    onBackPressed();
                    Toast.makeText(EditProfileActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
                else {
                    Toast.makeText(EditProfileActivity.this, "Error Occured, while updating profile info", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }
}
