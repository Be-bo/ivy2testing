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

/** @author Zahra Ghavasieh
 * Overview: A Spinner adapter that uses the first item in given String[] as a "hint"
 * Notes: the "hint" item has a different colour and cannot be selected from the drop-down menu but it's still shown
 */
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
        View view = super.getDropDownView(position, convertView, parent);

        // If showing 1st item, change colour to lighter grey
        TextView tv = (TextView) view;
        if (position == 0)
            tv.setTextColor(mContext.getResources().getColor(R.color.hint, mContext.getTheme()));
        else
            tv.setTextColor(mContext.getResources().getColor(R.color.off_black, mContext.getTheme()));

        return tv;
    }

    @Override
    public int getCount() {
        return mList.length;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0; // Disable item [0] from spinner
    }
}
