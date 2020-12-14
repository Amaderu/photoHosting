package com.example.photohosting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public class UploadActivity extends AppCompatActivity {
    ImageView imageView;
    Button btnSelect, btnUpload, btnBack;
    private Uri filePath;
    private String userId;
    private String fileName;

    private final int PICK_IMAGE_REQUEST = 71;
    private FirebaseStorage storage;
    private StorageReference mStorageReference;
    /*private FirebaseDatabase mDataBase;
    private DatabaseReference mDataBaseReference;*/
    private String USER_KEY = "Users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        init();
    }

    private void init() {
        //search btn on view
        imageView = (ImageView) findViewById(R.id.imageUpload);
        btnSelect = (Button) findViewById(R.id.btnSelect);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnBack = (Button) findViewById(R.id.btnBack);

        //getInstance database and storage
        storage = FirebaseStorage.getInstance();
        mStorageReference = storage.getReference();
        //
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        //Button actions
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        //Toast.makeText(this, "User: "+userId, Toast.LENGTH_LONG).show();

        /*Task<Uri> uriTask = mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.e("photoHosting+", "uri: " + uri.toString());
                Toast.makeText(MainActivity.this, uri.toString(), Toast.LENGTH_LONG).show();
                //Handle whatever you're going to do with the URL here
            }
        });*/
    }

    private void chooseImage() {
        final Intent intent = new Intent();

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        /*if(doesPackageExist("com.miui.gallery"))
            intent.setClassName("com.miui.gallery", "com.miui.gallery.activity.");*/
        //startActivityForResult(intent, PICK_IMAGE_REQUEST);
        //com.miui.gallery.open
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();

            Log.d("F_type","filepath "+data.getData());
            Log.d("F_type","fileType "+data.getType());
            Log.d("F_type","Intent PackageName "+ data.getPackage());
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                imageView.setImageResource(R.drawable.ic_error);
                e.printStackTrace();
            }
        }
    }

    public void uploadImage() {
        if (filePath != null) {
            //check name
            final String rawFileName = filePath.getLastPathSegment().replaceFirst("image:\\d*$",UUID.randomUUID().toString()+".jpg");
            if(rawFileName.contains("storage")) {
                fileName = rawFileName.substring(rawFileName.lastIndexOf('/'));
            }
            else fileName = rawFileName;
            Log.d("F_type","fileName "+fileName);
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = mStorageReference.child("images").child(userId).child(fileName);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(UploadActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UploadActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }
}