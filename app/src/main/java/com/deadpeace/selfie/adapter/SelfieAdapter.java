package com.deadpeace.selfie.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.deadpeace.selfie.R;
import com.deadpeace.selfie.activity.SelfieActivity;
import com.deadpeace.selfie.model.Selfie;
import com.deadpeace.selfie.util.Contract;
import com.deadpeace.selfie.util.SelfieConnection;
import com.deadpeace.selfie.util.SelfieUtil;
import com.deadpeace.selfie.loader.LoaderSelfie;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Виталий on 11.10.2015.
 */

public class SelfieAdapter extends RecyclerView.Adapter<SelfieAdapter.ViewHolder>
{
    private static List<Selfie> selfis;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public View mContainer;
        public ImageView mImage;
        public TextView mTitle;
        public TextView mTimestamp;

        public ViewHolder(View v)
        {
            super(v);
            mContainer=v;
            mTitle=(TextView)v.findViewById(R.id.text_item_title);
            mImage=(ImageView)v.findViewById(R.id.image_item_selfie);
            mTimestamp=(TextView)v.findViewById(R.id.text_item_timestamp);
        }
    }

    public SelfieAdapter(Context context)
    {
        mContext=context;
        refresh();
    }

    public void refresh()
    {
        new LoaderSelfie<List<Selfie>>()
        {
            @Override
            public void success(List<Selfie> selfies)
            {
                selfis=selfies;
                notifyDataSetChanged();
            }

            @Override
            public void fail(Exception e)
            {
                Toast.makeText(mContext,R.string.fail_load,Toast.LENGTH_LONG).show();
            }

            @Override
            public List<Selfie> doInBackground() throws InterruptedException
            {
                return Collections.synchronizedList(SelfieConnection.getInstance().getSelfieList());
            }
        }.start();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType)
    {
        View v=LayoutInflater.from(mContext).inflate(R.layout.item_selfie,parent,false);
        ViewHolder vh=new ViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,int position)
    {
        if(selfis!=null)
        {
            final Selfie selfie=selfis.get(position);
            new LoaderSelfie<Bitmap>()
            {
                @Override
                public void success(Bitmap bitmap)
                {
                    try
                    {
                        holder.mImage.setImageBitmap(bitmap);
                        File file=new File(mContext.getCacheDir(),String.format(Contract.FILE_SELFIE_PREVIEW,selfie.getId()));
                        if(!file.exists())
                            bitmap.compress(Bitmap.CompressFormat.JPEG,100,new BufferedOutputStream(new FileOutputStream(file)));
                    }
                    catch(FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void fail(Exception e)
                {

                }

                @Override
                public Bitmap doInBackground() throws InterruptedException
                {
                    File file=new File(mContext.getCacheDir(),String.format(Contract.FILE_SELFIE_PREVIEW,selfie.getId()));
                    return !file.exists()?SelfieUtil.convertToBitmap(SelfieConnection.getInstance().downloadPreviewSelfie(selfie.getId())):BitmapFactory.decodeFile(file.toString());
                }
            }.start();
            holder.mTitle.setText(selfie.getTitle());
            holder.mTimestamp.setText(DateFormat.getDateFormat(mContext).format(new Date(selfie.getDate()))+" "+DateFormat.getTimeFormat(mContext).format(new Date(selfie.getDate())));
            holder.mContainer.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.item_adapter));
            holder.mContainer.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent=new Intent(mContext,SelfieActivity.class);
                    intent.putExtra(Contract.SELFIE,selfie);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return selfis!=null?selfis.size():0;
    }

    public void saveSelfie(Selfie selfie)
    {
        int pos=selfis.indexOf(selfie);
        if(pos>=0)
        {
            Selfie s=selfis.get(pos);
            s.setTitle(selfie.getTitle());
            s.setDate(selfie.getDate());
            s.setDescription(selfie.getDescription());
        }
        else
            selfis.add(selfie);
        notifyDataSetChanged();
    }
}
