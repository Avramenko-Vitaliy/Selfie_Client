package com.deadpeace.selfie.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.deadpeace.selfie.R;
import com.deadpeace.selfie.util.ListenerChangeFocus;
import com.deadpeace.selfie.util.SelfieConnection;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Виталий on 11.10.2015.
 */
public class RegisterActivity extends Activity
{
    private EditText rLogin;
    private EditText rPassword;
    private EditText rcPassword;
    private EditText rEmail;
    private FloatingActionButton rFab;
    private ListenerChangeFocus focus=new ListenerChangeFocus();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        rLogin=(EditText)findViewById(R.id.edit_reg_login);
        rPassword=(EditText)findViewById(R.id.edit_reg_pass);
        rcPassword=(EditText)findViewById(R.id.edit_reg_cpass);
        rEmail=(EditText)findViewById(R.id.edit_reg_email);
        rFab=(FloatingActionButton)findViewById(R.id.btn_reg);
        rEmail.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v,int actionId,KeyEvent event)
            {
                boolean result=actionId==EditorInfo.IME_ACTION_DONE;
                if(result)
                    rFab.callOnClick();
                return result;
            }
        });
        rLogin.setOnFocusChangeListener(focus);
        rPassword.setOnFocusChangeListener(focus);
        rcPassword.setOnFocusChangeListener(focus);
        rEmail.setOnFocusChangeListener(focus);
        setEnabled(true);
    }

    public void doRegister(View view)
    {
        if(!rLogin.getText().toString().trim().equals("")&&!rPassword.getText().toString().trim().equals("")&&!rcPassword.getText().toString().trim().equals("")&&!rEmail.getText().toString().trim().equals(""))
            if(!rPassword.getText().toString().trim().equals(rcPassword.getText().toString().trim()))
                Toast.makeText(getApplicationContext(),R.string.msg_pass_not_equals,Toast.LENGTH_LONG).show();
            else
            {
                setEnabled(false);
                SelfieConnection.getInstance().register(rLogin.getText().toString().trim(),rPassword.getText().toString().trim(),rEmail.getText().toString().trim(),new Callback<Void>()
                {
                    @Override
                    public void success(Void aVoid,Response response)
                    {
                        RegisterActivity.this.finish();
                        setEnabled(true);
                    }

                    @Override
                    public void failure(RetrofitError error)
                    {
                        Toast.makeText(getApplicationContext(),R.string.msg_fail_register,Toast.LENGTH_LONG).show();
                        setEnabled(true);
                    }
                });
            }
        else
            Toast.makeText(getApplicationContext(),R.string.msg_empty_credentials,Toast.LENGTH_LONG).show();
    }

    private void setEnabled(boolean enabled)
    {
        rPassword.setEnabled(enabled);
        rLogin.setEnabled(enabled);
        rFab.setEnabled(enabled);
        rcPassword.setEnabled(enabled);
        rEmail.setEnabled(enabled);
    }
}
