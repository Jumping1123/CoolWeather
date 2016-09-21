package com.example.jumping.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.app.ProgressDialog;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jumping.coolweather.R;
import com.example.jumping.coolweather.model.City;
import com.example.jumping.coolweather.service.AutoUpdateService;
import com.example.jumping.coolweather.util.HttpCallbackListener;
import com.example.jumping.coolweather.util.HttpUtil;
import com.example.jumping.coolweather.util.Utility;


/**
 * Created by Jumping on 2016/9/19.
 */
public class WeatherActivity extends Activity {
    //我的和风天气KEY
    public static final String WEATHER_KEY = "6c233183c1524bebbdfc4b6abd640428";

    private ProgressDialog mProgressDialog;//进度条
    private SharedPreferences mSharedPreferences;//数据存储对象
    private SharedPreferences.Editor mEditor;

    private Button mChangeCityButton;//小房子按钮
    private TextView mTextView_cityName;//标题栏城市名称
    private Button mRefreshButton;//刷新按钮
    private TextView mTextView_updateTime;//更新时间
    private TextView mTextView_current_date;//当前日期
    private TextView mTextView_weather_desp;//具体的天气情况
    private TextView mTextView_textView_temp1;//最低温度
    private TextView mTextView_textView_temp2;//最高温度


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        //有米广告管理器实例
//        AdManager.getInstance(this).init("8c8f79aef6457ac0", "d71c14f920b0e968", false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);

        //实例化本地存储
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();

        //变更城市（小房子按钮）
        mChangeCityButton = (Button) findViewById(R.id.button_changeCity);
        mChangeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //就是启动CityChooseActivity
                Intent intent = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
            }
        });

        //实例化各个组件
        mTextView_cityName = (TextView) findViewById(R.id.textView_city_name);
        mTextView_updateTime = (TextView) findViewById(R.id.textView_publishTime);
        mTextView_current_date = (TextView) findViewById(R.id.textView_current_date);
        mTextView_weather_desp = (TextView) findViewById(R.id.textView_weather_desp);
        mTextView_textView_temp1 = (TextView) findViewById(R.id.textView_temp1);
        mTextView_textView_temp2 = (TextView) findViewById(R.id.textView_temp2);

        //刷新按钮
        mRefreshButton = (Button) findViewById(R.id.button_refresh);
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //从服务器更新
                mTextView_updateTime.setText("同步中...");
                String citycode = mSharedPreferences.getString("city_code", "");
                if (!TextUtils.isEmpty(citycode)) {
                    updateWeatherFromServer(citycode);
                }
            }
        });

        String citycode = getIntent().getStringExtra("city_code");
        if (!TextUtils.isEmpty(citycode)) {
            mTextView_updateTime.setText("同步中...");
            updateWeatherFromServer(citycode);
        } else {
            loadWeatherData(mSharedPreferences.getString("city_code", null), mSharedPreferences.getString("city_name_ch", null), mSharedPreferences.getString("update_time", null), mSharedPreferences.getString("data_now", null), mSharedPreferences.getString("txt_d", null), mSharedPreferences.getString("txt_n", null), mSharedPreferences.getString("tmp_min", null), mSharedPreferences.getString("tmp_max", null));
        }

//        //有米广告栏，不多做解释，第一行代码里面解释很清楚，也很简单
//        AdView adView = new AdView(this, AdSize.FIT_SCREEN);
//        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.adLayout);
//        linearLayout.addView(adView);
    }



    //刷新各组件数据的封装
    private void loadWeatherData(String city_code, String city_name, String update_time, String current_data, String txt_d, String txt_n, String tmp_min, String tmp_max) {

        mTextView_cityName.setText(city_name);
        mTextView_updateTime.setText(update_time);
        mTextView_current_date.setText(current_data);

        if (txt_d.equals(txt_n)) {
            mTextView_weather_desp.setText(txt_d);
        } else {
            mTextView_weather_desp.setText(txt_d + "转" + txt_n);
        }
        mTextView_textView_temp1.setText(tmp_min + "℃");
        mTextView_textView_temp2.setText(tmp_max + "℃");

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);

    }

    //从服务器更新数据（CityChooseActivity中有相似方法）
    private void updateWeatherFromServer( String citycode) {
        String address = "https://api.heweather.com/x3/weather?cityid=" + citycode + "&key=" + WeatherActivity.WEATHER_KEY;
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Utility.handleWeatherResponse(mEditor, response)) {
                            loadWeatherData(mSharedPreferences.getString("city_code", null), mSharedPreferences.getString("city_name_ch", null), mSharedPreferences.getString("update_time", null), mSharedPreferences.getString("data_now", null), mSharedPreferences.getString("txt_d", null), mSharedPreferences.getString("txt_n", null), mSharedPreferences.getString("tmp_min", null), mSharedPreferences.getString("tmp_max", null));
                            closeProgressDialog();
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(WeatherActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {

        if (mProgressDialog == null) {

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("正在同步数据...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    private void closeProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }
}
