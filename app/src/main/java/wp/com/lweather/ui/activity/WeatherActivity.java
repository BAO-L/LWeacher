package wp.com.lweather.ui.activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import wp.com.lweather.R;
import wp.com.lweather.common.Constants;
import wp.com.lweather.gson.Weather;
import wp.com.lweather.util.HttpUtil;
import wp.com.lweather.util.Utility;

public class WeatherActivity extends AppCompatActivity {

    @BindView(R.id.title_city)
    TextView titleCity;
    @BindView(R.id.title_updatetime)
    TextView titleUpdatetime;
    @BindView(R.id.degree_text)
    TextView degreeText;
    @BindView(R.id.weatherinfo_text)
    TextView weatherinfoText;
    @BindView(R.id.forecast_layout)
    LinearLayout forecastLayout;
    @BindView(R.id.aqi_text)
    TextView aqiText;
    @BindView(R.id.pm25_text)
    TextView pm25Text;
    @BindView(R.id.comfort_text)
    TextView comfortText;
    @BindView(R.id.carwash_text)
    TextView carwashText;
    @BindView(R.id.sport_text)
    TextView sportText;
    @BindView(R.id.weather_layout)
    ScrollView weatherLayout;
    @BindView(R.id.bing_pic)
    ImageView bingPic;
    @BindView(R.id.swipe_refresh)
    public SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.nav_button)
    Button navButton;
    @BindView(R.id.drawer_layout)
    public DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     /*
      * 状态栏和背景图融合 （5.0以上）
      * */
        if (Build.VERSION.SDK_INT >= 21) {
            System.out.println("背景图融合。。。。");
            ;
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);//获取天气缓存
        String bingURl = prefs.getString("bing_pic", null);//获取必应图片
        if (bingURl != null) {
            Glide.with(this).load(bingURl).into(bingPic);
        } else {
            loadBingPic();
        }

        final String[] weatherId = new String[1];
        if (weatherString != null) {
            Weather.HeWeatherBean weather = Utility.handleWeatherResponse(weatherString);
            weatherId[0] = weather.getBasic().getId();
            showWeatherInfo(weather);
            System.out.println("111111111wewather id is "+ weatherId[0]);
        } else {
            weatherId[0] = getIntent().getStringExtra("weather_id");
            System.out.println("222222222wewather id is "+ weatherId[0]);
            weatherLayout.setVisibility(View.INVISIBLE);//请求数据时，先隐藏界面，体验更佳
            requestWeather(weatherId[0]);
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                weatherId[0] = prefs.getString("weather_id",null);
                System.out.println("---------onrefreshing--------------"+ weatherId[0]);
                requestWeather(weatherId[0]);
            }
        });


    }

    /*请求天气数据
    * */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=" + Constants.KEY;
        HttpUtil.sendOKHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather.HeWeatherBean weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.getStatus())) {
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }


    /*
    * 显示天气数据
    * */
    private void showWeatherInfo(Weather.HeWeatherBean weather) {
        String cityName = weather.getBasic().getCity();
        String updateTime = weather.getBasic().getUpdate().getLoc().split(" ")[1];
        String degree = weather.getNow().getTmp() + "℃";
        String weatherInfo = weather.getNow().getCond().getTxt();
        titleCity.setText(cityName);
        titleUpdatetime.setText(updateTime);
        degreeText.setText(degree);
        weatherinfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for (Weather.HeWeatherBean.DailyForecastBean forecast : weather.getDaily_forecast()) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dataText = (TextView) view.findViewById(R.id.data_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dataText.setText(forecast.getDate());
            infoText.setText(forecast.getCond().getTxt_d());
            maxText.setText(forecast.getTmp().getMax());
            minText.setText(forecast.getTmp().getMax());
            forecastLayout.addView(view);
        }

        if (weather.getAqi() != null) {
            aqiText.setText(weather.getAqi().getCity().getAqi());
            pm25Text.setText(weather.getAqi().getCity().getPm25());
        }
        String comfort = "舒适度：" + weather.getSuggestion().getComf().getTxt();
        String carwash = "洗车指数：" + weather.getSuggestion().getCw().getTxt();
        String sport = "运动建议：：" + weather.getSuggestion().getSport().getTxt();
        comfortText.setText(comfort);
        carwashText.setText(carwash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        loadBingPic();

    }


    private void loadBingPic() {

        HttpUtil.sendOKHttpRequest(Constants.BING_PIC, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String url = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", url);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(url).into(bingPic);
                    }
                });
            }
        });
    }

    @OnClick(R.id.nav_button)
    public void onViewClicked() {
        drawerLayout.openDrawer(GravityCompat.START);
    }
}
