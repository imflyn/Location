package com.flyn.location;

import android.app.Application;

public class AppContext extends Application
{

    private static AppContext context;

    @Override
    public void onCreate()
    {
        super.onCreate();
        context = this;
    }

    public static AppContext getContext()
    {
        return context;
    }

}
