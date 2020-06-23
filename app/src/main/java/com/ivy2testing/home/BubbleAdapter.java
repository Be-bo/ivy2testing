package com.ivy2testing.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.R;

import java.util.ArrayList;

public class BubbleAdapter extends RecyclerView.Adapter<BubbleAdapter.BubbleViewHolder>{
    private ArrayList<String> bubble_arraylist;
    private BubbleViewHolder.BubbleClickListener bubble_click_listener;




    public static class BubbleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public Button bubble;
        BubbleClickListener bubble_click_listener;

        public BubbleViewHolder(@NonNull View itemView, BubbleClickListener bubble_click_listener) {
            super(itemView);
            bubble = itemView.findViewById(R.id.bubble_button);
            this.bubble_click_listener = bubble_click_listener;
            bubble.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            bubble_click_listener.onBubbleClick(getAdapterPosition());

        }

        public interface BubbleClickListener{
            void onBubbleClick(int position);
        }

    }

    public BubbleAdapter(ArrayList<String> bubble_list, BubbleViewHolder.BubbleClickListener bubble_click_listener){
        bubble_arraylist = bubble_list;
        this.bubble_click_listener = bubble_click_listener;


    }
    @NonNull
    @Override
    public BubbleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bubble_layout, parent, false);
        BubbleViewHolder bvh = new BubbleViewHolder(v, bubble_click_listener);
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
