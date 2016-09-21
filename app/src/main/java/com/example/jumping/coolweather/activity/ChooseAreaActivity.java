package com.example.jumping.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import com.example.jumping.coolweather.R;
import com.example.jumping.coolweather.db.CoolWeatherDB;
import com.example.jumping.coolweather.model.City;
import com.example.jumping.coolweather.util.HttpCallbackListener;
import com.example.jumping.coolweather.util.HttpUtil;
import com.example.jumping.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jumping on 2016/9/19.
 */
public class ChooseAreaActivity extends Activity {
    private CoolWeatherDB coolWeatherDB;//数据库操作对象
    private ProgressDialog mProgressDialog;//进度条对话框
    private EditText editText;//搜索编辑框
    private ArrayAdapter<String> mAdapter;//ListView适配器
    private ListView mListView;//城市ListView
    private List<String> cityNames = new ArrayList<>();//用于存放与输入的内容相匹配的城市名称字符串
    private List<City> mCities;//用于存放与输入的内容相匹配的城市名称对象

    private static final int NONE_DATA = 0;//标识是否有初始化城市数据

    private SharedPreferences mSharedPreferences;//本地存储
    private boolean isfromweatheractivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isfromweatheractivity = getIntent().getBooleanExtra("from_weather_activity", false);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);//获取本地存储对象
        if (mSharedPreferences.getBoolean("city_selected", false) && !isfromweatheractivity) {
            Intent intent3 = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
            startActivity(intent3);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);

        coolWeatherDB = CoolWeatherDB.getInstance(this);//获取数据库处理对象

        //先检查本地是否已同步过城市数据，如果没有，则从服务器同步
        if (coolWeatherDB.checkDataState() == NONE_DATA) {
            queryCitiesFromServer();
        }

        mCities = queryCitiesFromLocal("");//获取本地存储的所有的城市

        //搜索框，设置文本变化监听器
        editText = (EditText) findViewById(R.id.edit_city);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                mCities = queryCitiesFromLocal(s.toString());//每次文本变化就去本地数据库查询匹配的城市
                mAdapter.notifyDataSetChanged();//通知更新
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cityNames);//适配器初始化
        mListView = (ListView) findViewById(R.id.list_view_cities);
        mListView.setAdapter(mAdapter);

        //ListView的Item点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String citycode = mCities.get(position).getCity_code();//根据点击的位置获取对应的City_code
                Intent intent2 = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                intent2.putExtra("city_code", citycode);
                startActivity(intent2);
                finish();
            }
        });
    }

    //从服务器取出所有的城市信息
    private void queryCitiesFromServer() {
        String address = " https://api.heweather.com/x3/citylist?search=allchina&key=" + WeatherActivity.WEATHER_KEY;
        showProgressDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if (Utility.handleCityResponse(coolWeatherDB, response)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            coolWeatherDB.updateDataState();
                        }
                    });
                }
            }

            @Override
            public void onError(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //从本地数据库取出相似的城市名称
    private List<City> queryCitiesFromLocal(String name) {
        List<City> cities = coolWeatherDB.loadCitiesByName(name);
        cityNames.clear();
        for (City city : cities) {
            cityNames.add(city.getCity_name_ch());
        }
        return cities;
    }


    //显示进度条
    private void showProgressDialog() {

        if (mProgressDialog == null) {

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("正在同步数据...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    //关闭进度条
    private void closeProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (isfromweatheractivity) {
            Intent intent4 = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
            startActivity(intent4);
        }
        finish();
    }
}
