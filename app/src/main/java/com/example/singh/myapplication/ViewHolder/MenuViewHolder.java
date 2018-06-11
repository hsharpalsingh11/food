package com.example.singh.myapplication.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.singh.myapplication.Interface.ItemClickListener;
import com.example.singh.myapplication.R;

import org.w3c.dom.Text;

public class MenuViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener
{

    public TextView txtMenuName;
    public ImageView imageView;

    private ItemClickListener itemClickListener;

    public MenuViewHolder(View itemView)
    {
        super(itemView);

        txtMenuName =(TextView)itemView.findViewById(R.id.menu_name);
        imageView =(ImageView) itemView.findViewById(R.id.menu_image);
        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v)
    {
        itemClickListener.onCLick(v,getAdapterPosition(),false);

    }
}
