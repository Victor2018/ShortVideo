package com.victor.video.library.App;import android.app.Application;import android.util.DisplayMetrics;/** * Created by victor on 2017/4/26. */public class App extends Application{    private static App instance;    public static int screenWidth;    public static int screenHeight;    public static int beautyLevel = 5;    public App() {        instance = this;    }    public static App getContext() {        return instance;    }    @Override    public void onCreate() {        super.onCreate();        DisplayMetrics mDisplayMetrics = getApplicationContext().getResources()                .getDisplayMetrics();        screenWidth = mDisplayMetrics.widthPixels;        screenHeight = mDisplayMetrics.heightPixels;    }}