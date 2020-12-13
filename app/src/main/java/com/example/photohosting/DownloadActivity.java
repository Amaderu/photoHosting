package com.example.photohosting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadActivity extends AppCompatActivity {
    String TAG = "Create folder";
    ImageView imageView;
    Button btnDelete, btnDownload;
    private Uri filePath;
    private String folderPath;
    private String userId;
    private String fileName;

    /*private final int PICK_IMAGE_REQUEST = 71;*/
    private FirebaseStorage storage;
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        init();
    }

    private void init() {
        //search btn on view
        imageView = (ImageView) findViewById(R.id.imageUpload);
        btnDelete = (Button) findViewById(R.id.btnSelect);
        btnDownload = (Button) findViewById(R.id.btnUpload);

        //getInstance database and storage
        storage = FirebaseStorage.getInstance();
        mStorageReference = storage.getReference();
        //fields
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        fileName = intent.getStringExtra("downloadingFile");

        //Button actions

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImageFromCloud();
            }
        });
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImageToExternal();
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
        createDirectoryAndSaveFile();
    }

    //delete
    private void deleteImageFromCloud() {
        // Create a reference to the file to delete
        StorageReference imageRef = mStorageReference.child("images").child(userId).child(fileName);

        // Delete the file
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(DownloadActivity.this, "Successful delete image from cloud", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(DownloadActivity.this, "Failed delete image from cloud", Toast.LENGTH_LONG).show();

            }
        });
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /*Create directory to save files*/
    private void createDirectoryAndSaveFile() {
        //Danger premission Error
        if (!isExternalStorageWritable()) Log.d(TAG, "not Writable");
        if (!isExternalStorageReadable()) Log.d(TAG, "not Readable");
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath + "/Pictures/photoHosting");

        if (!file.exists()) {
            Log.d(TAG, "not exists");
            if (!file.mkdirs()) Log.d("Folder", "not Created");
            else {
                Log.d(TAG, "Created");
                folderPath = file.getPath();
                Log.d(TAG, folderPath);

            }
        } else Log.d(TAG, "exists");
        //file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg";

    }

    //download to cache file
    private void downloadImageToExternal() {
        final StorageReference downloadedFileRef = mStorageReference.child("images").child(userId).child(fileName);
        //local file
        //final File localFile = new File(DownloadActivity.this.getFilesDir(), fileName);
        final File localFile = new File(folderPath, fileName);
        Log.d(TAG,"Download to: "+localFile.getAbsolutePath());
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Download...");
        progressDialog.show();

        downloadedFileRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        //Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                        //Не нужно показывать
                        Picasso.get()
                                .load(localFile)
                                //.resize(imageView.getWidth(), imageView.getHeight())
                                .fit()
                                .centerInside()
                                .into(imageView);

                        //localFile.deleteOnExit();
                        Toast.makeText(DownloadActivity.this, "file " + fileName + " download to\n" + localFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("Error download", "error: " + e.toString());
                Picasso.get()
                        .load(R.drawable.ic_error)
                        .fit()
                        .centerInside()
                        .into(imageView);
                Toast.makeText(DownloadActivity.this, "file " + fileName + " not downloaded!", Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                        .getTotalByteCount());
                progressDialog.setMessage("Downloaded " + (int) progress + "%");
            }
        });
    }


    private void deleteImageFromCache() {
        if (!deleteFile("images.jpg")) {
            Toast.makeText(this, "Successful delete image from cache", Toast.LENGTH_LONG).show();
        } else Toast.makeText(this, "Failed delete image from cache", Toast.LENGTH_LONG).show();
    }

    //download to cache file
    private void downloadImage() {
        try {
            final StorageReference downloadedFileRef = mStorageReference.child("images").child(userId).child(fileName);
            //cache file
            final File localFile = File.createTempFile("images", "jpg");
            //final File localFile = new File(DownloadActivity.this.getFilesDir(), fileName);
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Download...");
            progressDialog.show();

            downloadedFileRef.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            //vtyzk
                            //Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                            Picasso.get()
                                    .load(localFile)
                                    //.resize(imageView.getWidth(), imageView.getHeight())
                                    .fit()
                                    .centerInside()
                                    .into(imageView);

                            //localFile.deleteOnExit();
                            Toast.makeText(DownloadActivity.this, "file " + fileName + " download to\n" + localFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Log.e("Error download", "error: " + e.toString());
                    Picasso.get()
                            .load(R.drawable.ic_error)
                            .resize(150, 150)
                            .centerCrop()
                            .into(imageView);
                    Toast.makeText(DownloadActivity.this, "file " + fileName + " not downloaded!", Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                            .getTotalByteCount());
                    progressDialog.setMessage("Downloaded " + (int) progress + "%");
                }
            });
        } catch (IOException e) {
        }
    }
}