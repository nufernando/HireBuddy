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
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import common.PredefineMethods;
public class MechanicProfileActivity extends AppCompatActivity {

    private Button backButton, verifyPassword, updatePassword;
    private String userID;
    private TextView name, email, password, profession;
    private ImageView profilePic;
    private RelativeLayout main1 , main2 , main3, main4, main5,
            passwordMenu1, passwordMenu2;
    private Button main2Back, main3Back, main4Back, main4BackB, main5Back,
            changeImage, save1, save2, save5;
    private RadioGroup professionType;
    private Animation slideUp;
    private Animation slideOut;
    private EditText name2, email2, password2, newPassword, newPasswordReEnter;
    private Uri resultUri;
    private String professionTypeValue = "";
    private String CURRENT_USER_EMAIL = "";
    private FirebaseUser user;
    private AuthCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_profile);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();

        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);
        profession = (TextView) findViewById(R.id.profession);
        password = (TextView) findViewById(R.id.password);
        profilePic = (ImageView) findViewById(R.id.image_frame);
        main1 = (RelativeLayout) findViewById(R.id.main1);
        main2 = (RelativeLayout) findViewById(R.id.main2);
        main3 = (RelativeLayout) findViewById(R.id.main3);
        main4 = (RelativeLayout) findViewById(R.id.main4);
        main5 = (RelativeLayout) findViewById(R.id.main5);
        main2Back = (Button) findViewById(R.id.backbutton2);
        main3Back = (Button) findViewById(R.id.backbutton3);
        main4Back = (Button) findViewById(R.id.backbutton4);
        main4BackB = (Button) findViewById(R.id.backbutton4b);
        main5Back = (Button) findViewById(R.id.backbutton5);
        name2 = (EditText) findViewById(R.id.name2);
        email2 = (EditText) findViewById(R.id.email2);
        password2 = (EditText) findViewById(R.id.password2);
        verifyPassword = (Button) findViewById(R.id.verify_password);
        changeImage = (Button) findViewById(R.id.change_image);
        save1 = (Button) findViewById(R.id.save_name);
        save2 = (Button) findViewById(R.id.save_email);
        save5 = (Button) findViewById(R.id.save_profession);
        professionType = (RadioGroup) findViewById(R.id.mechanicType);
        passwordMenu1 = (RelativeLayout) findViewById(R.id.password_menu1);
        passwordMenu2 = (RelativeLayout) findViewById(R.id.password_menu2);
        newPassword = (EditText) findViewById(R.id.password2b);
        updatePassword = (Button) findViewById(R.id.chnage_password);
        newPasswordReEnter = (EditText) findViewById(R.id.password2ba);

        // Animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.attribute_slide_up);
        slideOut = AnimationUtils.loadAnimation(this, R.anim.attribute_slide_out);

        getMechanicUsernameDatabaseInstance();
        getMechanicEmailDatabaseInstance();
        getMechanicImageDatabaseInstance();
        getMechanicProfessionDatabaseInstance();

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

                Toast.makeText(MechanicProfileActivity.this,"Edit feature is unavailable in current version" , Toast.LENGTH_SHORT).show();
                /*main1.setVisibility(View.INVISIBLE);

                main3.setVisibility(View.VISIBLE);
                main3.startAnimation(slideUp);*/
            }
        });

        profession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main1.setVisibility(View.INVISIBLE);

                main5.setVisibility(View.VISIBLE);
                main5.startAnimation(slideUp);
            }
        });

        save1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);
                rootRef.child("MechanicName").setValue(name2.getText().toString());

                Snackbar.make(v, "Name Updated", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        save2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);
                rootRef.child("MechanicEmail").setValue(email2.getText().toString());
            }
        });

        save5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);
                rootRef.child("MechanicType").setValue(professionTypeValue);

                Snackbar.make(v, "Profession Updated", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password2.setText("");
                passwordMenu1.setVisibility(View.VISIBLE);
                main1.setVisibility(View.INVISIBLE);
                main4.setVisibility(View.VISIBLE);
                main4.startAnimation(slideUp);
            }
        });

        verifyPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user_password = password2.getText().toString();
                final String user_email = CURRENT_USER_EMAIL;
                if(TextUtils.isEmpty(user_password)){
                    PredefineMethods.viewToast(MechanicProfileActivity.this,"Please fill all fields and don't submit empty fields.");
                }
                else {
                    credential = EmailAuthProvider.getCredential(user_email,user_password);
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                hideKeyBoard();
                                passwordMenu1.setVisibility(View.INVISIBLE);
                                newPassword.setText("");
                                newPasswordReEnter.setText("");
                                passwordMenu2.setVisibility(View.VISIBLE);
                                passwordMenu2.startAnimation(slideUp);
                                updatePassword.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String user_new_password = newPassword.getText().toString();
                                        String user_ReEnter_Password = newPasswordReEnter.getText().toString();

                                        if(TextUtils.isEmpty(user_new_password) || TextUtils.isEmpty(user_ReEnter_Password)){
                                            PredefineMethods.viewToast(MechanicProfileActivity.this,"Please fill all fields and don't submit empty fields.");
                                        }
                                        else {
                                            if(user_new_password.equals(user_ReEnter_Password)){
                                                user.updatePassword(user_new_password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            hideKeyBoard();
                                                            passwordMenu2.setVisibility(View.INVISIBLE);
                                                            main1.setVisibility(View.VISIBLE);
                                                            main1.startAnimation(slideUp);
                                                            PredefineMethods.viewToast(MechanicProfileActivity.this,"Password Updated");
                                                        }else{
                                                            hideKeyBoard();
                                                            PredefineMethods.viewToast(MechanicProfileActivity.this,"Error Password not Updated.");
                                                        }
                                                    }
                                                });
                                            }else {
                                                PredefineMethods.viewToast(MechanicProfileActivity.this,"Passwords Mismatch");
                                            }
                                        }
                                    }
                                });
                            }else
                            {
                                PredefineMethods.viewToast(MechanicProfileActivity.this,"The password you've entered is incorrect.");
                            }
                        }
                    });

                }
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
                Intent intent = new Intent (MechanicProfileActivity.this, MechanicMapActivity.class);
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

        main4BackB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordMenu2.setVisibility(View.INVISIBLE);

                passwordMenu1.setVisibility(View.VISIBLE);
                passwordMenu1.startAnimation(slideUp);
            }
        });

        main5Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main5.setVisibility(View.INVISIBLE);

                main1.setVisibility(View.VISIBLE);
                main1.startAnimation(slideUp);
            }
        });
    }

    private void hideKeyBoard() {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getMechanicProfessionDatabaseInstance() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);
        DatabaseReference professionDatabaseRef = rootRef.child("MechanicType");
        professionDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    profession.setText(dataSnapshot.getValue().toString());
                    professionTypeValue = dataSnapshot.getValue().toString();
                    if(dataSnapshot.getValue().toString().equals("Mechanic")){
                        professionType.check(R.id.radio_mechanic);
                    }else if(dataSnapshot.getValue().toString().equals("Technician")){
                        professionType.check(R.id.radio_technician);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void getMechanicImageDatabaseInstance() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);
        DatabaseReference imageDatabaseRef = rootRef.child("MechanicProfileImage");
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

    private void getMechanicEmailDatabaseInstance() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);
        DatabaseReference emailDatabaseRef = rootRef.child("MechanicEmail");
        emailDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    CURRENT_USER_EMAIL = dataSnapshot.getValue().toString();
                    email.setText(dataSnapshot.getValue().toString());
                    email2.setText(dataSnapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void getMechanicUsernameDatabaseInstance() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);
        DatabaseReference usernameDatabaseRef = rootRef.child("MechanicName");
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
        AlertDialog.Builder builder = new AlertDialog.Builder(MechanicProfileActivity.this);
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

                        Toast.makeText(MechanicProfileActivity.this,"Upload Failure, Please Try Agian." , Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                });

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(MechanicProfileActivity.this,"Upload Successfull" , Toast.LENGTH_SHORT).show();

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                        Uri downloadUrl = urlTask.getResult();

                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);
                        rootRef.child("MechanicProfileImage").setValue(downloadUrl.toString());

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

                            Toast.makeText(MechanicProfileActivity.this,"Upload Failure, Please Try Agian." , Toast.LENGTH_SHORT).show();

                            finish();
                            return;
                        }
                    });

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(MechanicProfileActivity.this,"Upload Successfull" , Toast.LENGTH_SHORT).show();

                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            Uri downloadUrl = urlTask.getResult();

                            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);
                            rootRef.child("MechanicProfileImage").setValue(downloadUrl.toString());

                            finish();
                        }
                    });
                }
            }
        }
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.radio_mechanic:
                if (checked)
                    professionTypeValue = "Mechanic";
                break;
            case R.id.radio_technician:
                if (checked)
                    professionTypeValue = "Technician";
                break;
        }
    }
}
