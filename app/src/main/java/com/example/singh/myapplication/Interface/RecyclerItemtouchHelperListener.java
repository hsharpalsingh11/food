package com.example.singh.myapplication.Interface;

import android.support.v7.widget.RecyclerView;

public interface RecyclerItemtouchHelperListener
{
    void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
}
