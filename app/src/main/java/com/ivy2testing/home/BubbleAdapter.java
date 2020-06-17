package com.ivy2testing.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;

import java.util.ArrayList;

public class BubbleAdapter extends RecyclerView.Adapter<BubbleAdapter.BubbleViewHolder>{
    private ArrayList<String> bubble_arraylist;




    public static class BubbleViewHolder extends RecyclerView.ViewHolder{
        public Button bubble;

        public BubbleViewHolder(@NonNull View itemView) {
            super(itemView);
            bubble = itemView.findViewById(R.id.bubblez);
        }

    }

    public BubbleAdapter(ArrayList<String> bubble_list){
        bubble_arraylist = bubble_list;


    }
    @NonNull
    @Override
    public BubbleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bubble_layout, parent, false);
        BubbleViewHolder bvh = new BubbleViewHolder(v);
        return bvh;
    }

    @Override
    public void onBindViewHolder(@NonNull BubbleViewHolder holder, int position) {
        holder.bubble.setText(bubble_arraylist.get(position));

    }

    @Override
    public int getItemCount() {
        return bubble_arraylist.size();
    }
}
