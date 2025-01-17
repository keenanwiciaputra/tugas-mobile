package umn.ac.id.tugasmobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.Sampler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {
    TextView cancelPost;
    private CircleImageView profilePicture;
    private ImageButton openCamera, openGallery;
    private Button btnPost;
    private EditText postDesc;
    private Switch btnSwitch;
    private static final int Gallery_Pick = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int pic_id = 321;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 123;
    private double longitude, latitude;
    private String cityName = null, s;

    private Uri imageUri, resultUri;
    private ImageView postImage;
    private String desc, saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl;
    private ProgressDialog loadingBar;

    private DatabaseReference usersRef, postsRef;
    private FirebaseAuth mAuth;
    private StorageReference imageRef;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    String currentPhotoPath;
    String currentUserID;

    private long countPosts = 0;

    public PostActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        imageRef = FirebaseStorage.getInstance().getReference();

        loadingBar = new ProgressDialog(this);
        postImage = findViewById(R.id.postImage);
        postDesc = findViewById(R.id.etPost);
        btnPost = findViewById(R.id.btnPost);
        openGallery = findViewById(R.id.insertPhoto);
        btnSwitch = findViewById(R.id.switch_location);

        openCamera = findViewById(R.id.openCamera);
        profilePicture = findViewById(R.id.post_profile);
        cancelPost = findViewById(R.id.cancelPost);


        //LOCATION PERMISSION
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(PostActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
        } else {
            Log.d("PERMISSION", "already granted.");
        }

        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnSwitch.isChecked())
                {
                    Log.d("DEBUG", "GETTING LOCATION");
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(PostActivity.this);
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(PostActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {

                                        cityName = null;
                                        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                                        List<Address> addresses;
                                        try {
                                            addresses = gcd.getFromLocation(location.getLatitude(),
                                                    location.getLongitude(), 1);
                                            if (addresses.size() > 0) {
                                                System.out.println(addresses.get(0).getLocality());
                                                cityName = addresses.get(0).getLocality();
                                            }
                                        }
                                        catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                         s = "My current location is: "
                                                + cityName;
                                        Toast.makeText(PostActivity.this, "" + s, Toast.LENGTH_LONG).show();


//                                        Toast.makeText(PostActivity.this, "Lat: " + location.getLatitude() + "and Long: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                                    }
                                    else {
//                                        TextView tvLoc = findViewById(R.id.tvLocation);
//                                        tvLoc.setText("LOCATION NOT FOUND");
//                                        Log.d("DEBUG", "LOCATION NOT FOUND");
                                        cityName = null;
                                        Toast.makeText(PostActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                                      @Override
                                                      public void onFailure(@NonNull Exception e) {
                                                          Log.d("DEBUG", "LISTENER FAILURE");
                                                      }
                                                  }
                            );
                }
                else
                {

                }
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });

        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenCamera();
            }
        });

        cancelPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profilePicture);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void ValidatePostInfo()
    {
        desc = postDesc.getText().toString();
//
//        if(imageUri == null)
//        {
//
//        }
        if(TextUtils.isEmpty(desc))
        {
            Toast.makeText(this, "Caption can't be empty. Write something!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Add New Pots");
            loadingBar.setMessage("Please wait, while we are adding your new post. . .");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            SavingImageInformation();
        }
    }

    private void SavingImageInformation()
    {
        Calendar getDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(getDate.getTime());

        Calendar getTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(getTime.getTime());

        Random rand = new Random();
        int randPost = rand.nextInt(1000);

        postRandomName = saveCurrentDate + saveCurrentTime + randPost;

        final StorageReference filepath = imageRef.child("Post Images").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");

        filepath.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return filepath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task)
            {
                if(task.isSuccessful())
                {
                    Uri downUri = task.getResult();

                    downloadUrl = downUri.toString();
                    Toast.makeText(PostActivity.this, "Posted successfully!", Toast.LENGTH_SHORT).show();
                    SavingPostInformation();

                } else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occured : " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SavingPostInformation()
    {

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    countPosts = dataSnapshot.getChildrenCount();
                }
                else
                {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String userName = dataSnapshot.child("username").getValue().toString();

                    HashMap postsMap = new HashMap();
                        postsMap.put("uid", currentUserID);
                        postsMap.put("date", saveCurrentDate);
                        postsMap.put("time", saveCurrentTime);
                        postsMap.put("description", desc);
                        postsMap.put("postimage", downloadUrl);
                        postsMap.put("profileimage", userProfileImage);
                        postsMap.put("fullname", userFullName);
                        postsMap.put("username", userName);
                        postsMap.put("location", cityName);
                        postsMap.put("counter", countPosts);

                    postsRef.child(currentUserID + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        SendUserToMainActivity();
                                        Toast.makeText(PostActivity.this, "New post is updated successfully!", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else
                                    {
                                        Toast.makeText(PostActivity.this, "Error occured while updating your post.", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
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

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    private void OpenCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = postImage.getWidth();
        int targetH = postImage.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        postImage.setImageBitmap(bitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode == RESULT_OK && data!=null)
        {
            imageUri = data.getData();
            postImage.setImageURI(imageUri);
        }

        if(requestCode == REQUEST_IMAGE_CAPTURE)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                File f = new File(currentPhotoPath);
                postImage.setImageURI(Uri.fromFile(f));

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                imageUri = Uri.fromFile(f);
                mediaScanIntent.setData(imageUri);
                this.sendBroadcast(mediaScanIntent);

            }
        }
    }
}
