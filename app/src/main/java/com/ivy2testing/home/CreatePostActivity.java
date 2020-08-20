package com.ivy2testing.home;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Event;
import com.ivy2testing.entities.Post;
import com.ivy2testing.entities.User;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.TimePickerDialog;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, android.app.TimePickerDialog.OnTimeSetListener, AdapterView.OnItemSelectedListener {


    // MARK: Variables

    private static final String TAG = "CreatePostActivityTag";
    private User this_user;
    private Event this_event;
    private Post this_post;
    private boolean editing_mode = false;
    private long start_millis = 0;
    private long end_millis = 0;
    private boolean setting_start_millis = true; //used with date picker -> so that when know which var to update when TimeSet is called

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference stor = FirebaseStorage.getInstance().getReference();

    private TextView type_title;
    private LinearLayout type_container;
    private Button type_event_button;
    private Button type_post_button;
    private Button visual_nothing_button;
    private Button visual_image_button;
    private ImageView image_upload_view;
    private EditText description_edit_text;
    private EditText title_edit_text;
    private EditText location_edit_text;
    private Button start_time_button;
    private Button end_time_button;
    private ConstraintLayout event_fields_layout;
    private ConstraintLayout post_fields_layout;
    private Spinner pinned_spinner;
    private Button submit_button;
    private ProgressBar progress_bar;
    private EditText link_edit_text;

    private Calendar temp_calendar = Calendar.getInstance();
    private List<String> pinnable_event_names = new ArrayList<>();
    private List<String> pinnable_event_ids = new ArrayList<>();


    // MARK: Override

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Create Post");
        setContentView(R.layout.activity_create_post);
        this_user = getIntent().getParcelableExtra("this_user");
        if (this_user == null) finish();
        else {
            this_post = new Post(UUID.randomUUID().toString(), this_user.getUni_domain(), this_user.getId(), this_user.getName(), true, "", "", "");
            declareHandles();
            setListeners();
            prepSpinner();

            editing_mode = getIntent().getBooleanExtra("editing_mode", false);
            if (editing_mode) {
                submit_button.setEnabled(true);
                hideType();
            } else {
                initializePost();
            }

        }
    }

    private void initializePost() {
        this_post = new Post(
                UUID.randomUUID().toString(),
                this_user.getUni_domain(),
                this_user.getId(),
                this_user.getName(),
                true,
                "",
                "",
                "");

        this_post.setAuthor_is_organization(this_user.getIs_organization());
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        temp_calendar.clear();
        temp_calendar.set(Calendar.YEAR, year);
        temp_calendar.set(Calendar.MONTH, month);
        temp_calendar.set(Calendar.DATE, dayOfMonth);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        temp_calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        temp_calendar.set(Calendar.MINUTE, minute);
        if (setting_start_millis) {
            start_millis = temp_calendar.getTimeInMillis();
            start_time_button.setText(getButtonDisplayTime());
        } else {
            end_millis = temp_calendar.getTimeInMillis();
            end_time_button.setText(getButtonDisplayTime());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }


    // MARK: Setup

    private void declareHandles() { //connect the xml with with the activity
        type_title = findViewById(R.id.create_post_type_title);
        type_container = findViewById(R.id.create_post_type_container);
        type_event_button = findViewById(R.id.create_post_event_button);
        type_post_button = findViewById(R.id.create_post_post_button);
        visual_nothing_button = findViewById(R.id.create_post_nothing_button);
        visual_image_button = findViewById(R.id.create_post_image_button);
        image_upload_view = findViewById(R.id.create_post_image_view);
        description_edit_text = findViewById(R.id.create_post_description_edit_text);
        title_edit_text = findViewById(R.id.create_post_title_edit_text);
        location_edit_text = findViewById(R.id.create_post_location_edit_text);
        start_time_button = findViewById(R.id.create_post_start_time_button);
        end_time_button = findViewById(R.id.create_post_end_time_button);
        event_fields_layout = findViewById(R.id.create_post_event_fields_layout);
        post_fields_layout = findViewById(R.id.create_post_post_fields_layout);
        pinned_spinner = findViewById(R.id.create_post_pin_event_spinner);
        submit_button = findViewById(R.id.create_post_submit_btn);
        progress_bar = findViewById(R.id.create_post_progress_bar);
        link_edit_text = findViewById(R.id.create_post_link_edit_text);
    }

    private void setListeners() { //set up on click listener actions for all buttons/interactive elements
        type_event_button.setOnClickListener(view -> handleClick(type_event_button));
        type_post_button.setOnClickListener(view -> handleClick(type_post_button));
        visual_nothing_button.setOnClickListener(view -> handleClick(visual_nothing_button));
        visual_image_button.setOnClickListener(view -> handleClick(visual_image_button));
        start_time_button.setOnClickListener(view -> handleClick(start_time_button));
        end_time_button.setOnClickListener(view -> handleClick(end_time_button));
        image_upload_view.setOnClickListener(view -> pickImage());
        submit_button.setOnClickListener(view -> databaseUpload());

        TextWatcher universalTW = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkFields();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        description_edit_text.addTextChangedListener(universalTW);
        title_edit_text.addTextChangedListener(universalTW);
        location_edit_text.addTextChangedListener(universalTW);
        start_time_button.addTextChangedListener(universalTW);
        end_time_button.addTextChangedListener(universalTW);
    }

    private void checkFields() { //checks text input fields (description, title, location)
        if (!type_event_button.isEnabled()) { //the user is prepping an event
            if (!location_edit_text.getText().toString().trim().isEmpty() && !title_edit_text.getText().toString().isEmpty() && !description_edit_text.getText().toString().trim().isEmpty()
                    && start_millis != 0 && end_millis != 0 && start_millis < end_millis)
                submit_button.setEnabled(true);
            else submit_button.setEnabled(false);
        } else {
            if (!description_edit_text.getText().toString().trim().isEmpty())
                submit_button.setEnabled(true);
            else submit_button.setEnabled(false);
        }
    }

    private void handleClick(Button clickedButton) { //universal clicking method
        if (clickedButton == type_event_button) { //reset collected data & clear event layout and make it visible, flip type buttons
            this_event = new Event(this_post); // TODO the problem is here
            this_post = null;
            resetLayoutToEvent();

        } else if (clickedButton == type_post_button) { //reset collected data & clear post layout and make it visible, flip type buttons
            this_post = new Post(this_event);
            this_event = null;
            resetLayoutToPost();

        } else if (clickedButton == visual_nothing_button) { //hide image upload view, flip buttons
            setNothingLayout();

        } else if (clickedButton == visual_image_button) { //show image view, start image picker, and flip visual buttons
            pickImage();
            setImageLayout();

        } else if (clickedButton == start_time_button) { //open time pickers, once user done selecting put time as text into the button, update start millis var
            setting_start_millis = true;
            DialogFragment timePicker = new TimePickerDialog();
            timePicker.show(getSupportFragmentManager(), "time picker");

            DialogFragment datePickerStart = new com.ivy2testing.util.DatePickerDialog();
            datePickerStart.show(getSupportFragmentManager(), "date picker");

        } else if (clickedButton == end_time_button) { //open time pickers, once user done selecting put time as text into the button, update end millis var
            setting_start_millis = false;
            DialogFragment timePicker = new TimePickerDialog();
            timePicker.show(getSupportFragmentManager(), "time picker");

            DialogFragment datePickerStart = new com.ivy2testing.util.DatePickerDialog();
            datePickerStart.show(getSupportFragmentManager(), "date picker");
        }
    }

    private void setFields() { //this method is for when the user is editing their post (it'll set the appropriate post/event object values to the ui)
        boolean isEvent = getIntent().getBooleanExtra("is_event", false);

        if (isEvent) { //the edited post is an event
            setTitle("Edit Event");
            handleClick(type_event_button);
            this_event = getIntent().getParcelableExtra("event");
            if (this_event == null) {
                Toast.makeText(this, "Couldn't transfer event data.", Toast.LENGTH_LONG).show();
                finish();
            }
            if (this_event.getVisual() != null && !this_event.getVisual().equals("nothing") && !this_event.getVisual().equals("")) { //the event has a visual
                stor.child(this_event.getVisual()).getDownloadUrl().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null)
                        Glide.with(this).load(task.getResult()).into(image_upload_view);
                });
                setImageLayout();
            }
            description_edit_text.setText(this_event.getText());
            title_edit_text.setText(this_event.getName());
            location_edit_text.setText(this_event.getLocation());
            temp_calendar.setTimeInMillis(this_event.getStart_millis());
            start_time_button.setText(getButtonDisplayTime());
            temp_calendar.setTimeInMillis(this_event.getEnd_millis());
            end_time_button.setText(getButtonDisplayTime());
            start_millis = this_event.getStart_millis();
            end_millis = this_event.getEnd_millis();

        } else { //the edited post is a standard post
            setTitle("Edit Post");
            handleClick(type_post_button);
            this_post = getIntent().getParcelableExtra("post");
            if (this_post == null) {
                Toast.makeText(this, "Couldn't transfer post data.", Toast.LENGTH_LONG).show();
                finish();
            }
            if (this_post.getVisual() != null && !this_post.getVisual().equals("nothing") && !this_post.getVisual().equals("")) { //the event has a visual
                stor.child(this_post.getVisual()).getDownloadUrl().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null)
                        Glide.with(this).load(task.getResult()).into(image_upload_view);
                });
                setImageLayout();
            }
            description_edit_text.setText(this_post.getText());
            pinned_spinner.setSelection(pinnable_event_ids.indexOf(this_post.getPinned_id()));
        }
    }


    // MARK: Spinner

    private void prepSpinner() { //prepares pinned event spinner
        pinnable_event_names.add("none"); //first item is no pinned event
        pinnable_event_ids.add("none");
        db.collection("universities").document(this_user.getUni_domain()).collection("posts").whereEqualTo("is_event", true).whereEqualTo("is_active", true).get()
                .addOnCompleteListener(querTask -> {
                    if (querTask.isSuccessful() && querTask.getResult() != null && !querTask.getResult().isEmpty()) {
                        for (DocumentSnapshot doc : querTask.getResult()) {
                            pinnable_event_ids.add(doc.getId());
                            pinnable_event_names.add(String.valueOf(doc.get("name")));
                        }

                        ArrayAdapter<String> pinned_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, pinnable_event_names); // create and set adapter
                        pinned_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        pinned_spinner.setAdapter(pinned_adapter);
                        if (editing_mode) setFields(); //have to wait for the spinner to load...
                    }
                });
    }


    // MARK: Image Picking

    private void pickImage() { //pick image from storage
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, Constant.PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constant.PICK_IMAGE_REQUEST_CODE:
                    if (data != null && data.getData() != null) {
                        Uri destinationUri = Uri.fromFile(new File(this.getCacheDir(), "img_" + System.currentTimeMillis()));
                        UCrop.of(data.getData(), destinationUri).withAspectRatio(1, 1).withMaxResultSize(ImageUtils.IMAGE_MAX_DIMEN, ImageUtils.IMAGE_MAX_DIMEN).start(this);
                    }
                    break;
                case UCrop.REQUEST_CROP:
                    if (data != null) {
                        final Uri resultUri = UCrop.getOutput(data);
                        Glide.with(this).load(resultUri).into(image_upload_view);
                    }
                    break;
            }
        } else {
            if (resultCode == RESULT_CANCELED) {
                handleClick(visual_nothing_button); //if cancelled it's the same as if the user pressed the nothing button
            } else {
                Toast.makeText(this, "Failed to get image. :-(", Toast.LENGTH_LONG).show();
            }
        }
    }


    // MARK: DB Interaction

    private void databaseUpload() { //upload the new post/event to the database
        barInteraction();
        if (!type_event_button.isEnabled()) { //if we're uploading an event
            this_event.setText(description_edit_text.getText().toString());
            this_event.setName(title_edit_text.getText().toString());
            this_event.setLocation(location_edit_text.getText().toString());
            this_event.setStart_millis(start_millis);
            this_event.setEnd_millis(end_millis);
            if(!link_edit_text.getText().toString().trim().isEmpty()) this_event.setLink(link_edit_text.getText().toString().trim());
            else this_event.setLink(null);

            if (image_upload_view.getVisibility() == View.VISIBLE) {
                String visualPath = "postfiles/" + this_event.getId() + "/" + this_event.getId() + ".jpg";
                this_event.setVisual(visualPath);
            } else this_event.setVisual("");

            db.collection("universities").document(this_user.getUni_domain()).collection("posts").document(this_event.getId()).set(this_event).addOnCompleteListener(task -> {
                if (task.isSuccessful()) visualStorageUpload(this_event.getId());
                else Toast.makeText(this, "Failed to upload event.", Toast.LENGTH_LONG).show();
            });

        } else { //if we're uploading a post
            this_post.setText(description_edit_text.getText().toString());
            if (pinned_spinner.getSelectedItemPosition() > 0) {
                this_post.setPinned_id(pinnable_event_ids.get(pinned_spinner.getSelectedItemPosition()));
                this_post.setPinned_name(pinnable_event_names.get(pinned_spinner.getSelectedItemPosition()));
            } else {
                this_post.setPinned_id("");
                this_post.setPinned_name("");
            }

            if (image_upload_view.getVisibility() == View.VISIBLE) {
                String visualPath = "postfiles/" + this_post.getId() + "/" + this_post.getId() + ".jpg";
                this_post.setVisual(visualPath);
            } else this_post.setVisual("");

            db.collection("universities").document(this_user.getUni_domain()).collection("posts").document(this_post.getId()).set(this_post).addOnCompleteListener(task -> {
                if (task.isSuccessful()) visualStorageUpload(this_post.getId());
                else Toast.makeText(this, "Failed to upload post.", Toast.LENGTH_LONG).show();
            });
        }
    }

    private void visualStorageUpload(String id) {
        if (image_upload_view.getVisibility() == View.VISIBLE) { //we have an image
            Bitmap bitmap = Bitmap.createBitmap(image_upload_view.getWidth(), image_upload_view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            image_upload_view.draw(canvas);
            String visualPath = "postfiles/" + id + "/" + id + ".jpg";
            stor.child(visualPath).putBytes(ImageUtils.compressAndGetBytes(bitmap)).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String previewPath = "postfiles/" + id + "/previewimage.jpg";
                    stor.child(previewPath).putBytes(ImageUtils.compressAndGetPreviewBytes(bitmap)).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            allowInteraction();
                            setResult(RESULT_OK);
                            if (editing_mode)
                                Toast.makeText(this, "Your Feed and Profile will update after restart.", Toast.LENGTH_LONG).show();
                            finish();
                        } else
                            Toast.makeText(this, "Failed to upload image.", Toast.LENGTH_LONG).show();
                    });
                } else Toast.makeText(this, "Failed to upload image.", Toast.LENGTH_LONG).show();
            });

        } else { //we don't have an image
            allowInteraction();
            setResult(RESULT_OK);
            if (editing_mode)
                Toast.makeText(this, "Your Feed and Profile will update after restart.", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    // MARK: Support Methods

    private void resetLayoutToEvent() { //reset layout to default right after the user changed type from post to event
        type_event_button.setEnabled(false);
        type_post_button.setEnabled(true);
        title_edit_text.setText("");
        location_edit_text.setText("");
        start_millis = 0;
        end_millis = 0;
        start_time_button.setText(getString(R.string.start_time));
        end_time_button.setText(getString(R.string.end_time));
        event_fields_layout.setVisibility(View.VISIBLE);
        post_fields_layout.setVisibility(View.GONE);
    }

    private void resetLayoutToPost() { //reset layout to default right after the user changed type from event to post
        type_post_button.setEnabled(false);
        type_event_button.setEnabled(true);
        pinned_spinner.setSelection(-1);
        event_fields_layout.setVisibility(View.GONE);
        post_fields_layout.setVisibility(View.VISIBLE);
    }

    private String getButtonDisplayTime() { //formatting time for displaying in within the start and end time buttons
        String amPm = "am";
        String hour = "";
        if (temp_calendar.get(Calendar.HOUR) == 0) hour = "12";
        else hour = String.valueOf(temp_calendar.get(Calendar.HOUR));
        if (temp_calendar.get(Calendar.HOUR_OF_DAY) > 11) amPm = "pm";
        return (temp_calendar.get(Calendar.MONTH) + 1) + "/" + temp_calendar.get(Calendar.DATE) + "/" + temp_calendar.get(Calendar.YEAR) + " at " + hour + ":" + temp_calendar.get(Calendar.MINUTE) + amPm;
    }

    private void hideType() {
        type_title.setVisibility(View.GONE);
        type_container.setVisibility(View.GONE);
    }

    private void setNothingLayout() {
        image_upload_view.setVisibility(View.GONE);
        visual_nothing_button.setEnabled(false); //true means not selected, and false means selected
        visual_image_button.setEnabled(true);
    }

    private void setImageLayout() {
        image_upload_view.setVisibility(View.VISIBLE);
        visual_image_button.setEnabled(false);
        visual_nothing_button.setEnabled(true);
    }

    private void allowInteraction() {
        submit_button.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.GONE);
    }

    private void barInteraction() {
        submit_button.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);
    }
}
