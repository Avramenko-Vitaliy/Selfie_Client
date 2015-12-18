package com.deadpeace.selfie.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.deadpeace.selfie.R;
import com.deadpeace.selfie.model.User;
import com.deadpeace.selfie.util.Contract;
import com.deadpeace.selfie.util.SelfieConnection;
import com.deadpeace.selfie.util.SelfieUtil;
import com.deadpeace.selfie.loader.LoaderSelfie;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import retrofit.mime.TypedFile;

/**
 * Created by Виталий on 15.11.2015.
 */
public class ChangeCredentialActivity extends Activity
{
    private EditText password,confirmPassword,eMail;
    private ImageView avatar;
    private Bitmap bitmap;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_credential);
        eMail=((EditText)findViewById(R.id.edit_email));
        password=((EditText)findViewById(R.id.edit_pass));
        confirmPassword=((EditText)findViewById(R.id.edit_cpass));
        avatar=((ImageView)findViewById(R.id.imgAvatar));
        eMail.setText(SelfieUtil.getCurrentUser().getEmail());
        avatar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new BottomSheet.Builder(ChangeCredentialActivity.this,R.style.BottomSheet_StyleDialog).
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
        });
        new LoaderSelfie<Bitmap>()
        {
            @Override
            public void success(Bitmap bitmap)
            {
                ChangeCredentialActivity.this.bitmap=bitmap;
                avatar.setImageBitmap(bitmap);
            }

            @Override
            public void fail(Exception e)
            {

            }

            @Override
            public Bitmap doInBackground() throws InterruptedException
            {
                return SelfieUtil.convertToBitmap(SelfieConnection.getInstance().downloadAvatar(SelfieUtil.getCurrentUser().getUsername()));
            }
        }.start();
    }

    public void saveCredential(View v)
    {
        if(password.getText().toString().trim().equals(confirmPassword.getText().toString().trim()))
        {
            new LoaderSelfie<User>()
            {
                @Override
                public void success(final User user)
                {
                    final ProgressDialog dialog=ProgressDialog.show(ChangeCredentialActivity.this,"",getApplicationContext().getResources().getString(R.string.pb_message),true);
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
                                new File(getCacheDir(),String.format(Contract.FILE_USER,user.getUsername())).delete();
                                new File(getCacheDir(),String.format(Contract.FILE_USER_PREVIEW,user.getUsername())).delete();
                                ChangeCredentialActivity.this.finish();
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
                            File file=new File(getCacheDir(),String.format(Contract.FILE_USER,user.getUsername()));
                            try
                            {
                                FileOutputStream stream=new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                            }
                            catch(FileNotFoundException e)
                            {
                                e.printStackTrace();
                            }
                            return SelfieConnection.getInstance().uploadAvatar(new TypedFile("image/jpeg",file));
                        }
                    }.start();
                    SelfieUtil.setCurrentUser(user);
                }

                @Override
                public void fail(Exception e)
                {
                    Toast.makeText(ChangeCredentialActivity.this,R.string.msg_fail_upload,Toast.LENGTH_LONG).show();
                }

                @Override
                public User doInBackground() throws InterruptedException
                {
                    return SelfieConnection.getInstance().changeCredential(password.getText().toString().trim(),eMail.getText().toString().trim());
                }
            }.start();
        }
        else
            Toast.makeText(this,"",Toast.LENGTH_LONG).show();
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
                    avatar.setImageBitmap(bitmap);
                    break;
                case Contract.PICK_CAMERA:
                    try
                    {
                        Intent mediaScanIntent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaScanIntent.setData(this.uri);
                        this.sendBroadcast(mediaScanIntent);
                        bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),this.uri);
                        avatar.setImageBitmap(bitmap);
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
}
