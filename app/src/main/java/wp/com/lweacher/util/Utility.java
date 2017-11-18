package wp.com.lweacher.util;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import wp.com.lweacher.db.City;
import wp.com.lweacher.db.County;
import wp.com.lweacher.db.Province;
import wp.com.lweacher.gson.Weather;

/**
 * Created by wpuser on 2017/11/18.
 */

public class Utility {
    /*
    * 解析处理省的数据
    * */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvince = new JSONArray(response);
                for (int i = 0; i < allProvince.length(); i++) {
                    JSONObject provinceObject = allProvince.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
   * 解析处理城市的数据
   * */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityOBject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityOBject.getString("name"));
                    city.setCityCode(cityOBject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityOBject = allCities.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(cityOBject.getString("name"));
                    county.setWeatherId(cityOBject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public static Weather.HeWeatherBean handleWeatherResponse(String response) {
        return new Gson().fromJson(response, Weather.class).getHeWeather().get(0);
    }
}
