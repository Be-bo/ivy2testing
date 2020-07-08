package com.ivy2testing.util;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog d = new android.app.DatePickerDialog(getActivity(), (android.app.DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);


        return new android.app.DatePickerDialog(getActivity(), (android.app.DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
    }
}
