package com.ivy2testing.chat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.SortedList;

/**
 * An extension of Android's "SortedList" Data Structure
 * Uses a different method to search for an item so changes to sorting criteria are more feasible
 */
public class ExtendedSortedList<T> extends SortedList<T> {

    Callback<T> mCallback;

    public ExtendedSortedList(@NonNull Class<T> klass, @NonNull Callback<T> callback) {
        super(klass, callback);
        mCallback = callback;
    }

    /**
     * Returns the position of the provided item by searching by ID instead of comparator
     * More expensive since list is not sorted by ID. So only use if comparator of item is updated
     *
     * @param item The item to query for position.
     *
     * @return The position of the provided item or {@link #INVALID_POSITION} if item is not in the
     * list.
     */
    public int findIndexById(T item){
        for (int i = 0; i < super.size(); i++){
            if (mCallback.areItemsTheSame(super.get(i), item))
                return i;
        }
        return INVALID_POSITION;
    }
}
