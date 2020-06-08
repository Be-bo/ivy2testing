package com.ivy2testing.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ivy2testing.R;
import com.ivy2testing.entities.Post;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class CreatePost extends AppCompatActivity {

    //toolbar
    private Toolbar post_toolbar;

    //type
    private Button post_button;
    private Button event_button;

    //show in feed
    private Button show_button;
    private Button dont_show_button;

    // import
    private Button from_scratch_button;
    private Button import_button;

    // visual
    private Button nothing_button;
    private Button image_button;
    private Button video_button;
    private Button gif_button;

    // visual uploads
    private ImageView image_upload_view;
    private VideoView video_upload_view;
    private ImageView gif_upload_view;

    // edit text
    private EditText description_edit_text;
    // submit
    private Button submit_button;

    //images + conversion
    private Uri pic_selected;
    private Bitmap compressed_bitmap;
    private Uri gif_selected;

    //code for onActivityResult
    private final int PICK_IMAGE_PHONE = 450;
    private final int PICK_GIF_PHONE = 550;

    // post class
    private Post current_post;

    // firebase
    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();
    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();

    //TODO WE MUST CHANGE BACK PERMISSION SETTINGS ON THE STORAGE ( so no READ/ WRITE IF AUTH == NULL)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post);
        // post toolbar is included as an <include>
        // this toolbar has a custom theme applied to change the color of the back button
        // https://stackoverflow.com/questions/34581408/change-toolbar-back-arrow-color
        post_toolbar = findViewById(R.id.post_toolbar_reference_id);
        setSupportActionBar(post_toolbar);

        // this adds the back button to the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        // this is required to nullify title, title will be sent to blank character in xml too
        getSupportActionBar().setTitle(null);

        initialize_post();
        setHandlers();
        setListeners();
    }

    /* ************************************************************************************************** */
    // default end action selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    /* ************************************************************************************************** */
    // grab all pertinent views

    private void setHandlers() {
        post_button = findViewById(R.id.post_button);
        event_button = findViewById(R.id.event_button);
        show_button = findViewById(R.id.show_button);
        dont_show_button = findViewById(R.id.dont_show_button);
        from_scratch_button = findViewById(R.id.from_scratch_button);
        import_button = findViewById(R.id.import_button);
        nothing_button = findViewById(R.id.nothing_button);
        image_button = findViewById(R.id.image_button);
        video_button = findViewById(R.id.video_button);
        gif_button = findViewById(R.id.gif_button);

        image_upload_view = findViewById(R.id.image_imageview);
        video_upload_view = findViewById(R.id.video_imageview);
        gif_upload_view = findViewById(R.id.gif_imageview);

        submit_button = findViewById(R.id.submit_button);

        description_edit_text = findViewById(R.id.description_edittext);
    }
    /* ************************************************************************************************** */
    // If a button is disabled it is selected and therefore non clickable

    private void setListeners() {
        post_button.setEnabled(false);
        show_button.setEnabled(false);
        from_scratch_button.setEnabled(false);
        nothing_button.setEnabled(false);

        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEnabled(post_button, event_button);
                swap_type();
            }
        });


        event_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEnabled(post_button, event_button);
                swap_type();
            }
        });

        show_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEnabled(show_button, dont_show_button);
                swap_campus_feed();
            }
        });
        dont_show_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEnabled(show_button, dont_show_button);
                swap_campus_feed();
            }
        });

        from_scratch_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEnabled(from_scratch_button, import_button);
            }
        });
        import_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEnabled(from_scratch_button, import_button);
            }
        });
        nothing_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEnabled(nothing_button);
            }
        });
        image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEnabled(image_button);
            }
        });
        video_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEnabled(video_button);
            }
        });
        gif_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEnabled(gif_button);
            }
        });

        image_upload_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picSelect();

            }
        });
        video_upload_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        gif_upload_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gifSelect();
            }
        });

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    finalize_post();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    /* ************************************************************************************************** */
    // event is initialized to false (its a post), this will swap if its true or not
    private void swap_type(){
        if(current_post.getIs_event())
            current_post.setIs_event(false);
        else
            current_post.setIs_event(true);
    }

    /* ************************************************************************************************** */
    // campus feed is initialized to false, this function swaps that
    private void swap_campus_feed(){
        if(current_post.getMain_feed_visible())
            current_post.setMain_feed_visible(false);
        else
            current_post.setMain_feed_visible(true);
    }

    /* ************************************************************************************************** */
    // this function runs when a button is clicked, it swaps which is enabled,
    // the disabled button is disabled so it can't be re-clicked (because it was selected by user)
    // the enabled button is enabled so it can be clicked, (because it was not selected by user)
    private void toggleEnabled(Button one, Button two) {
        if (one.isEnabled()) {
            one.setEnabled(false);
            two.setEnabled(true);
        } else {
            one.setEnabled(true);
            two.setEnabled(false);
        }
    }
    /* ************************************************************************************************** */
    // re enables all buttons and disables selected
    private void toggleEnabled(Button one) {
        nothing_button.setEnabled(true);
        image_button.setEnabled(true);
        video_button.setEnabled(true);
        gif_button.setEnabled(true);
        one.setEnabled(false);
        swapImageView(one);
    }
    /* ************************************************************************************************** */
    // sets visibility based on which button was clicked

    private void swapImageView(Button btn){
        if(btn == nothing_button){
            clearViews();

        }
        else if(btn == image_button){
            clearViews();
            image_upload_view.setVisibility(View.VISIBLE);
        }
        else if (btn == video_button){
            clearViews();
            video_upload_view.setVisibility(View.VISIBLE);
        }
        else{
            clearViews();
            gif_upload_view.setVisibility(View.VISIBLE);
        }
    }
    /* ************************************************************************************************** */
    // clears all media upload views
    private void clearViews(){
        image_upload_view.setVisibility(View.GONE);
        video_upload_view.setVisibility(View.GONE);
        gif_upload_view.setVisibility(View.GONE);
    }
    /* ************************************************************************************************** */
    // allows user to select a picture from within their phone
    // starts activity for result
    private void picSelect(){
        Intent intent = new Intent();
        // allows any file that is an image, with any file type to be selected
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_PHONE);
    }
    // allows user to select a gif from within their phone
    // starts activity for result
    private void gifSelect(){
        Intent intent = new Intent();
        // allows any file with gif filetype to be selected
        intent.setType("image/gif");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Gif"), PICK_GIF_PHONE);
    }

    /* ************************************************************************************************** */
    // catches picture selection activities when returning and handles their data
    // compressed bitmaps are set here...
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // case pick_image_phone
        if(resultCode== RESULT_OK && requestCode == PICK_IMAGE_PHONE){
            pic_selected = data.getData();

            // the picture setting methods need to be here or after this point/
            // if they are in the on click listener they run asynchronously and don't update in time
            if(pic_selected!=null){

                // try's and catch's might not be required anymore, best to just rewrite function later
                try {
                    //TODO  this is the place to check sizes/ compress images also potentially clear all other views of their previously chosen images
                    compressed_bitmap =compressionHandler();
                    image_upload_view.setImageBitmap(compressed_bitmap);

                    // visual is set to "picture" until finalized and a path is set
                    current_post.setVisual("picture");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            else{
                Toast.makeText(CreatePost.this, "No Picture Selected", Toast.LENGTH_SHORT).show();
            }
        }

        // gifs are just displayed, not sent to DB
        if(resultCode == RESULT_OK && requestCode == PICK_GIF_PHONE){
            gif_selected = data.getData();
            if(gif_selected!=null){
                RequestOptions myOptions = new RequestOptions().centerCrop();
                // glide allows gif to be displayed
                Glide.with(this).asGif().apply(myOptions).load(gif_selected).into(gif_upload_view);
                //TODO this is the place to check sizes/ compress images also potentially clear all other views their previously chosen images
                current_post.setVisual("gif");
            }
            else{
                Toast.makeText(CreatePost.this, "No Gif Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* ************************************************************************************************** */
    // this function will create a post class and give it some preliminary data
    //TODO update this post function properly when AUTH is set

    private void initialize_post(){
        current_post = new Post();
        String newUUID = UUID.randomUUID().toString();
        current_post.setId(newUUID);
        current_post.setUni_domain("ucalgary");
        current_post.setAuthor_id("temp_id");
        current_post.setAuthor_name("test_user");
        current_post.setPinned_id("null");
        current_post.setViews_id("n/a");


        //initialize choices
        current_post.setIs_event(false);
        current_post.setMain_feed_visible(true);
    }

    /* ************************************************************************************************** */
    // on pressing the post button, this function is called to submit in the db and end the activity

    private void finalize_post() throws IOException {
        current_post.setCreation_millis(System.currentTimeMillis());
        current_post.setText(description_edit_text.getText().toString());

        // TODO add proper on failure listener

        // if a picture was selected, a compressed bitmap will converted to a byte array and stored in the DB
        //TODO if someone chooses a picture, then a different media but finally decides on picture this wont fire
        if(current_post.getVisual().equals("picture")){
            storePictureInDB(bmpToByteArray(compressed_bitmap));
        }
        // object will be added to the DB, must wait for on success listener to end AFTER everything else has finished
        db_reference.collection("universities").document("ucalgary.ca").collection("posts").document(current_post.getId()).set(current_post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(CreatePost.this, "Posted!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreatePost.this, "Post failed. Please try again later.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    //compression methods
    // https://stackoverflow.com/questions/51919925/compress-bitmap-to-a-specific-byte-size-in-android
    /* ************************************************************************************************** */
    private Bitmap compressionHandler() throws IOException {
        // restructures uri to bitmap
        Bitmap initial_bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pic_selected);
        //Toast.makeText(this, "initial" +initial_bitmap.getByteCount(), Toast.LENGTH_LONG).show();

        // currently checks if images are larger than 1mb, resizes if so
        //TODO WHAT IF STILL OVER 1.5 MEGS WHEN RESIZED
        if(initial_bitmap.getByteCount()>1000000) {
            // reusing initial bitmap memory... but it is resized to be max 500 px
            initial_bitmap = getResizedBitmap(initial_bitmap, 500);
            //Toast.makeText(this, "Resized" +initial_bitmap.getByteCount(), Toast.LENGTH_LONG).show();
        }
        return initial_bitmap;
    }

    //temporary compression methods

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image,
                width,
                height,
                true);
    }
    // converts a bitmap to to a byte array, and format JPEG, quality remains at 100 to prevent further loss
    //https://stackoverflow.com/questions/4989182/converting-java-bitmap-to-byte-array
    private byte[] bmpToByteArray(Bitmap bmp) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bmp.recycle();
        stream.close();

        return byteArray;
    }

    /* ************************************************************************************************** */
    // stores pics in the db to a test repository

    private void storePictureInDB(byte [] jpeg_file){
        // uses current posts random UUID
        final String path = "test_for_posts/" + current_post.getId() + "/" + current_post.getId() + ".jpg";

        // i believe that test_storage is a keyword, the path cant be initialized
        //final String path = "test_storage/" + current_post.getId()  + ".jpg";

        StorageReference post_image_storage = db_storage.child(path);

        // if current post is not set here the function ends to quick for it to be set properly
        current_post.setVisual(path);
        post_image_storage.putBytes(jpeg_file).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "photo upload failed", Toast.LENGTH_LONG).show();
                current_post.setVisual("upload failed");
            }
        });
    }
}
