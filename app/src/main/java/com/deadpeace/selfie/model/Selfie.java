package com.deadpeace.selfie.model;

import com.google.common.base.Objects;
import com.google.gson.annotations.JsonAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Виталий on 07.10.2015.
 */

public class Selfie implements Serializable
{
    private long id;
    private long date;
    private User creator;
    private List<User> liked=new ArrayList<>();
    private String description;
    private String title;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id=id;
    }

    public long getDate()
    {
        return date;
    }

    public void setDate(long date)
    {
        this.date=date;
    }

    public User getCreator()
    {
        return creator;
    }

    public void setCreator(User creator)
    {
        this.creator=creator;
    }

    public List<User> getLiked()
    {
        return Collections.unmodifiableList(liked);
    }

    public void setLiked(List<User> liked)
    {
        this.liked=liked;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description=description;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title=title;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof Selfie)
        {
            Selfie other=(Selfie)obj;
            return Objects.equal(id,other.id);
        }
        else
            return false;
    }
}
