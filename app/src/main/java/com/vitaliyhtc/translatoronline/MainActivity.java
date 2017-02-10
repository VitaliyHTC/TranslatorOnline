package com.vitaliyhtc.translatoronline;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created by VitaliyHTC on 12.01.2017.
 */

public class MainActivity  extends AppCompatActivity {

    private static final String apiKey = "trnsl.1.1.20170112T135601Z.bee22c8cd84fa8da.3f971312dbca8748790dd308c7bfab07019c915a";

    private final Context context = this;
    public static final String TAG = "ValleyTag";

    public static String sDefSystemLanguage=null;
    private String language;

    private static HashMap<String, String> langsMap = new HashMap<>();
    private static ArrayList<String> langsList = new ArrayList<>();
    private Spinner fromLangSpinner;
    private Spinner toLangSpinner;
    private Button swapButton;
    private Button translateButton;
    private EditText fromEditText;
    private EditText toEditText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(sDefSystemLanguage==null){sDefSystemLanguage = Locale.getDefault().getLanguage();}
        setLanguage();
        setContentView(R.layout.main_activity);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.translate_launcher);

        final Bundle savedInstanceStateFinal = savedInstanceState;
        // fill spinners with languages list
        if(!restoreLangsSpinners(savedInstanceState)){
            setLangsSpinnersFromServer();
        }

        fromLangSpinner = (Spinner) findViewById(R.id.fromSpinner);
        toLangSpinner = (Spinner) findViewById(R.id.toSpinner);
        swapButton = (Button) findViewById(R.id.swapButton);
        translateButton = (Button) findViewById(R.id.translateButton);
        fromEditText = (EditText) findViewById(R.id.fromEditText);
        toEditText = (EditText) findViewById(R.id.toEditText);

        View.OnClickListener onClickListenerSwap = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //hideSoftKeyboard when leave EditText
                hideSoftKeyboard(MainActivity.this);

                int tmp = fromLangSpinner.getSelectedItemPosition();
                fromLangSpinner.setSelection(toLangSpinner.getSelectedItemPosition());
                toLangSpinner.setSelection(tmp);
            }
        };
        swapButton.setOnClickListener(onClickListenerSwap);


        /* Translate button onclick listener */
        View.OnClickListener onClickListenerTranslate = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //hideSoftKeyboard when leave EditText
                hideSoftKeyboard(MainActivity.this);

                Object fromSpinnerSelectedItem = fromLangSpinner.getSelectedItem();
                Object toSpinnerSelectedItem = toLangSpinner.getSelectedItem();

                /* Verify that spinners is filled and has selected items */
                if( fromSpinnerSelectedItem!=null && toSpinnerSelectedItem!=null ){

                String fromString = fromLangSpinner.getSelectedItem().toString();
                String toString = toLangSpinner.getSelectedItem().toString();
                final String fromLangCode = fromString.substring(0, fromString.indexOf(" "));
                String toLangCode = toString.substring(0, toString.indexOf(" "));
                String fromEditTextString = fromEditText.getText().toString();

                String textURLEncoded = "";
                // https://docs.oracle.com/javase/7/docs/api/java/net/URLEncoder.html
                try {
                    textURLEncoded = URLEncoder.encode(fromEditTextString, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                translateButton.setEnabled(false);



                    /* Volley language verification. */
                    if(!textURLEncoded.isEmpty()){
                        //https://translate.yandex.net/api/v1.5/tr.json/detect?key=trnsl.1.1.___&hint=en,de&text=___
                        String requestURLDetect = "https://translate.yandex.net/api/v1.5/tr.json/detect?key=" + apiKey +
                                "&hint=" + fromLangCode + "," + toLangCode + "&text=" + textURLEncoded;

                        JsonObjectRequest jsObjRequestDetect = new JsonObjectRequest
                                (Request.Method.GET, requestURLDetect, null, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        //{"code":200,"lang":"uk"}
                                        int responseCode = response.optInt("code");
                                        String detectLangCode = null;
                                        if (responseCode == 200) {
                                            detectLangCode = response.optString("lang");
                                        }
                                        if (detectLangCode != null) {
                                            if (!fromLangCode.equals(detectLangCode)) {
                                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.detect_lang_other) + " " + detectLangCode, Toast.LENGTH_LONG).show();
                                                String langString = langsMap.get(detectLangCode);
                                                int langPosition = langsList.indexOf(detectLangCode + " " + langString);
                                                fromLangSpinner.setSelection(langPosition);
                                            }
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        //Auto-generated method stub
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_detector_error), Toast.LENGTH_LONG).show();
                                        translateButton.setEnabled(true);
                                        error.printStackTrace();
                                    }
                                });
                        jsObjRequestDetect.setTag(TAG);
                        MyVolleySingleton.getInstance(context).addToRequestQueue(jsObjRequestDetect);
                    }
                    /* Volley language verification. end */



                /* Volley translation here! */
                String requestUrlTranslate = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + apiKey +
                        "&text=" + textURLEncoded + "&lang=" + toLangCode;
                    //"&text=" + textURLEncoded + "&lang=" + fromLangCode + "-" + toLangCode;
                    // Source language autodetect if only target lang code sended to API

                    toEditText.setText("");
                JsonObjectRequest jsObjRequestTranslate = new JsonObjectRequest
                        (Request.Method.GET, requestUrlTranslate, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                JSONArray textJSONArray = response.optJSONArray("text");
                                int resultLength = textJSONArray.length();

                                StringBuilder textSB = new StringBuilder(256);
                                for (int i = 0; i < resultLength; i++) {
                                    textSB.append(textJSONArray.optString(i));
                                    if (i < resultLength - 1) {
                                        textSB.append("\r\n");
                                    }
                                }
                                String resultString = textSB.toString();
                                resultString = resultString.replaceAll("\n", "\r\n");

                                toEditText.setText(resultString);

                                translateButton.setEnabled(true);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //Auto-generated method stub
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_translator_error), Toast.LENGTH_LONG).show();
                                translateButton.setEnabled(true);
                                error.printStackTrace();
                            }
                        });
                jsObjRequestTranslate.setTag(TAG);
                // Access the RequestQueue through your singleton class.
                MyVolleySingleton.getInstance(context).addToRequestQueue(jsObjRequestTranslate);
                /* Volley translation here! end */

            }//if( fromSpinnerSelectedItem!=null && toSpinnerSelectedItem!=null ){
                else{
                    toEditText.setText(getResources().getString(R.string.no_internet_connection));
                    // fill spinners with languages list
                    if(!restoreLangsSpinners(savedInstanceStateFinal)){
                        setLangsSpinnersFromServer();
                    }
                }
            }//public void onClick(View view)
        };
        translateButton.setOnClickListener(onClickListenerTranslate);
        /* Translate button onclick listener end */

    }

    private void setLanguage() {
        language = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("Language", "default");
        if (language.equals("default")) {
            if(sDefSystemLanguage!=null){
                language = sDefSystemLanguage;
            }else{
                language = Locale.getDefault().getLanguage();
            }
        }
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, null);
    }




    private boolean restoreLangsSpinners(Bundle savedInstanceState){
        if(savedInstanceState == null) {
            return false;
        } else {
            langsList = (ArrayList<String>) savedInstanceState.getSerializable("langsList");
            if(langsList == null || langsList.isEmpty()) {
                return false;
            } else {
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, langsList);

                Spinner fromLangSpinner = (Spinner) findViewById(R.id.fromSpinner);
                Spinner toLangSpinner = (Spinner) findViewById(R.id.toSpinner);

                fromLangSpinner.setAdapter(spinnerArrayAdapter);
                toLangSpinner.setAdapter(spinnerArrayAdapter);

                fromLangSpinner.setSelection(savedInstanceState.getInt("selectedFromSpinnerItemPosition"));
                toLangSpinner.setSelection(savedInstanceState.getInt("selectedToSpinnerItemPosition"));

                return true;
            }
        }
    }

    /*
    Ок. Тут заповнюємо список для Spinners, вибираємо мови.
    Зберігаємо вибрані при завершенні та відновлюємо при повторному запуску.
    Зроблено перевірку, якщо при відновленні не співпадає позиція і рядок мови то значить змінено
    список мов до використання у Yandex Translate API. Відповідно скидуємо вибір до default значень.
    При зміні мови додатку вибір скидається до default. (String збережений і нового поля не співпадає).
    Варіант оптимізації: парсити і порівнювати коди на початку. *10 mins later* Готово!
    Чому в spinner порядок неправильний? Ітератор йде непослідовно?
    https://developer.android.com/reference/org/json/JSONObject.html#keys()
    >>> The order of the keys is undefined.
    Треба сортований список для spinner-а і номер позиції для дефолтної мови.
    Тут ніби все готово.
     */
    private void setLangsSpinnersFromServer(){
        String requestUrlGetLangs = "https://translate.yandex.net/api/v1.5/tr.json/getLangs?ui="+language+"&key="+apiKey;
        ///api/v1.5/tr.json/getLangs?ui=en&key=API-KEY

        JsonObjectRequest jsObjRequestGetLangs = new JsonObjectRequest
                (Request.Method.GET, requestUrlGetLangs, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Spinner fromSpinner = (Spinner) findViewById(R.id.fromSpinner);
                        Spinner toSpinner = (Spinner) findViewById(R.id.toSpinner);

                        JSONObject langsJSONObject = response.optJSONObject("langs");

                        int enLangPosition = 0;
                        int ukLangPosition = 0;
                        String enLangString = "";
                        String ukLangString = "";
                        Iterator<String> keysIterator = langsJSONObject.keys();
                        String key;
                        String lang;

                        while(keysIterator.hasNext()){
                            key = keysIterator.next();
                            lang="_";
                            try {
                                lang = langsJSONObject.getString(key);
                            } catch (JSONException e) {e.printStackTrace();}
                            langsList.add(key + " " + lang);
                            langsMap.put(key, lang);
                            if("en".equals(key)){ enLangString = key + " " + lang; }
                            if("uk".equals(key)){ ukLangString = key + " " + lang; }
                        }
                        Collections.sort(langsList);
                        enLangPosition = langsList.indexOf(enLangString);
                        ukLangPosition = langsList.indexOf(ukLangString);



                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, langsList);

                        fromSpinner.setAdapter(spinnerArrayAdapter);
                        toSpinner.setAdapter(spinnerArrayAdapter);

                        SharedPreferences sharedPreferences = context.getSharedPreferences("TranslatorOnline_Setting", 0);
                        int lastSelectedFromPosition = sharedPreferences.getInt("selectedFromSpinnerItemPosition", enLangPosition);
                        int lastSelectedToPosition = sharedPreferences.getInt("selectedToSpinnerItemPosition", ukLangPosition);

                        if(langsList.size()<lastSelectedFromPosition || langsList.size()<lastSelectedToPosition){
                            lastSelectedFromPosition = enLangPosition;
                            lastSelectedToPosition = ukLangPosition;
                        }else{
                            String lastFromString = langsList.get(lastSelectedFromPosition);
                            String lastToString = langsList.get(lastSelectedToPosition);
                            String lastFromCodeString = lastFromString.substring(0, lastFromString.indexOf(" "));
                            String lastToCodeString = lastToString.substring(0, lastToString.indexOf(" "));
                            if(!lastFromCodeString.equals(sharedPreferences.getString("selectedFromSpinnerCodeString", ""))){
                                lastSelectedFromPosition = enLangPosition;
                            }
                            if(!lastToCodeString.equals(sharedPreferences.getString("selectedToSpinnerCodeString", ""))){
                                lastSelectedToPosition = ukLangPosition;
                            }
                        }
                        fromSpinner.setSelection(lastSelectedFromPosition);
                        toSpinner.setSelection(lastSelectedToPosition);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Auto-generated method stub
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_getlangs_error), Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
                });
        jsObjRequestGetLangs.setTag(TAG);
        // Access the RequestQueue through your singleton class.
        MyVolleySingleton.getInstance(context).addToRequestQueue(jsObjRequestGetLangs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_info) {
            Intent intent = new Intent(MainActivity.this, MainInfoActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void writeSharedPreferences() {
        Object fromSpinnerSelectedItem = ((Spinner) findViewById(R.id.fromSpinner)).getSelectedItem();
        Object toSpinnerSelectedItem = ((Spinner) findViewById(R.id.toSpinner)).getSelectedItem();
        if( fromSpinnerSelectedItem!=null && toSpinnerSelectedItem!=null ){
            SharedPreferences.Editor editor = this.getSharedPreferences("TranslatorOnline_Setting", 0).edit();
            editor.putInt("selectedFromSpinnerItemPosition", ((Spinner) findViewById(R.id.fromSpinner)).getSelectedItemPosition());
            editor.putInt("selectedToSpinnerItemPosition", ((Spinner) findViewById(R.id.toSpinner)).getSelectedItemPosition());

            String fromString = fromSpinnerSelectedItem.toString();
            String toString = toSpinnerSelectedItem.toString();
            editor.putString("selectedFromSpinnerCodeString", fromString.substring(0, fromString.indexOf(" ")));
            editor.putString("selectedToSpinnerCodeString", toString.substring(0, toString.indexOf(" ")));
            editor.commit();
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.writeSharedPreferences();
        RequestQueue requestQueue = MyVolleySingleton.getInstance(context).getRequestQueue();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("langsList", langsList);
        outState.putInt("selectedFromSpinnerItemPosition", ((Spinner) findViewById(R.id.fromSpinner)).getSelectedItemPosition());
        outState.putInt("selectedToSpinnerItemPosition", ((Spinner) findViewById(R.id.toSpinner)).getSelectedItemPosition());
        //outState.putSerializable("langsMap", langsMap);
    }

}
