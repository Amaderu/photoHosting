package com.example.photohosting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    //list
    int pageCount=0;
    ArrayList<String> listItems;
    ArrayAdapter<String> arrayAdapter;
    //refs
    List<StorageReference> prefixes;
    List<StorageReference> items;
    String page;
    //View
    ImageView imageView;
    /*Button btnSelect, btnUpload;
    private final int PICK_IMAGE_REQUEST = 71;*/
    Button btnSignOut, btnSearch, btnToUpload, btnSync;
    ListView listView;
    private Uri filePath;
    private String userId;
    Intent intent;
    List<String> listTemp;

    /*private FirebaseAuth mAuth;
    private FirebaseUser cUser;*/

    private FirebaseStorage storage;
    private StorageReference mStorageReference;
    private FirebaseDatabase mDataBase;
    private DatabaseReference mDataBaseReference;
    private String USER_KEY = "Users";
    String searchFName = "island";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        listView = (ListView) findViewById(R.id.listView);
        //не нужная часть адаптера
        listItems= new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1,
                listItems);
        listView.setAdapter(arrayAdapter);
        setOnClickItem();

        //search btn on view
        imageView = (ImageView) findViewById(R.id.imageView);


        btnSignOut = (Button) findViewById(R.id.btnSignOut);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnToUpload = (Button) findViewById(R.id.btnToUpload);
        btnSync = (Button) findViewById(R.id.btnSync);

        //getInstance database and storage
        storage = FirebaseStorage.getInstance();
        mStorageReference = storage.getReference();
        mDataBase = FirebaseDatabase.getInstance();
        mDataBaseReference = mDataBase.getReference(USER_KEY);
        //Button actions
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page=null;
                pageCount=0;
                listItems.clear();
                imageListAllPaginated(page,searchFName);
            }
        });
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountSignOut();
            }
        });
        btnToUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toUploadActivity();
            }
        });
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page=null;
                pageCount=0;
                listItems.clear();
                imageListAllPaginated(page);
            }
        });
        /*btnSelect.setOnClickListener(new View.OnClickListener() {
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
        //загрузка превращается в скачку
        btnUpload.setText("Download Image");
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImage();
            }
        });*/

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

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
    private void setOnClickItem(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toDownloadActivity(listItems.get(position));
            }
        });
    }

    private void toUploadActivity(){
        intent = new Intent(this,UploadActivity.class);
        intent.putExtra("userId",userId);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    private void toDownloadActivity(String imageName){
        intent = new Intent(this,DownloadActivity.class);
        intent.putExtra("userId",userId);
        intent.putExtra("downloadingFile",imageName);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
    private void toDownloadActivity(){
        intent = new Intent(this,DownloadActivity.class);
        intent.putExtra("userId",userId);
        intent.putExtra("downloadingFile","island.png");

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
    public void imageListAllPaginated(@Nullable String pageToken,String searchFName) {
        StorageReference listRef = mStorageReference.child("images").child(userId);

        Task<ListResult> listPageTask = pageToken != null
                ? listRef.list(10, pageToken)
                : listRef.list(10);

        listPageTask
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        List<StorageReference> prefixes = listResult.getPrefixes();
                        List<StorageReference> items = listResult.getItems();
                        pageCount+=1;

                        // Process page of results
                        Toast.makeText(MainActivity.this, "Page "+pageCount, Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainActivity.this, "Page token: "+pageToken, Toast.LENGTH_SHORT).show();
                        //Toast.makeText(MainActivity.this, "Prefixes", Toast.LENGTH_SHORT).show();
                       /* for (StorageReference prefix : prefixes) {
                            Toast.makeText(MainActivity.this, prefix.toString(), Toast.LENGTH_SHORT).show();
                        }*/
                        //Toast.makeText(MainActivity.this, "Items", Toast.LENGTH_SHORT).show();

                        for (StorageReference item : items) {
                            if(!listItems.contains(item.getName())&&item.getName().matches("^.*"+searchFName+".*$")) listItems.add(item.getName());
                            //Toast.makeText(MainActivity.this, item.toString(), Toast.LENGTH_LONG).show();
                        }

                        // Recurse onto next page
                        if (listResult.getPageToken() != null) {
                            imageListAllPaginated(listResult.getPageToken());
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Uh-oh, an error occurred.
                Toast.makeText(MainActivity.this, "Uh-oh, an error occurred.", Toast.LENGTH_SHORT).show();
                Log.e("Error ImageList",e.toString());
            }
        });
    }

    public void imageListAllPaginated(@Nullable String pageToken) {
        StorageReference listRef = mStorageReference.child("images").child(userId);

        Task<ListResult> listPageTask = pageToken != null
                ? listRef.list(10, pageToken)
                : listRef.list(10);

        listPageTask
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        List<StorageReference> prefixes = listResult.getPrefixes();
                        List<StorageReference> items = listResult.getItems();
                        pageCount+=1;

                        // Process page of results
                        Toast.makeText(MainActivity.this, "Page "+pageCount, Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainActivity.this, "Page token: "+pageToken, Toast.LENGTH_SHORT).show();
                        //Toast.makeText(MainActivity.this, "Prefixes", Toast.LENGTH_SHORT).show();
                       /* for (StorageReference prefix : prefixes) {
                            Toast.makeText(MainActivity.this, prefix.toString(), Toast.LENGTH_SHORT).show();
                        }*/
                        //Toast.makeText(MainActivity.this, "Items", Toast.LENGTH_SHORT).show();

                        for (StorageReference item : items) {
                            if(!listItems.contains(item.getName())) listItems.add(item.getName());
                            //Toast.makeText(MainActivity.this, item.toString(), Toast.LENGTH_LONG).show();
                        }

                        // Recurse onto next page
                        if (listResult.getPageToken() != null) {
                            imageListAllPaginated(listResult.getPageToken());
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Uh-oh, an error occurred.
                Toast.makeText(MainActivity.this, "Uh-oh, an error occurred.", Toast.LENGTH_SHORT).show();
                Log.e("Error ImageList",e.toString());
            }
        });
    }

    //delete file from cloud storage
    private void deleteImageFromCloud() {
        StorageReference storageRef = storage.getReference();

        // Create a reference to the file to delete
        StorageReference desertRef = storageRef.child("images").child(userId).child("desert.jpg");

        // Delete the file
        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });
    }

    /*//download to file
    private void downloadImage() {
        try {
            String fileName = "island.jpg";
            final StorageReference downloadedFileRef = mStorageReference.child("images").child(userId).child(fileName);
            //cache file
            final File localFile = File.createTempFile("images", "jpg");
            //final File localFile = new File(Context.getApplicationContext.getFilesDir(), fileName);
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
                            Toast.makeText(MainActivity.this, "file " + fileName + " download to\n" + localFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainActivity.this, "file " + fileName + " not downloaded!", Toast.LENGTH_LONG).show();
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
    }*/
    /*private void uploadFile(){
        Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
        StorageReference riversRef = storageRef.child("images/rivers.jpg");

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                });
    }
    private void downloadFile(){
        File localFile = File.createTempFile("images", "jpg");
        riversRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Successfully downloaded data to local file
                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // ...
            }
        });
    }*/

    /*private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                imageView.setImageResource(R.drawable.ic_error);
                e.printStackTrace();
            }
        }
    }*/

    public void accountSignOut() {
        showDialog(776);
    }

    protected Dialog onCreateDialog(int id) {
        if (id == 776) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Выход из аккаунта");  // заголовок
            builder.setMessage("Вы действительно хотите выйти?"); // сообщение
            builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    FirebaseAuth.getInstance().signOut();
                    final Intent intent = new Intent(MainActivity.this, Logged_out.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//root
                    startActivity(intent);
                    finish();
                }
            });
            builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Toast.makeText(MainActivity.this, "Отмена выхода", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setCancelable(true);
            return builder.create();
        }
        return super.onCreateDialog(id);
    }

    /*public void uploadImage() {
        if (filePath != null) {
            //check name
            //final String fileName =UUID.randomUUID().toString();
            final String fileName = filePath.getLastPathSegment();

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = mStorageReference.child("images").child(userId).child(fileName);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    }*/
}