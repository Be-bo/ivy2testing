package com.ivy2testing.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.R;
import com.ivy2testing.entities.User;

import java.util.HashMap;
import java.util.Map;

public class ContactUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        setTitle("Contact Us");
        FirebaseFirestore ref = FirebaseFirestore.getInstance();
        User thisUser = getIntent().getParcelableExtra("this_user");

        ProgressBar progressBar = findViewById(R.id.contact_us_progress);
        EditText editText = findViewById(R.id.contact_us_edittext);
        Button sendButton = findViewById(R.id.contact_us_send_button);
        sendButton.setOnClickListener(view -> {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            progressBar.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.INVISIBLE);

            Map<String, Object> feedbackPackage = new HashMap<>();
            feedbackPackage.put("message", editText.getText().toString());
            feedbackPackage.put("read", false);
            feedbackPackage.put("time", System.currentTimeMillis());
            if(thisUser == null){
                feedbackPackage.put("user_id", "anonymous");
                feedbackPackage.put("uni_domain", "ucalgary.ca");
            }else{
                feedbackPackage.put("user_id", thisUser.getId());
                feedbackPackage.put("uni_domain", thisUser.getUni_domain());
            }

            ref.collection("contactus").document(Long.toString(System.currentTimeMillis())).set(feedbackPackage).addOnCompleteListener(task -> {
                if(task.isSuccessful()) Toast.makeText(this, "Sent!", Toast.LENGTH_LONG).show();
                else Toast.makeText(this, "failed!", Toast.LENGTH_LONG).show();
                finish();
            });
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String txt = editText.getText().toString();
                if(!txt.trim().isEmpty()) sendButton.setEnabled(true);
                else sendButton.setEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });

    }
}
