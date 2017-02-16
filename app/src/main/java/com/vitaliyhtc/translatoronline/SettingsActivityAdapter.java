package com.vitaliyhtc.translatoronline;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsActivityAdapter extends PreferenceActivity {
    private AppCompatDelegate appCompatDelegate;

    private AppCompatDelegate getAppCompactDelegate() {
        if (this.appCompatDelegate == null) {
            this.appCompatDelegate = AppCompatDelegate.create(this, null);
        }
        return this.appCompatDelegate;
    }

    public ActionBar getSupportActionBar1() {
        return getAppCompactDelegate().getSupportActionBar();
    }

    public void addContentView(View paramView, ViewGroup.LayoutParams paramLayoutParams) {
        getAppCompactDelegate().addContentView(paramView, paramLayoutParams);
    }

    public MenuInflater getMenuInflater() {
        return getAppCompactDelegate().getMenuInflater();
    }

    public void invalidateOptionsMenu() {
        getAppCompactDelegate().invalidateOptionsMenu();
    }

    public void onConfigurationChanged(Configuration paramConfiguration) {
        super.onConfigurationChanged(paramConfiguration);
        getAppCompactDelegate().onConfigurationChanged(paramConfiguration);
    }

    protected void onCreate(Bundle paramBundle) {
        getAppCompactDelegate().installViewFactory();
        getAppCompactDelegate().onCreate(paramBundle);
        super.onCreate(paramBundle);
    }

    protected void onDestroy() {
        super.onDestroy();
        getAppCompactDelegate().onDestroy();
    }

    protected void onPostCreate(Bundle paramBundle) {
        super.onPostCreate(paramBundle);
        getAppCompactDelegate().onPostCreate(paramBundle);
    }

    protected void onPostResume() {
        super.onPostResume();
        getAppCompactDelegate().onPostResume();
    }

    protected void onStop() {
        super.onStop();
        getAppCompactDelegate().onStop();
    }

    protected void onTitleChanged(CharSequence paramCharSequence, int paramInt) {
        super.onTitleChanged(paramCharSequence, paramInt);
        getAppCompactDelegate().setTitle(paramCharSequence);
    }

    public void setContentView(int paramInt) {
        getAppCompactDelegate().setContentView(paramInt);
    }

    public void setContentView(View paramView) {
        getAppCompactDelegate().setContentView(paramView);
    }

    public void setContentView(View paramView, ViewGroup.LayoutParams paramLayoutParams) {
        getAppCompactDelegate().setContentView(paramView, paramLayoutParams);
    }

}
