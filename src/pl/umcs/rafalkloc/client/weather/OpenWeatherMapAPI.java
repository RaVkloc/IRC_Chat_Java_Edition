package pl.umcs.rafalkloc.client.weather;

import pl.umcs.rafalkloc.common.FillBeforeRun;
import top.jfunc.json.JsonObject;
import top.jfunc.json.impl.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class OpenWeatherMapAPI {

    public static Map<String, String> getWeatherFromAPI(String town)
    {
        try {
            return convertJsonToWeatherInfo(getWeather(town), town);
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject getWeather(String town) throws Exception
    {
        InputStream is = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + town + "&appid=" +
                                         FillBeforeRun.OPEN_WEATHER_APP_KEY + "&units=metric").openStream();
        JSONObject json;
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            int read;
            while ((read = rd.read()) != -1) {
                sb.append((char) read);
            }
            String jsonText = sb.toString();
            json = new JSONObject(jsonText);
        } finally {
            is.close();
        }
        return json;
    }

    private static Map<String, String> convertJsonToWeatherInfo(JSONObject json, String town)
    {
        JsonObject basicObj = json.getJsonObject("main");

        Map<String, String> result = new HashMap<>();
        result.put("Town", town);
        result.put("Temperature", basicObj.getDouble("temp").toString());
        result.put("FeelTemperature", basicObj.getDouble("feels_like").toString());
        result.put("Pressure", basicObj.getInteger("pressure").toString());

        return result;
    }
}
