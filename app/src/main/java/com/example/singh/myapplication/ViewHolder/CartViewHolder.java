package com.example.singh.myapplication.ViewHolder;


import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.singh.myapplication.Common.Common;
import com.example.singh.myapplication.Interface.ItemClickListener;
import com.example.singh.myapplication.R;

public class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnCreateContextMenuListener
{
    public TextView txt_cart_name,txt_price;
    public ElegantNumberButton img_cart_count;
    public ImageView cart_image;
    public RelativeLayout view_background;
    public LinearLayout view_foreground;

    private ItemClickListener itemClickListener;

    public void setTxt_cart_name(TextView txt_cart_name)
    {
        this.txt_cart_name = txt_cart_name;
    }

    public CartViewHolder(View itemView)
    {
        super(itemView);
        txt_cart_name = (TextView)itemView.findViewById(R.id.cart_item_name);
        txt_price = (TextView)itemView.findViewById(R.id.cart_item_price);
        img_cart_count =(ElegantNumberButton) itemView.findViewById(R.id.btnQuantiy);
        cart_image = (ImageView) itemView.findViewById( R.id.cart_image );
        view_background = (RelativeLayout)itemView.findViewById(R.id.view_background);
        view_foreground = (LinearLayout) itemView.findViewById(R.id.view_foreground);
        itemView.setOnCreateContextMenuListener( this );
    }

    @Override
    public void onClick(View v)
    {

    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle( "Select Action" );
        contextMenu.add( 0,0,getAdapterPosition(), Common.DELETE);

    }
}
