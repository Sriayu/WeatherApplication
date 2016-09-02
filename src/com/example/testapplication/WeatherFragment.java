package com.example.testapplication;

import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;



public class WeatherFragment extends android.support.v4.app.Fragment {

	Typeface weatherFont; 
	
	TextView cityField;
	TextView updateField;
	TextView detailField;
	TextView currentTemperatureField;
	TextView weatherIcon;
	
	Handler handler;
	
	public WeatherFragment() {
		handler = new Handler();
	}
	
	public void onCreateView (@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "weather.ttf");
		
		updateWeatherData(new CityPreference(getActivity()).getCity());
		
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		View rootView = 
				inflater.inflate(R.layout.fragment_weather, container, false);
		
		cityField = (TextView) rootView.findViewById(R.id.city_field);
		updateField = (TextView) rootView.findViewById(R.id.update_field);
		detailField = (TextView) rootView.findViewById(R.id.details_field);
		currentTemperatureField = (TextView) rootView.findViewById(R.id.current_temperatur_field);
		weatherIcon = (TextView) rootView.findViewById(R.id.weather_icon);
		
		weatherIcon.setTypeface(weatherFont);
		
		return rootView;
	}
	
	public void updateWeatherData(final String city) {
		new Thread(){
			@Override
			public void run(){
				final JSONObject json = RemoteFetch.getJSON(getActivity(), city);
				
				if (json == null){
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(getActivity(), 
									getString(R.string.place_not_found), 
									Toast.LENGTH_LONG).show();
							
						}
					});
				}else{
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							renderWeather(json);
							
						}
					});
				}
			}
		}.start();
	}
	
	private void renderWeather(JSONObject json){
		try {
			cityField.setText(json.getString("name").toUpperCase(Locale.US) + 
					"," + json.getJSONObject("sys").getString("country"));
			
			JSONObject details = json.getJSONArray("weather").getJSONObject(0);
			JSONObject main = json.getJSONObject("main");
			
			detailField.setText(details.getString("description").toUpperCase(Locale.US) + "\n"
					+ "Humidity : " + main.getString("humidity") + "%" + "\n" + "Presure : "
					+ main.getString("presure") + "hPa");
			
			currentTemperatureField.setText(String.format("%.2f", main.getDouble("temp"))+ " *C");
			
			android.text.format.DateFormat df = new android.text.format.DateFormat();
			//df.format("yyyy-MM-dd hh:mm:ss", new java.util.Date());
			
			String updateOn = (String) df.format("yyyy-MM-dd hh:mm:ss", new java.util.Date());
			updateField.setText("Last Update : " + updateOn);
			
			setWeatherIcon(details.getInt("id"), json.getJSONObject("sys").getLong("sunrise") * 1000, 
					json.getJSONObject("sys").getLong("sunset") * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void setWeatherIcon(int actualId, long sunrise, long sunset){
		int id = actualId / 100;
		String Icon = "";
		
		if (actualId == 800){
			long currentTime = new Date().getTime();
			
			if (currentTime >= sunrise && currentTime < sunset){
				Icon = getActivity().getString(R.string.weather_sunny);
			}else {
				Icon = getActivity().getString(R.string.weather_clear_night);
			}
		}else {
			switch (id) {
				case 2: Icon = getActivity().getString(R.string.weather_thunder);
				break;
				
				case 3 : Icon = getActivity().getString(R.string.weather_drizzle);
				break;
				
				case 5 : Icon = getActivity().getString(R.string.weather_rainy);
				break;
				
				case 6 : Icon = getActivity().getString(R.string.weather_snowy);
				break;
				
				case 7 : Icon = getActivity().getString(R.string.weather_foggy);
				break;
				
				case 8 : Icon = getActivity().getString(R.string.weather_cloudy);
				break;

			}
		}
		
		weatherIcon.setText(Icon);
	}
	
	public void changeCity(String city){
		updateWeatherData(city);
	}
}
