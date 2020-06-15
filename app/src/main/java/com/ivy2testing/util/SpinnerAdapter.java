package com.ivy2testing.util;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ivy2testing.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<String> {

    Context mContext;
    String[] mList;

    public SpinnerAdapter(Context context, String[] list){
        super(context, android.R.layout.simple_spinner_item, new ArrayList<>(Arrays.asList(list)));
        mContext = context;
        mList = list;
        Log.d("SPINNER ADAPTER", ""+ mList[0]);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        // Remove left padding
        view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom() + 1);

        // If showing 1st item, change colour to lighter grey
        if (position == 0)
            ((TextView) view).setTextColor(mContext.getResources().getColor(R.color.hint, mContext.getTheme()));

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return super.getDropDownView(position + 1, convertView, parent);
    }

    @Override
    public int getCount() {
        return mList.length - 1;
    }
}
