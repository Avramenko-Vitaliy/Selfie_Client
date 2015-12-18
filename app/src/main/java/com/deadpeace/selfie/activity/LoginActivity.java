package com.deadpeace.selfie.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.deadpeace.selfie.R;
import com.deadpeace.selfie.util.Contract;
import com.deadpeace.selfie.util.ListenerChangeFocus;
import com.deadpeace.selfie.util.SelfieConnection;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Виталий on 11.10.2015.
 */
public class LoginActivity extends Activity
{
    private EditText login;
    private EditText password;
    private Button btnSignIn;
    private Button btnSignUp;
    private Button btnSignCancel;
    private CheckBox cbRemember;
    private ListenerChangeFocus focus=new ListenerChangeFocus();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        login=(EditText)findViewById(R.id.edit_login);
        password=(EditText)findViewById(R.id.edit_pass);
        btnSignIn=(Button)findViewById(R.id.btn_enter);
        btnSignUp=(Button)findViewById(R.id.btn_register);
        btnSignCancel=(Button)findViewById(R.id.btn_cancel);
        cbRemember=(CheckBox)findViewById(R.id.cb_remember);
        password.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v,int actionId,KeyEvent event)
            {
                boolean result=actionId==EditorInfo.IME_ACTION_DONE;
                if(result)
                    btnSignIn.callOnClick();
                return result;
            }
        });
        setEnabled(true);
        SharedPreferences preferences=getSharedPreferences(Contract.CREDENTIAL,MODE_PRIVATE);
        String strUsername=preferences.getString(Contract.USERNAME,"").trim();
        String strPasword=preferences.getString(Contract.PASSWORD,"").trim();
        if(!strPasword.equals("")&&!strUsername.equals(""))
        {
            login.setText(strUsername);
            password.setText(strPasword);
            btnSignIn.callOnClick();
        }
        login.setOnFocusChangeListener(focus);
        password.setOnFocusChangeListener(focus);
    }

    public void doSignIn(View v)
    {
        if(!login.getText().toString().trim().equals("") && !password.getText().toString().trim().equals(""))
        {
            setEnabled(false);
            SelfieConnection.getInstance().login(login.getText().toString().trim(),password.getText().toString().trim(),new Callback<Void>()
            {
                @Override
                public void success(Void aVoid,Response response)
                {
                    if(cbRemember.isChecked())
                        saveLogin(login.getText().toString(),password.getText().toString());
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    LoginActivity.this.finish();
                }

                @Override
                public void failure(RetrofitError error)
                {
                    Toast.makeText(getApplicationContext(),R.string.msg_fail_connected,Toast.LENGTH_LONG).show();
                    setEnabled(true);
                }
            });
        }
        else
            Toast.makeText(getApplicationContext(),R.string.msg_empty_credentials,Toast.LENGTH_LONG).show();
    }

    public void doRegister(View view)
    {
        startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
    }

    public void doCancel(View view)
    {
        LoginActivity.this.finish();
    }

    private void saveLogin(String username,String password)
    {
        SharedPreferences.Editor editor=getSharedPreferences(Contract.CREDENTIAL,MODE_PRIVATE).edit();
        editor.putString(Contract.USERNAME,username.trim());
        editor.putString(Contract.PASSWORD,password.trim());
        editor.apply();
    }

    private void setEnabled(boolean enabled)
    {
        password.setEnabled(enabled);
        login.setEnabled(enabled);
        btnSignIn.setEnabled(enabled);
        btnSignCancel.setEnabled(enabled);
        btnSignUp.setEnabled(enabled);
        cbRemember.setEnabled(enabled);
    }
}
