package com.deadpeace.selfie.util;

import android.support.design.widget.TextInputLayout;
import android.view.View;

import com.deadpeace.selfie.R;

/**
 * Created by Виталий on 12.10.2015.
 */
public class ListenerChangeFocus implements View.OnFocusChangeListener
{
    @Override
    public void onFocusChange(View v,boolean hasFocus)
    {
        TextInputLayout text=(TextInputLayout)v.getParent();
        if(!hasFocus&&text.getEditText().getText().toString().trim().equals(""))
            text.setError(v.getContext().getString(R.string.msg_blank));
        else
            text.setErrorEnabled(false);
    }
}
