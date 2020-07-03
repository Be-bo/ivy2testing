package com.ivy2testing.terms;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;


public class LongTextViewHolder extends RecyclerView.ViewHolder {

    TextView title;
    TextView text;

    LongTextViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.item_long_text_title);
        text = itemView.findViewById(R.id.item_long_text_body);
    }
}
