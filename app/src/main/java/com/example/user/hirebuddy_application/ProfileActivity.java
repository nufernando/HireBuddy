package com.example.user.hirebuddy_application;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private Button backButton;
    private String userID;
    private TextView name, email, password;
    private ImageView profilePic;
    private RelativeLayout main1 , main2 , main3, main4;
    private Button main2Back, main3Back, main4Back, changeImage, save1, save2;
    private Animation slideUp;
    private Animation slideOut;
    private EditText name2, email2, password2;
    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);
        password = (TextView) findViewById(R.id.password);
        profilePic = (ImageView) findViewById(R.id.image_frame);
        main1 = (RelativeLayout) findViewById(R.id.main1);
        main2 = (RelativeLayout) findViewById(R.id.main2);
        main3 = (RelativeLayout) findViewById(R.id.main3);
        main4 = (RelativeLayout) findViewById(R.id.main4);
        main2Back = (Button) findViewById(R.id.backbutton2);
        main3Back = (Button) findViewById(R.id.backbutton3);
        main4Back = (Button) findViewById(R.id.backbutton4);
        name2 = (EditText) findViewById(R.id.name2);
        email2 = (EditText) findViewById(R.id.email2);
        password2 = (EditText) findViewById(R.id.password2);
        changeImage = (Button) findViewById(R.id.change_image);
        save1 = (Button) findViewById(R.id.save_name);
        save2 = (Button) findViewById(R.id.save_email);

        // Animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.attribute_slide_up);
        slideOut = AnimationUtils.loadAnimation(this, R.anim.attribute_slide_out);

        getCustomerUsernameDatabaseInstance();
        getCustomerEmailDatabaseInstance();
        getCustomerImageDatabaseInstance();

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main1.setVisibility(View.INVISIBLE);

                main2.setVisibility(View.VISIBLE);
                main2.startAnimation(slideUp);
            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main1.setVisibility(View.INVISIBLE);

                main3.setVisibility(View.VISIBLE);
                main3.startAnimation(slideUp);
            }
        });

        save1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
                rootRef.child("CustomerName").setValue(name2.getText().toString());

                Snackbar.make(v, "Name Updated", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        save2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
                rootRef.child("CustomerEmail").setValue(email2.getText().toString());
            }
        });

        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main1.setVisibility(View.INVISIBLE);

                main4.setVisibility(View.VISIBLE);
                main4.startAnimation(slideUp);
            }
        });

        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });



        backButton = (Button) findViewById(R.id.backbutton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (ProfileActivity.this, CustomerMapActivity.class);
                startActivity(intent);
                finish();
            }
        });

        main2Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main2.setVisibility(View.INVISIBLE);

                main1.setVisibility(View.VISIBLE);
                main1.startAnimation(slideUp);
            }
        });

        main3Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main3.setVisibility(View.INVISIBLE);

                main1.setVisibility(View.VISIBLE);
                main1.startAnimation(slideUp);
            }
        });

        main4Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main4.setVisibility(View.INVISIBLE);

                main1.setVisibility(View.VISIBLE);
                main1.startAnimation(slideUp);
            }
        });
    }

    private void getCustomerImageDatabaseInstance() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        DatabaseReference imageDatabaseRef = rootRef.child("CustomerProfileImage");
        imageDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Picasso.get().load(dataSnapshot.getValue().toString()).into(profilePic);
                    profilePic.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void getCustomerEmailDatabaseInstance() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        DatabaseReference emailDatabaseRef = rootRef.child("CustomerEmail");
        emailDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    email.setText(dataSnapshot.getValue().toString());
                    email2.setText(dataSnapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void getCustomerUsernameDatabaseInstance() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        DatabaseReference usernameDatabaseRef = rootRef.child("CustomerName");
        usernameDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    name.setText(dataSnapshot.getValue().toString());
                    name2.setText(dataSnapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void selectImage(){
        final CharSequence[] items ={"Camera", "Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Import Profile Picture");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(items[i].equals("Camera")){
                    Intent intent1 = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent1, 0);
                }else if(items[i].equals("Gallery")){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Select File"), 1);
                }else if(items[i].equals("Cancel")){
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == 0){
                StorageReference filePath = FirebaseStorage.getInstance().getReference()
                        .child("profile_images").child(userID);

                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                profilePic.setImageBitmap(bitmap);
                profilePic.setScaleType(ImageView.ScaleType.CENTER_CROP);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos );
                byte[] data1 = baos.toByteArray();
                UploadTask uploadTask = filePath.putBytes(data1);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(ProfileActivity.this,"Upload Failure, Please Try Agian." , Toast.LENGTH_SHORT).show();

                        finish();
                        return;
                    }
                });

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(ProfileActivity.this,"Upload Successfull" , Toast.LENGTH_SHORT).show();

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                        Uri downloadUrl = urlTask.getResult();

                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
                        rootRef.child("CustomerProfileImage").setValue(downloadUrl.toString());

                        finish();
                    }
                });

            }else if(requestCode == 1){
                final Uri selectedImageUri = data.getData();
                resultUri = selectedImageUri;
                profilePic.setImageURI(selectedImageUri);
                profilePic.setScaleType(ImageView.ScaleType.CENTER_CROP);

                if(resultUri != null){
                    StorageReference filePath = FirebaseStorage.getInstance().getReference()
                            .child("profile_images").child(userID);
                    Bitmap bitmap = null;
                    try{
                        bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos );
                    byte[] data1 = baos.toByteArray();
                    UploadTask uploadTask = filePath.putBytes(data1);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(ProfileActivity.this,"Upload Failure, Please Try Agian." , Toast.LENGTH_SHORT).show();

                            finish();
                            return;
                        }
                    });

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(ProfileActivity.this,"Upload Successfull" , Toast.LENGTH_SHORT).show();

                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            Uri downloadUrl = urlTask.getResult();

                            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
                            rootRef.child("CustomerProfileImage").setValue(downloadUrl.toString());

                            finish();
                        }
                    });
                }
            }
        }
    }
}
