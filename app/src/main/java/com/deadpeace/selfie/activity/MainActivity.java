package com.deadpeace.selfie.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.deadpeace.selfie.R;
import com.deadpeace.selfie.adapter.SelfieAdapter;
import com.deadpeace.selfie.model.Selfie;
import com.deadpeace.selfie.model.User;
import com.deadpeace.selfie.util.Contract;
import com.deadpeace.selfie.util.SelfieConnection;
import com.deadpeace.selfie.util.SelfieUtil;
import com.deadpeace.selfie.loader.LoaderSelfie;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private SelfieAdapter mAdapter;
    private TextView userName;
    private ImageView userAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer=(DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView=(NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        userName=(TextView)findViewById(R.id.text_username);
        userAvatar=(ImageView)findViewById(R.id.profile_image);

        final ReentrantLock lock=new ReentrantLock();
        final Condition condition=lock.newCondition();


        if(SelfieUtil.getCurrentUser()==null)
            new LoaderSelfie<User>()
            {
                @Override
                public void success(User user)
                {
                    userName.setText(user.getUsername());
                    Log.i("Load-avatar","Thread 'getUser' is complete work!");
                }

                @Override
                public void fail(Exception e)
                {
                    Toast.makeText(getApplicationContext(),R.string.msg_fail_connected,Toast.LENGTH_LONG).show();
                    Log.i("Load-avatar","Thread 'getUser' have a Exception: "+e.getMessage());
                }

                @Override
                public User doInBackground()throws InterruptedException
                {
                    try
                    {
                        lock.lock();
                        SelfieUtil.setCurrentUser(SelfieConnection.getInstance().getUser());
                    }
                    finally
                    {
                        Log.i("Load-avatar","Thread 'getUser' sent signal!");
                        condition.signal();
                        lock.unlock();
                    }
                    return SelfieUtil.getCurrentUser();
                }
            }.start();
        else
            userName.setText(SelfieUtil.getCurrentUser().getUsername());


        new LoaderSelfie<Bitmap>()
        {
            @Override
            public void success(Bitmap bitmap)
            {
                try
                {
                    userAvatar.setImageBitmap(bitmap);
                    File file=new File(getCacheDir(),String.format(Contract.FILE_USER_PREVIEW,SelfieUtil.getCurrentUser().getUsername()));
                    if(!file.exists())
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,new BufferedOutputStream(new FileOutputStream(file)));
                    Log.i("Load-avatar","Thread 'loadAvatar' is complete work!");
                }
                catch(FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void fail(Exception e)
            {
                Log.i("Load-avatar","Thread 'loadAvatar' have a Exception: "+e.getMessage());
            }

            @Override
            public Bitmap doInBackground()throws InterruptedException
            {
                Bitmap bitmap=null;
                try
                {
                    lock.lock();
                    while(SelfieUtil.getCurrentUser()==null)
                    {
                        Log.i("Load-avatar","Thread 'loadAvatar' is sleeping!");
                        condition.await();
                    }
                    File file=new File(getCacheDir(),String.format(Contract.FILE_USER_PREVIEW,SelfieUtil.getCurrentUser().getUsername()));
                    bitmap=!file.exists()?SelfieUtil.convertToBitmap(SelfieConnection.getInstance().downloadPreviewAvatar(SelfieUtil.getCurrentUser().getUsername())):BitmapFactory.decodeFile(file.toString());
                }
                finally
                {
                    lock.unlock();
                }
                return bitmap;
            }
        }.start();
        RecyclerView mRecyclerView=(RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager mLayoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter=new SelfieAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void doAddSelfie(View v)
    {
        startActivityForResult(new Intent(this,SelfieActivity.class),Contract.CODE_SELFIE);
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer=(DrawerLayout)findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        switch(item.getItemId())
        {
            case R.id.profile:
                startActivity(new Intent(this,ChangeCredentialActivity.class));
                break;
            case R.id.reminder:
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                View reminder=getLayoutInflater().inflate(R.layout.reminder,null);
                final TimePicker picker=(TimePicker)reminder.findViewById(R.id.timePicker);
                final CheckBox checkBox=(CheckBox)reminder.findViewById(R.id.cb_reminder);
                picker.setIs24HourView(DateFormat.is24HourFormat(getApplicationContext()));
                SharedPreferences preferences=getSharedPreferences(Contract.TIME_REMINDER,MODE_PRIVATE);
                checkBox.setChecked(preferences.getBoolean(Contract.DO_REMIND,false));
                picker.setCurrentHour(preferences.getInt(Contract.HOUR,12));
                picker.setCurrentMinute(preferences.getInt(Contract.MINUTE,0));
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
                    {
                        picker.setEnabled(!isChecked);
                    }
                });
                builder.setTitle(R.string.reminder)
                        .setView(reminder)
                        .setPositiveButton(R.string.done,new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog,int which)
                            {
                                SharedPreferences.Editor editor=getSharedPreferences(Contract.TIME_REMINDER,MODE_PRIVATE).edit();
                                editor.putInt(Contract.HOUR,picker.getCurrentHour());
                                editor.putInt(Contract.MINUTE,picker.getCurrentMinute());
                                editor.putBoolean(Contract.DO_REMIND,checkBox.isChecked());
                                editor.apply();
                                SelfieUtil.cancelAlarm(getApplicationContext());
                                if(!checkBox.isChecked())
                                    SelfieUtil.startAlarm(getApplicationContext(),picker.getCurrentHour(),picker.getCurrentMinute());
                            }
                        }).create().show();
                break;
            case R.id.logout:
                new LoaderSelfie<Void>()
                {
                    @Override
                    public void success(Void aVoid)
                    {
                        SharedPreferences.Editor editor=getSharedPreferences(Contract.CREDENTIAL,MODE_PRIVATE).edit();
                        editor.remove(Contract.USERNAME);
                        editor.remove(Contract.PASSWORD);
                        editor.apply();
                        SelfieUtil.setCurrentUser(null);
                        startActivity(new Intent(MainActivity.this,LoginActivity.class));
                        MainActivity.this.finish();
                    }

                    @Override
                    public void fail(Exception e)
                    {

                    }

                    @Override
                    public Void doInBackground() throws InterruptedException
                    {
                        SelfieConnection.getInstance().logout();
                        return null;
                    }
                }.start();
            default:
                break;
        }
        DrawerLayout drawer=(DrawerLayout)findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        if(requestCode==Contract.CODE_SELFIE && resultCode==RESULT_OK)
            mAdapter.saveSelfie((Selfie)data.getSerializableExtra(Contract.SELFIE));
        else
            super.onActivityResult(requestCode,resultCode,data);
    }
}
