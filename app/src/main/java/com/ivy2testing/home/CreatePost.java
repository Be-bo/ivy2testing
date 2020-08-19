package com.ivy2testing.home;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ivy2testing.R;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;
import com.ivy2testing.entities.User;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.TimePickerDialog;
import com.yalantis.ucrop.UCrop;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;




// TODO: gifs and vids add a textview

public class CreatePost extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, android.app.TimePickerDialog.OnTimeSetListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "CreatePostActivity";

    //type
    private Button post_button;
    private Button event_button;

    //show in feed
    private Button show_button;
    private Button dont_show_button;

    // import
    private Button from_scratch_button;
    private Button import_button;
    private TextView import_coming_soon_text;

    // visual
    private Button nothing_button;
    private Button image_button;
    private Button video_button;
    private Button gif_button;

    // visual uploads
    private ImageView image_upload_view;
    private TextView coming_soon_textview;
    private ImageView gif_upload_view;

    // edit text
    private EditText description_edit_text;

    // submit
    private Button submit_button;
    private ProgressBar submit_progress_bar;

    //images + conversion
    private Uri pic_selected;
    private byte[] final_image_bytes;
    private byte[] preview_image_bytes;
    private Uri gif_selected;

    //code for onActivityResult
    private final int PICK_IMAGE_PHONE = 450;
    private final int PICK_GIF_PHONE = 550;

    // post class
    private Post current_post;
    private Event current_event;

    // event items
    private ConstraintLayout pinned_layout;
    private ConstraintLayout event_fields;
    private EditText title_editText;
    private EditText location_editText;
    private EditText link_editText;

    //calendar buttons + times
    private Button start_date_button;
    private Button end_date_button;
    private Long start_date_millis;
    private Long end_date_millis;

    //calendar and time toggles
    private boolean start_or_end;
    private boolean start_time_or_end;

    // time buttons + times
    private Button start_time_button;
    private Button end_time_button;
    private Long start_time_millis;
    private Long end_time_millis;


    // pinned event spinner
    final ArrayList<String> pinned_names_array = new ArrayList<String>();
    private Spinner pinned_spinner;
    private Dictionary pinnable_events = new Hashtable();
    private String pinned_event_name;
    private String pinned_event_id;


    // firebase
    private User this_user;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();
    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();

    // TODO THE TIME SELECTION SETTINGS RELY ON THE CALENDAR CLASS, THEY NEED TO BE TESTED WITH DIFFERENT ON PHONE CALENDAR SETTINGS/ TIMEZONES
    //TODO WE MUST CHANGE BACK PERMISSION SETTINGS ON THE STORAGE ( so no READ/ WRITE IF AUTH == NULL)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        this_user = intent.getParcelableExtra("this_user");
        if(this_user == null) finish();

        setContentView(R.layout.activity_create_post);
        setTitle(R.string.new_post);            // App Bar Title

        // pinned event spinner wont allow selection for some reason, but it can pull from the db properly
        initializePinnedSpinner();
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
        //post fields
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
        description_edit_text = findViewById(R.id.description_edittext);
        pinned_layout = findViewById(R.id.pin_event_constraint);
        import_coming_soon_text = findViewById(R.id.import_coming_soon_textview);

        //media views
        image_upload_view = findViewById(R.id.image_imageview);
        coming_soon_textview = findViewById(R.id.coming_soon_textview);
        gif_upload_view = findViewById(R.id.gif_imageview);

        // event fields
        title_editText = findViewById(R.id.event_title_editText);
        location_editText = findViewById(R.id.event_location_editText);
        link_editText = findViewById(R.id.external_link_editText);

        event_fields = findViewById(R.id.event_constraint_view);
        start_date_button = findViewById(R.id.start_date_button);
        end_date_button = findViewById(R.id.end_date_button);
        start_time_button = findViewById(R.id.start_time_button);
        end_time_button = findViewById(R.id.end_time_button);

        submit_progress_bar = findViewById(R.id.submit_progress_bar);
        submit_button = findViewById(R.id.submit_button);
    }


    /* ************************************************************************************************** */
    // Post handlers
    // If a button is disabled it is "selected" and therefore non clickable

    private void setListeners() {
        post_button.setEnabled(false);
        show_button.setEnabled(false);
        from_scratch_button.setEnabled(false);
        nothing_button.setEnabled(false);

        post_button.setOnClickListener(v -> {
            toggleEnabled(post_button, event_button);
            swap_type();
        });


        event_button.setOnClickListener(v -> {
            toggleEnabled(post_button, event_button);
            swap_type();

        });


        show_button.setOnClickListener(v -> {
            toggleEnabled(show_button, dont_show_button);
            swap_campus_feed();
        });
        dont_show_button.setOnClickListener(v -> {
            toggleEnabled(show_button, dont_show_button);
            swap_campus_feed();
        });

        from_scratch_button.setOnClickListener(v ->{
            import_coming_soon_text.setVisibility(View.GONE);
            toggleEnabled(from_scratch_button, import_button);
        });
        import_button.setOnClickListener(v ->{
            import_coming_soon_text.setVisibility(View.VISIBLE);
            toggleEnabled(from_scratch_button, import_button);
        });
        nothing_button.setOnClickListener(v -> toggleEnabled(nothing_button));
        image_button.setOnClickListener(v -> {
            picSelect();
            toggleEnabled(image_button);

        });
        video_button.setOnClickListener(v -> toggleEnabled(video_button));
        gif_button.setOnClickListener(v -> {
            gifSelect();
            toggleEnabled(gif_button);
        });

        image_upload_view.setOnClickListener(v -> picSelect());
        coming_soon_textview.setOnClickListener(v -> {
        });
        gif_upload_view.setOnClickListener(v -> gifSelect());


        /* ************************************************************************************************** */
        //events only
        start_date_button.setOnClickListener(v -> {
            start_or_end = true;
            DialogFragment datePickerStart = new com.ivy2testing.util.DatePickerDialog();
            datePickerStart.show(getSupportFragmentManager(), "start date picker");
        });

        end_date_button.setOnClickListener(v -> {
            start_or_end = false;
            DialogFragment datePickerEnd = new com.ivy2testing.util.DatePickerDialog();
            datePickerEnd.show(getSupportFragmentManager(), "end date picker");
        });

        start_time_button.setOnClickListener(v -> {
            start_time_or_end = true;
            DialogFragment timePicker = new TimePickerDialog();
            timePicker.show(getSupportFragmentManager(), "time picker start");
        });

        end_time_button.setOnClickListener(v -> {
            start_time_or_end = false;
            DialogFragment timePicker = new TimePickerDialog();
            timePicker.show(getSupportFragmentManager(), "time picker start");
        });

        /* ************************************************************************************************** */
        //submit will show progress bar, and remove the signup button,
        submit_button.setOnClickListener(v -> {

            submitViewChange();
            try {
                if (current_post != null)
                    finalizePost();
                else if (current_event != null)
                    finalizeEvent();
            } catch (IOException e) {
                e.printStackTrace();
                submitViewChange();
            }
        });

    }

    /* ************************************************************************************************** */
    // event is initialized to false (its a post), this will swap if its true or not
    private void swap_type() {
        if (current_post != null) {
            current_event = new Event(current_post);
            current_post = null;
            event_fields.setVisibility(View.VISIBLE);
            pinned_layout.setVisibility(View.GONE);
        } else if (current_event != null) {
            current_post = new Post(current_event);
            current_event = null;
            event_fields.setVisibility(View.GONE);
            pinned_layout.setVisibility(View.VISIBLE);
        } else Log.e("CreatePost", "Both current_event and current_post are null!");
    }


    /* ************************************************************************************************** */
    // campus feed is initialized to false, this function swaps that
    private void swap_campus_feed() {
        if (current_post != null) {
            if (current_post.isMain_feed_visible())
                current_post.setMain_feed_visible(false);
            else
                current_post.setMain_feed_visible(true);
        } else if (current_event != null) {
            if (current_event.isMain_feed_visible())
                current_event.setMain_feed_visible(false);
            else
                current_event.setMain_feed_visible(true);
        }
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

    private void swapImageView(Button btn) {
        if (btn == nothing_button) {
            clearViews();

        } else if (btn == image_button) {
            clearViews();
            image_upload_view.setVisibility(View.VISIBLE);
        } else if (btn == video_button
        || btn == gif_button
        ) {
            clearViews();
            coming_soon_textview.setVisibility(View.VISIBLE);
        } else {
            clearViews();
//            gif_upload_view.setVisibility(View.VISIBLE);
        }
    }

    /* ************************************************************************************************** */
    // swap around visibility of progress bar and submit button
    private void submitViewChange() {
        if (submit_progress_bar.getVisibility() == View.GONE) {
            barInteraction();
            submit_progress_bar.setVisibility(View.VISIBLE);
            submit_button.setVisibility(View.GONE);
        } else {
            submit_progress_bar.setVisibility(View.GONE);
            submit_button.setVisibility(View.VISIBLE);
            allowInteraction();
        }
    }

    /* ************************************************************************************************** */
    // clears all media upload views
    private void clearViews() {
        image_upload_view.setVisibility(View.GONE);
        coming_soon_textview.setVisibility(View.GONE);
        gif_upload_view.setVisibility(View.GONE);
    }

    /* ************************************************************************************************** */
    // allows user to select a picture from within their phone
    // starts activity for result

    private void picSelect() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, Constant.PICK_IMAGE_REQUEST_CODE);
    }


    // allows user to select a gif from within their phone
    // starts activity for result
    //TODO gifs can be selected but wont upload to the db
    private void gifSelect() {
//        Intent intent = new Intent();
//        intent.setType("image/gif");
//        intent.setAction(Intent.ACTION_PICK);
//        startActivityForResult(Intent.createChooser(intent, "Select Gif"), PICK_GIF_PHONE);
    }






    /* ************************************************************************************************** */
    // catches picture selection activities when returning and handles their data
    // compressed bitmaps are set here...
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                    if(data != null){
                        final Uri resultUri = UCrop.getOutput(data);
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                            preview_image_bytes = ImageUtils.compressAndGetPreviewBytes(bitmap);
                            final_image_bytes = ImageUtils.compressAndGetBytes(bitmap);
                            Glide.with(this).load(resultUri).into(image_upload_view);
                            if (current_post!=null) current_post.setVisual("picture");
                            else if (current_event!=null) current_event.setVisual("picture");
                        } catch (IOException e) {
                            Toast.makeText(this, "Failed to get image. :-(", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Failed to get image. :-(", Toast.LENGTH_LONG).show();
        }

        if (resultCode == RESULT_OK && requestCode == PICK_GIF_PHONE && data != null) { // gifs are just displayed, not sent to DB
            gif_selected = data.getData();
            if (gif_selected != null) {
                RequestOptions myOptions = new RequestOptions().centerCrop();
                // glide allows gif to be displayed
                Glide.with(this).asGif().apply(myOptions).load(gif_selected).into(gif_upload_view);
                //TODO this is the place to check sizes/ compress images also potentially clear all other views their previously chosen images
                if (!current_post.equals("")) current_post.setVisual("gif");
                else if (!current_event.equals("")) current_event.setVisual("gif");
            } else {
                Toast.makeText(CreatePost.this, "No Gif Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }





















    /* ************************************************************************************************** */
    // the on date set listener is called after the date view dialogs
    // it will post on either the start or end date button a human readable date
    // and also instantiate a calendar object to the time of midnight on the selected date + store the time in millis

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);


        // set getDateInstance(DateFormat.FULL) to get weekdays
        String selected_date = DateFormat.getDateInstance().format(c.getTime());
        if (start_or_end) {
            start_date_button.setError(null);
            start_date_button.setText(selected_date);
            start_date_millis = c.getTimeInMillis();
        } else {
            end_date_button.setError(null);
            end_date_button.setText(selected_date);
            end_date_millis = c.getTimeInMillis();
        }

    }
    /* ************************************************************************************************** */
    //onTimeSet is called after the timepicker dialogs
    // it will translate strings from military time to 12hr time and add an am/pm
    // it will also multiply the selected time by the number of millis in an hour/ minute and store those times

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        //TODO
        String minute_adjusted;
        String string_adjusted;


        if (minute < 10)
            minute_adjusted = "0" + minute;
        else
            minute_adjusted = "" + minute;
        if (hourOfDay == 12)
            string_adjusted = "12:" + minute_adjusted + " pm";
        else if (hourOfDay > 12)
            string_adjusted = (hourOfDay - 12) + ":" + minute_adjusted + " pm";
        else if (hourOfDay == 0)
            string_adjusted = "12:" + minute_adjusted + " am";
        else
            string_adjusted = hourOfDay + ":" + minute_adjusted + " am";

        if (start_time_or_end) {
            start_time_button.setError(null);
            start_time_millis = (long) (hourOfDay * 3600000) + (minute * 60000);
            start_time_button.setText(string_adjusted);
        } else {
            end_time_button.setError(null);
            end_time_button.setText(string_adjusted);
            end_time_millis = (long) (hourOfDay * 3600000) + (minute * 60000);

        }
    }

    /* ************************************************************************************************** */
    // this function will create a post class and give it some preliminary data
    //TODO update this post function properly when AUTH is set

    private void initialize_post() {
        current_post = new Post(
                UUID.randomUUID().toString(),
                this_user.getUni_domain(),
                this_user.getId(),
                this_user.getName(),
                true,
                "",
                "",
                "");

        current_post.setAuthor_is_organization(this_user.getIs_organization());
    }


    // Finalize methods
    /* ************************************************************************************************** */
    // on pressing the post button, this function is called to submit in the db and end the activity

    private void finalizePost() throws IOException {


        // if description edit text is not blank, otherwise end the method + set error
        if (fieldsOk()) {
            String address = "universities/" + current_post.getUni_domain() + "/posts/" + current_post.getId();

            // final check to make sure the post can get to the db
            if (current_post.getUni_domain() == null || current_post.getId() == null) {
                Log.e("CreatePost", "database address was null!");
                Toast.makeText(this, "Post failed, Are you logged in? ", Toast.LENGTH_SHORT).show();
                finish();
            }
            current_post.setPinned_id(pinned_event_id);
            current_post.setPinned_name(pinned_event_name);
            current_post.setText(description_edit_text.getText().toString());

            // if a picture was selected, a compressed bitmap will converted to a byte array and stored in the DB
            //TODO if someone chooses a picture, then a different media but finally decides on picture this wont fire
            if (current_post.getVisual().equals("")) {
                current_post.setVisual("nothing");

            } else if (current_post.getVisual().equals("picture")) {
                storePictureInDB(final_image_bytes, preview_image_bytes);
            }


            // object will be added to the DB, must wait for on success listener to end AFTER everything else has finished
            db_reference.document(address).set(current_post).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(CreatePost.this, "Posted!", Toast.LENGTH_SHORT).show();

                    //new post's id also has to be added to this user's post_ids - Robert's Addition
                    db_reference.collection("users").document(this_user.getId()).update("post_ids", FieldValue.arrayUnion(current_post.getId()))
                            .addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreatePost.this, "Post failed. Please try again later.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }

    private void finalizeEvent() throws IOException {

        // if fields have appropriate input, otherwise end method
        if (fieldsOk() && timeOk()) {
            String address = "universities/" + current_event.getUni_domain() + "/posts/" + current_event.getId();

            // final check to ensure variables have been instantiated properly
            if (current_event.getUni_domain() == null || current_event.getId() == null) {
                Log.e("CreatePost", "database address was null!");
                Toast.makeText(this, "Post failed, Are you logged in? ", Toast.LENGTH_SHORT).show();
                finish();
            }

            // text fields
            current_event.setText(description_edit_text.getText().toString());
            current_event.setName(title_editText.getText().toString());
            current_event.setLocation(location_editText.getText().toString());
            current_event.setLink(linkFunction(link_editText.getText().toString().trim()));
            // time functions
            current_event.setStart_millis(start_date_millis + start_time_millis);
            current_event.setEnd_millis(end_date_millis + end_time_millis);

            //pinned
            current_event.setPinned_id(pinned_event_id);
            current_event.setPinned_name(pinned_event_name);


            if (current_event.getVisual().equals("")) {
                current_event.setVisual("nothing");
            } else if (current_event.getVisual().equals("picture")) {
                storePictureInDB(final_image_bytes, preview_image_bytes);
            }

            db_reference.document(address).set(current_event).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(CreatePost.this, "Posted!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
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

    }

    // field checking methods
    /* ************************************************************************************************** */
    // check is all the textfields are okay, sequentially
    private Boolean fieldsOk() {
        // post method
        if (current_post!=null) {
            if (description_edit_text.getText().toString().isEmpty()) {
                description_edit_text.setError("Please enter a description.");
                submitViewChange();
                return false;
            }
        }
            // event methods
        else if (current_event!=null) {
            // check title first, location, then description, same order as layout
            // will set error as soon as one field is false, will clear error on previous fields because they are not empty

            if (title_editText.getText().toString().isEmpty()) {
                title_editText.setError("Please enter a description.");
                submitViewChange();
                return false;
            } else if (location_editText.getText().toString().isEmpty()) {
                location_editText.setError("Please give your event a name.");
                title_editText.setError(null);
                submitViewChange();
                return false;
            } else if (description_edit_text.getText().toString().isEmpty()) {
                description_edit_text.setError("Please enter where your event will be.");
                title_editText.setError(null);
                location_editText.setError(null);
                submitViewChange();
                return false;
            }
        }
        // something went wrong in either post or event creation... prompt retry
        else {
            Toast.makeText(this, "POST NULL, Please try again later", Toast.LENGTH_SHORT).show();
            submitViewChange();
            return false;
        }

        return true;
    }

    private boolean timeOk() {
        if (start_date_millis == null) {

            start_date_button.setError("Please choose a start date");
            submitViewChange();
            return false;
        }
        if (start_time_millis == null) {
            start_time_button.setError("Please choose a start time");
            submitViewChange();
            return false;
        }
        if (end_date_millis == null) {
            end_date_button.setError("Please choose an end date");
            submitViewChange();
            return false;
        }
        if (end_time_millis == null) {
            end_time_button.setError("Please choose an end time");
            submitViewChange();
            return false;
        }
        if (start_date_millis + start_time_millis > end_date_millis + end_time_millis) {
            Toast.makeText(this, "Your event starts after it ends!", Toast.LENGTH_LONG).show();
            submitViewChange();
            return false;
        }

        return true;
    }




    /* ************************************************************************************************** */
    // stores pics in the db to a test repository

    private void storePictureInDB(byte[] jpeg_file, byte [] preview_file) {
        String path = "";
        String preview_path = "";
        // if current post is not set here the function ends to quick for it to be set properly
        if (current_post != null) {
            // uses current posts random UUID
            path = "postfiles/" + current_post.getId() + "/" + current_post.getId() + ".jpg";
            preview_path = "postfiles/" + current_post.getId() + "/previewimage.jpg";
            current_post.setVisual(path);

        } else if (current_event != null) {
            path = "postfiles/" + current_event.getId() + "/" + current_event.getId() + ".jpg";
            preview_path = "postfiles/" + current_event.getId() + "/previewimage.jpg";
            current_event.setVisual(path);
        }

        // Error check for the db address
        if (path.contains("null") || path.equals("")) {
            if(current_post!=null)
                current_post.setVisual("upload failed");
            if(current_event!=null)
                current_event.setVisual("upload failed");
            Log.e("CreatePost", "storePictureinDB: path contains null!");
            return;
        }

        StorageReference post_image_storage = db_storage.child(path);
        StorageReference post_preview_storage = db_storage.child(preview_path);



        post_image_storage.putBytes(jpeg_file).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "photo upload failed", Toast.LENGTH_LONG).show();
                if(current_post!=null)
                    current_post.setVisual("upload failed");
                if(current_event!=null)
                    current_event.setVisual("upload failed");
            }
        });
        post_preview_storage.putBytes(preview_file).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


    /* ************************************************************************************************** */
    // this method is basically good to go,
    // first issue is how it pulls more info than it needs and passes it around sloppily
    // it fills an array list with names for the adapter, and creates a hashmap to save the ids for storage
    // TODO the on select listeners are just not working, after that the code is complete
    private void initializePinnedSpinner() {
        // initialize spinner
        pinned_spinner = findViewById(R.id.pinned_event_spinner);
        pinned_names_array.add("None");
        pinnable_events.put("None", "0000");

        // create names array and hashmap to find IDs
        db_reference.collection("universities").document(this_user.getUni_domain()).collection("posts")
                .whereEqualTo("is_event", true)
                .whereEqualTo("is_active", true)
                .whereEqualTo("main_feed_visible", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> is_post = document.getData();
                                if (!is_post.get("name").toString().equals("")) {
                                    pinned_names_array.add(is_post.get("name").toString());
                                    pinnable_events.put(is_post.get("name").toString(), document.getId().toString());
                                }
                            }
                            addArrayListToAdapter();
                        } else {
                            pinned_names_array.add("No pinnable events");
                        }
                    }
                });
    }

    // basic check if link is properly formatted
    private String linkFunction(String link){
        if(link.isEmpty()) return null;
        else if(link.contains(".")) return link;
        else return null;
    }

    private void addArrayListToAdapter(){
        // create and set adapter
        ArrayAdapter<String> pinned_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, pinned_names_array);
        pinned_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pinned_spinner.setAdapter(pinned_adapter);
        // on select listeners
        pinned_spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position == 0){
            pinned_event_name = "";
            pinned_event_id = "";
        }
        else{
            pinned_event_name = pinned_names_array.get(position);
            pinned_event_id = (String) pinnable_events.get(pinned_names_array.get(position));
        }

        //Toast.makeText(this, ""+ pinned_names_array.get(position) + " " + pinnable_events.get(pinned_names_array.get(position)) , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /* ************************************************************************************************** */
    // bar/ allow interaction is swapped with the views on submit/progress bar in submitViewChange
    private void barInteraction() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void allowInteraction() {
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}