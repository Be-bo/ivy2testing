package com.ivy2testing.hometab;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Post;
import com.ivy2testing.entities.User;

/** @author Zahra Ghavasieh
 * Overview: Post view fragment only includes text and pinned ID
 */
public class ViewPostFragment extends Fragment {

    // Constants
    private final static String TAG = "ViewPostFragmentTag";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference stor = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private Post post;
    private User this_user;   // Nullable!


    // Constructor
    public ViewPostFragment(Post post, User this_user){
        this.post = post;
        this.this_user = this_user;
    }


/* Override Methods
***************************************************************************************************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root_view = inflater.inflate(R.layout.fragment_viewpost, container, false);
        setUp(root_view);
        return root_view;
    }

/* Initialization Methods
***************************************************************************************************/

    private void setUp(View v){
        // Populate Text View
        TextView tv_description = v.findViewById(R.id.viewPost_description);
        tv_description.setText(post.getText());

        // Handle Pinned Event
        TextView pinnedEventText = v.findViewById(R.id.viewPost_pinned);
        if (post.getPinned_id().equals("") || post.getPinned_id() == null) v.findViewById(R.id.viewPost_pinLayout).setVisibility(View.GONE); //if no pinned event
        else{
            pinnedEventText.setText(post.getPinned_name());
            pinnedEventText.setOnClickListener(v1 -> viewPinned());
        }
    }


/* Transition and OnClick Methods
***************************************************************************************************/

    private void viewPinned() { // Transition to pinned
        Intent intent = new Intent(getActivity(), ViewPostOrEventActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("post_id", post.getPinned_id());
        intent.putExtra("post_uni", post.getUni_domain());
        intent.putExtra("author_id", post.getAuthor_id());
        startActivity(intent);
    }
}
