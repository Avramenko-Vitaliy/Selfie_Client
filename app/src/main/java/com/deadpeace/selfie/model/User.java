package com.deadpeace.selfie.model;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * Created by Виталий on 06.10.2015.
 */

public class User implements Serializable
{
    private long id;
    private String username;
    private String email;
    private boolean locked=false;
    private boolean enabled=true;
    private boolean credentials_expired=false;
    private boolean account_expired=false;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id=id;
    }

    public void setUsername(String username)
    {
        this.username=username;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email=email;
    }

    public void setLocked(boolean locked)
    {
        this.locked=locked;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled=enabled;
    }

    public void setCredentials_expired(boolean credentials_expired)
    {
        this.credentials_expired=credentials_expired;
    }

    public void setAccount_expired(boolean account_expired)
    {
        this.account_expired=account_expired;
    }

    public String getUsername()
    {
        return username;
    }

    public boolean isAccountNonExpired()
    {
        return !account_expired;
    }

    public boolean isAccountNonLocked()
    {
        return !locked;
    }

    public boolean isCredentialsNonExpired()
    {
        return !credentials_expired;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof User)
        {
            User other=(User)obj;
            return Objects.equal(id,other.id);
        }
        else
            return false;
    }
}
