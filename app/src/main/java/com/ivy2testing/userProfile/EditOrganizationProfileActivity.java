package com.ivy2testing.userProfile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.User;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.ImageUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditOrganizationProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditOrganizationProfileActivity";


    // MARK: Variables

    private CircleImageView profile_image;
    private TextView change_pic_button;
    private EditText name_edittext;
    private Button bafe_sutton;
    private ProgressBar progress_bar;
    private User this_user;
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();
    private FirebaseFirestore base_database_ref = FirebaseFirestore.getInstance();
    private boolean prof_pic_changed = false;
    private byte[] preview_bytes;
    private byte[] standard_bytes;





    // MARK: Base Methods

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_organizationprofile);
        declareHandles();
        setUpActivity();
    }

    private void declareHandles(){
        profile_image = findViewById(R.id.edit_org_img);
        name_edittext = findViewById(R.id.edit_org_name);
        change_pic_button = findViewById(R.id.edit_org_change);
        bafe_sutton = findViewById(R.id.edit_org_saveButton);
        progress_bar = findViewById(R.id.edit_org_progressBar);
        setTitle(getString(R.string.editProfile));
    }

    private void setUpActivity(){
        this_user = getIntent().getParcelableExtra("this_user");
        if(this_user != null){
            name_edittext.setText(this_user.getName());
            name_edittext.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(nameOk()) bafe_sutton.setEnabled(true);
                    else bafe_sutton.setEnabled(false);
                }
                @Override
                public void afterTextChanged(Editable editable) { }
            });
            String profPicPath = "userfiles/"+this_user.getId()+"/profileimage.jpg";
            try {
                base_storage_ref.child(profPicPath).getDownloadUrl().addOnCompleteListener(uriTask -> {
                    if (uriTask.isSuccessful() && uriTask.getResult() != null)
                        Glide.with(this).load(uriTask.getResult()).into(profile_image);
                });
            } catch (Exception e) {
                Log.w(TAG, "StorageException! No Preview Image for this user.");
            }
            change_pic_button.setOnClickListener(view -> changeProfPic());
            bafe_sutton.setOnClickListener(view -> saveChanges());
        }
    }

    private void saveChanges(){
        startLoading();
        boolean nameChanged = !name_edittext.getText().toString().equals(this_user.getName());
        base_database_ref.collection("users").document(this_user.getId()).update("name", name_edittext.getText().toString()).addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               this_user.setName(name_edittext.getText().toString());
               if(nameChanged) updatePosts();
               if(prof_pic_changed && standard_bytes != null && preview_bytes != null){
                   String profPicPath = "userfiles/" + this_user.getId() + "/profileimage.jpg";
                   String previewPath = "userfiles/" + this_user.getId() + "/previewimage.jpg";
                   base_storage_ref.child(profPicPath).putBytes(standard_bytes).addOnCompleteListener(task1 -> {
                       if (task1.isSuccessful()) {
                           base_storage_ref.child(previewPath).putBytes(preview_bytes).addOnCompleteListener(task2 -> {
                               if(task2.isSuccessful()){
                                   endLoading();
                                   returnToMain();
                               } else{
                                   Toast.makeText(this, "Failed to save image. :-(", Toast.LENGTH_LONG).show();
                                   endLoading();
                               }
                           });
                       } else{
                           Toast.makeText(this, "Failed to save image. :-(", Toast.LENGTH_LONG).show();
                           endLoading();
                       }
                   });
               }else{
                   endLoading();
                   returnToMain();
               }
           }else{
               Toast.makeText(this, "Failed to save name. :-(", Toast.LENGTH_LONG).show();
               endLoading();
           }
        });
    }

    private void updatePosts(){// Update posts associated with org if org name has changed
        String address = "universities/" + this_user.getUni_domain() + "/posts";
        base_database_ref.collection(address).whereEqualTo("author_id", this_user.getId())
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                for (DocumentSnapshot doc : task.getResult())
                    doc.getReference().update("author_name",this_user.getName());
            }
        });
    }

    private boolean nameOk() {
        return !name_edittext.getText().toString().trim().isEmpty();
    }

    private void startLoading(){
        bafe_sutton.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);
    }

    private void endLoading(){
        progress_bar.setVisibility(View.GONE);
        bafe_sutton.setVisibility(View.VISIBLE);
    }

    private void returnToMain(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }










    // MARK: Profile Pic Methods

    private void changeProfPic(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, Constant.PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            switch(requestCode){
                case Constant.PICK_IMAGE_REQUEST_CODE:
                    if(data != null && data.getData() != null){
                        Uri destinationUri = Uri.fromFile(new File(this.getCacheDir(), "img_" + System.currentTimeMillis()));
                        UCrop.of(data.getData(), destinationUri).withAspectRatio(1,1).withMaxResultSize(ImageUtils.IMAGE_MAX_DIMEN, ImageUtils.IMAGE_MAX_DIMEN).start(this);
                    }
                    break;
                case UCrop.REQUEST_CROP:
                    final Uri resultUri = UCrop.getOutput(data);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        preview_bytes = ImageUtils.compressAndGetPreviewBytes(bitmap);
                        standard_bytes = ImageUtils.compressAndGetBytes(bitmap);
                        Glide.with(this).load(resultUri).into(profile_image);
                        bafe_sutton.setEnabled(true);
                        prof_pic_changed = true;
                    } catch (IOException e) {
                        Toast.makeText(this, "Failed to get image. :-(", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    break;
            }
        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Failed to get image. :-(", Toast.LENGTH_LONG).show();
        }
    }
}
