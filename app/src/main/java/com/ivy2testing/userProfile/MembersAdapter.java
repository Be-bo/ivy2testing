package com.ivy2testing.userProfile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.entities.Student;
import com.ivy2testing.util.ImageUtils;

class MembersAdapter extends RecyclerView.Adapter<MembersViewHolder>{


    // MARK: Variables and Constants

    private Organization organization;
    private Context context;
    private boolean itemsEditable = false;
    private FirebaseFirestore baseDatabaseReference = FirebaseFirestore.getInstance();
    private StorageReference baseStorageReference = FirebaseStorage.getInstance().getReference();




    // MARK: Base Methods

    MembersAdapter(Context con, Organization org, boolean itemsEditable){
        this.organization = org;
        this.context = con;
        this.itemsEditable = itemsEditable;
    }





    // MARK: Override Methods

    @NonNull
    @Override
    public MembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_members, parent, false);
        return new MembersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MembersViewHolder holder, int position) {
        String currentId = organization.getMember_ids().get(position);
        baseDatabaseReference.collection("universities").document(organization.getUni_domain()).collection("users").document(currentId).get().addOnCompleteListener((task) -> {
            if(task.isSuccessful() && task.getResult() != null){
                Student pulledProfile = task.getResult().toObject(Student.class);
                if(pulledProfile != null){
                    holder.name.setText(pulledProfile.getName());
                    baseStorageReference.child(ImageUtils.getProfilePath(pulledProfile.getId())).getDownloadUrl().addOnCompleteListener(uri -> { Glide.with(context).load(uri).into(holder.hodendofWeirdEuropeanNameCircleImageView); });
                    if(itemsEditable) {
                        holder.options.setVisibility(View.VISIBLE);
                        holder.options.setOnClickListener(view -> {
                            //TODO: woah, carriage return
                        });
                    }
                    else holder.options.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return organization.getMember_ids().size();
    }
}
