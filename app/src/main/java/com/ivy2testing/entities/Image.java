package com.ivy2testing.entities;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.PageKeyedDataSource;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/** @author Zahra Ghavasieh
 * Overview: Class to store an image address in storage and its Uri
 * Features: Parcelable (can pass as intent Extra)
 */
public class Image implements Parcelable {

    // Fields
    private String address;
    private Uri uri;

    private static final String TAG = "ImageEntity";
    private static final String DEFAULT_IMG = "android.resource://ivy2testing/app/src/main/res/drawable/ic_account_circle.xml";
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();


/* Constructors
***************************************************************************************************/

    public Image(String address, Uri uri){
        this(address);
        this.uri = uri;
    }

    public Image(String address){
        this.address = address;
        if (uri == null) loadUri();
    }

/* Setters and Getters
***************************************************************************************************/

    public String getAddress() {
        return address;
    }

    //TODO test this
    public Uri getUri() {
        if (uri == null) uri  = Uri.parse(DEFAULT_IMG);
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }


/* Parcelable Override Methods
***************************************************************************************************/

    protected Image(Parcel in) {
        address = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeParcelable(uri, flags);
    }

/* Firebase Methods
***************************************************************************************************/

    // Load preview pictures
    private void loadUri(){
        base_storage_ref.child(address).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Uri uri = task.getResult();
                        if (uri != null){
                            this.uri = uri;
                        }
                    }
                    else{
                        this.uri = Uri.parse(DEFAULT_IMG);
                        Log.w(TAG, "Uri doesn't exist at address: " + address);
                    }
                });
    }
}
