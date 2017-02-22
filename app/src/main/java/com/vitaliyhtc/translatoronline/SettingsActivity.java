package com.vitaliyhtc.translatoronline;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Locale;

public class SettingsActivity  extends SettingsActivityAdapter {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        if(getSupportActionBar1() != null){
            getSupportActionBar1().setDisplayHomeAsUpEnabled(true);
        }

        //setLanguageChangeListener
        Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(preference.getKey().equals("Language")){
                    String language = (String)newValue;

                    if (language.equals("default")) {
                        if(MainActivity.sDefSystemLanguage!=null){
                            language = MainActivity.sDefSystemLanguage;
                        }else{
                            language = Locale.getDefault().getLanguage();
                        }
                    }
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.app_restart)+" _"+language, Toast.LENGTH_SHORT).show();
                    Locale locale = new Locale(language);
                    Locale.setDefault(locale);
                    Configuration configuration = new Configuration();
                    configuration.locale = locale;
                    getBaseContext().getResources().updateConfiguration(configuration, null);

                    Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
                return true;
            }
        };
        ListPreference listPreference = (ListPreference)findPreference("Language");
        listPreference.setOnPreferenceChangeListener(changeListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
