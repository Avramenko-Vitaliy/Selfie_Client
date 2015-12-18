package com.deadpeace.selfie.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.cocosw.bottomsheet.BottomSheet;
import com.deadpeace.selfie.R;
import com.deadpeace.selfie.model.Selfie;
import com.deadpeace.selfie.util.Contract;
import com.deadpeace.selfie.util.SelfieConnection;
import com.deadpeace.selfie.util.SelfieUtil;
import com.deadpeace.selfie.util.StatusSelfie;
import com.deadpeace.selfie.loader.LoaderSelfie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit.mime.TypedFile;

/**
 * Created by Виталий on 12.10.2015.
 */
public class SelfieActivity extends AppCompatActivity
{
    private ViewSwitcher titleSwitcher;
    private ViewSwitcher descSwitcher;
    private Toolbar toolbar;
    private StatusSelfie status=StatusSelfie.SHOWING;
    private FloatingActionButton fabEdit;
    private ImageView vSelfie;
    private TextView vDate;
    private ExecutorService executor=Executors.newFixedThreadPool(1);

    private Selfie selfie=new Selfie();
    private Bitmap bitmap;
    private Uri uri;

    private View.OnClickListener selectImage=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            new BottomSheet.Builder(SelfieActivity.this,R.style.BottomSheet_StyleDialog).
                    grid().
                    sheet(R.menu.menu_bottom_sheet).
                    listener(new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog,int which)
                        {
                            switch(which)
                            {
                                case R.id.bs_camera:
                                    try
                                    {
                                        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        uri=Uri.fromFile(SelfieUtil.createImageFile());
                                        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                                        startActivityForResult(intent,Contract.PICK_CAMERA);
                                    }
                                    catch(IOException e)
                                    {
                                        e.printStackTrace();
                                    }
                                    break;
                                case R.id.bs_gallery:
                                    Intent intent=new Intent();
                                    intent.setType("image/jpeg");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent,"Select Picture"),Contract.PICK_IMAGE);
                                    break;
                            }
                        }
                    }).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selfie);
        toolbar=(Toolbar)findViewById(R.id.selfie_toolbar);
        setSupportActionBar(toolbar);
        titleSwitcher=(ViewSwitcher)findViewById(R.id.switcher_title);
        descSwitcher=(ViewSwitcher)findViewById(R.id.switcher_desc);
        fabEdit=(FloatingActionButton)findViewById(R.id.btn_edit);
        vSelfie=(ImageView)findViewById(R.id.img_selfie);
        vDate=(TextView)findViewById(R.id.lb_date);

        if(savedInstanceState!=null)
        {
            if(savedInstanceState.containsKey(Contract.SELFIE))
                selfie=(Selfie)savedInstanceState.getSerializable(Contract.SELFIE);
            if(savedInstanceState.containsKey(Contract.STATUS))
                status=StatusSelfie.values()[savedInstanceState.getInt(Contract.STATUS)];
            if(savedInstanceState.containsKey(Contract.BITMAP))
            {
                ByteArrayInputStream stream=new ByteArrayInputStream(savedInstanceState.getByteArray(Contract.BITMAP));
                bitmap=BitmapFactory.decodeStream(stream);
                vSelfie.setImageBitmap(bitmap);
            }
            if(savedInstanceState.containsKey(Contract.URI_FILE))
                uri=Uri.parse(savedInstanceState.getString(Contract.URI_FILE));
        }
        else
        {
            Intent intent=getIntent();
            if(intent.hasExtra(Contract.SELFIE))
                selfie=(Selfie)intent.getSerializableExtra(Contract.SELFIE);
        }
        if(bitmap==null)
            new LoaderSelfie<Bitmap>()
            {
                @Override
                public void success(Bitmap bitmap)
                {
                    SelfieActivity.this.bitmap=bitmap;
                    vSelfie.setImageBitmap(bitmap);
                }

                @Override
                public void fail(Exception e)
                {

                }

                @Override
                public Bitmap doInBackground() throws InterruptedException
                {
                    return SelfieUtil.convertToBitmap(SelfieConnection.getInstance().downloadSelfie(selfie.getId()));
                }
            }.start();
        setVisibleMenu(toolbar.getMenu(),status==StatusSelfie.EDITING);
        if(selfie.getId()<=0)
        {
            status=StatusSelfie.EDITING;
            vSelfie.setOnClickListener(selectImage);
        }
        else
            fabEdit.setVisibility(selfie.getCreator().equals(SelfieUtil.getCurrentUser())?View.VISIBLE:View.INVISIBLE);
        if(StatusSelfie.EDITING==status)
            changeView();
        ((TextView)titleSwitcher.getCurrentView()).setText(selfie.getTitle());
        ((TextView)descSwitcher.getCurrentView()).setText(selfie.getDescription());
        vDate.setText(DateFormat.getDateFormat(getApplicationContext()).format(new Date(selfie.getDate())));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.selfie_menu,menu);
        setVisibleMenu(menu,status==StatusSelfie.EDITING);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        if(bitmap!=null)
        {
            executor.execute(new LoaderSelfie<Bitmap>()
            {
                @Override
                public void success(Bitmap bitmap)
                {
                    SelfieActivity.this.bitmap=bitmap;
                    vSelfie.setImageBitmap(bitmap);
                }

                @Override
                public void fail(Exception e)
                {
                    Toast.makeText(getApplicationContext(),R.string.msg_fail_upload,Toast.LENGTH_LONG).show();
                }

                @Override
                public Bitmap doInBackground() throws InterruptedException
                {
                    File file=new File(getCacheDir(),String.format(Contract.FILE_SELFIE,selfie.getId()));
                    try
                    {
                        FileOutputStream stream=new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                    }
                    catch(FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    switch(item.getItemId())
                    {
                        case R.id.menu_brightness:
                            return SelfieUtil.convertToBitmap(SelfieConnection.getInstance().toBrightness(new TypedFile("image/jpeg",file),20));
                        case R.id.menu_black_white:
                            return SelfieUtil.convertToBitmap(SelfieConnection.getInstance().toBlackAndWhite(new TypedFile("image/jpeg",file)));
                        /*case R.id.menu_rotate_left:
                            return Contract.convertToBitmap(SelfieConnection.getInstance().rotate(new TypedFile("image/jpeg",file),-90));
                        case R.id.menu_rotate_right:
                            return Contract.convertToBitmap(SelfieConnection.getInstance().rotate(new TypedFile("image/jpeg",file),50));*/
                        case R.id.menu_sepia:
                            return SelfieUtil.convertToBitmap(SelfieConnection.getInstance().toSepia(new TypedFile("image/jpeg",file),80));
                        default:
                            return null;
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    private void setVisibleMenu(Menu menu,boolean visible)
    {
        for(int i=0;i<menu.size();i++)
            menu.getItem(i).setVisible(visible);
    }

    public void doEdit(View view)
    {
        fabEdit.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_hide));
        status=status==StatusSelfie.EDITING?StatusSelfie.SHOWING:StatusSelfie.EDITING;
        changeView();
        fabEdit.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_show));
        if(status==StatusSelfie.SHOWING)
        {
            vSelfie.setOnClickListener(null);
            saveSelfie();
        }
        else
            vSelfie.setOnClickListener(selectImage);
    }

    private void changeView()
    {
        setVisibleMenu(toolbar.getMenu(),status==StatusSelfie.EDITING);
        selfie.setTitle(((TextView)titleSwitcher.getCurrentView()).getText().toString());
        if(status==StatusSelfie.EDITING)
            titleSwitcher.showNext();
        else
            titleSwitcher.showPrevious();
        ((TextView)titleSwitcher.getCurrentView()).setText(selfie.getTitle());
        selfie.setDescription(((TextView)descSwitcher.getCurrentView()).getText().toString());
        if(status==StatusSelfie.EDITING)
            descSwitcher.showNext();
        else
            descSwitcher.showPrevious();
        ((TextView)descSwitcher.getCurrentView()).setText(selfie.getDescription());
        fabEdit.setImageResource(status==StatusSelfie.EDITING?R.drawable.ic_done_white_16dp_2x:R.mipmap.ic_edit_white_18dp);
    }

    @Override
    public void onBackPressed()
    {
        if(status==StatusSelfie.EDITING)
        {
            titleSwitcher.showPrevious();
            descSwitcher.showPrevious();
            status=StatusSelfie.SHOWING;
            fabEdit.setImageResource(status==StatusSelfie.EDITING?R.drawable.ic_done_white_16dp_2x:R.mipmap.ic_edit_white_18dp);
        }
        else
            super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        if(selfie!=null)
            outState.putSerializable(Contract.SELFIE,selfie);
        if(bitmap!=null)
        {
            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
            outState.putByteArray(Contract.BITMAP,stream.toByteArray());
        }
        if(uri!=null)
            outState.putString(Contract.URI_FILE,uri.toString());
        outState.putInt(Contract.STATUS,status.ordinal());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        if(resultCode==RESULT_OK)
        {
            switch(requestCode)
            {
                case Contract.PICK_IMAGE:
                    Uri uri=data.getData();
                    String[] projection={MediaStore.Images.Media.DATA};
                    Cursor cursor=getContentResolver().query(uri,projection,null,null,null);
                    cursor.moveToFirst();
                    int columnIndex=cursor.getColumnIndex(projection[0]);
                    String picturePath=cursor.getString(columnIndex);
                    cursor.close();
                    bitmap=BitmapFactory.decodeFile(picturePath);
                    vSelfie.setImageBitmap(bitmap);
                    break;
                case Contract.PICK_CAMERA:
                    try
                    {
                            Intent mediaScanIntent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            mediaScanIntent.setData(this.uri);
                            this.sendBroadcast(mediaScanIntent);
                            bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),this.uri);
                            vSelfie.setImageBitmap(bitmap);
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    private void saveSelfie()
    {
        new LoaderSelfie<Selfie>()
        {
            @Override
            public void success(final Selfie selfie)
            {
                SelfieActivity.this.selfie=selfie;
                final ProgressDialog dialog=ProgressDialog.show(SelfieActivity.this,"",getApplicationContext().getResources().getString(R.string.pb_message),true);
                new LoaderSelfie<String>()
                {
                    @Override
                    public void success(String s)
                    {
                        dialog.dismiss();
                        if(!s.equals("Done!"))
                            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
                        else
                        {
                            new File(getCacheDir(),String.format(Contract.FILE_SELFIE_PREVIEW,selfie.getId())).delete();
                            new File(getCacheDir(),String.format(Contract.FILE_SELFIE,selfie.getId())).delete();
                            Intent intent=new Intent();
                            intent.putExtra(Contract.SELFIE,selfie);
                            SelfieActivity.this.setResult(RESULT_OK,intent);
                            finish();
                        }
                    }

                    @Override
                    public void fail(Exception e)
                    {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(),R.string.msg_fail_upload,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public String doInBackground() throws InterruptedException
                    {
                        File file=new File(getCacheDir(),String.format(Contract.FILE_SELFIE,selfie.getId()));
                        try
                        {
                            FileOutputStream stream=new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                        }
                        catch(FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                        return SelfieConnection.getInstance().uploadSelfie(new TypedFile("image/jpeg",file),selfie.getId());
                    }
                }.start();
            }

            @Override
            public void fail(Exception e)
            {

            }

            @Override
            public Selfie doInBackground() throws InterruptedException
            {
                return SelfieConnection.getInstance().addSelfie(selfie);
            }
        }.start();
    }

    @Override
    protected void onDestroy()
    {
        executor.shutdown();
        super.onDestroy();
    }
}
