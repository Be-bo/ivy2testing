package com.ivy2testing.terms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;

import java.util.ArrayList;

public class LongTextAdapter extends RecyclerView.Adapter<LongTextViewHolder> {

    private ArrayList<LongTextModel> all_paragraphs = new ArrayList<LongTextModel>();
    private Context context;

    public LongTextAdapter(Context con, ArrayList<LongTextModel> initialParagraphs){
        this.context = con;
        this.all_paragraphs = initialParagraphs;
    }

    @NonNull
    @Override
    public LongTextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_long_text, parent, false);
        return new LongTextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LongTextViewHolder holder, int position) {
        LongTextModel currentParagraph = all_paragraphs.get(position);
        if(currentParagraph.getTitle().equals("")){
            holder.title.setVisibility(View.GONE);
        }else{
            holder.title.setText(currentParagraph.getTitle());
            holder.title.setVisibility(View.VISIBLE);
        }
        if(currentParagraph.getText().equals("")){
            holder.text.setVisibility(View.GONE);
        }else{
            holder.text.setText(currentParagraph.getText());
            holder.text.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return all_paragraphs.size();
    }

    public void appendParagraph(LongTextModel newParagraph){
        all_paragraphs.add(newParagraph);
        notifyDataSetChanged();
    }

    public ArrayList<LongTextModel> getAll_paragraphs() {
        return all_paragraphs;
    }

    public void setAll_paragraphs(ArrayList<LongTextModel> all_paragraphs) {
        this.all_paragraphs = all_paragraphs;
        notifyDataSetChanged();
    }
}
