package com.example.singh.myapplication.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.singh.myapplication.Interface.ItemClickListener;
import com.example.singh.myapplication.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    public TextView food_name,food_price;
    public ImageView food_image, fav_image, share_image,btn_quick_cart;

    private ItemClickListener itemClickListener;
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    public FoodViewHolder(View itemView)
    {
        super(itemView);

        food_name =(TextView)itemView.findViewById(R.id.food_name);
        food_image =(ImageView) itemView.findViewById(R.id.food_image);
        fav_image =(ImageView) itemView.findViewById(R.id.fav);
        share_image =(ImageView) itemView.findViewById(R.id.btnShare);
        food_price = (TextView) itemView.findViewById(R.id.food_price);
        btn_quick_cart = (ImageView) itemView.findViewById( R.id.btn_quick_cart );
        itemView.setOnClickListener(this);
    }


        @Override
    public void onClick(View v)
    {
        itemClickListener.onCLick(v,getAdapterPosition(),false);

    }


}
