package io.romain.passport.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import butterknife.Bind;
import io.romain.passport.R;
import io.romain.passport.model.City;
import io.romain.passport.utils.glide.CityDetailTarget;

public class CityDetailActivity extends BaseActivity implements OnMapReadyCallback {

	public static final String EXTRA_CITY = "extra_city";

	@Bind(R.id.city_detail_toolbar)
	Toolbar mToolbar;
	@Bind(R.id.city_detail_collapsing_toolbar)
	CollapsingToolbarLayout mCollapsingToolbar;
	@Bind(R.id.city_detail_picture)
	ImageView mImageView;
	@Bind(R.id.city_detail_weather_frame)
	LinearLayout mWeatherLayout;

	private SupportMapFragment mGoogleMapsFragment;

	private City mCity;

	private final Random mRandom = new Random();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCity = getIntent().getParcelableExtra(EXTRA_CITY);
		if (mCity == null) {
			throw new RuntimeException("Please provide a city");
		}

		setContentView(R.layout.activity_city_detail);
		setFragment(SupportMapFragment.newInstance(
				new GoogleMapOptions()
						.liteMode(true)
						.camera(
								new CameraPosition.Builder()
										.target(new LatLng(mCity.latitude, mCity.longitude))
										.zoom(11)
										.build()
						)
				)
		);

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mCollapsingToolbar.setTitle(mCity.name + ", " +  mCity.country);
		Glide.with(this)
				.load(mCity.picture)
				.centerCrop()
				.fallback(R.drawable.city_list_placeholder)
				.error(R.drawable.city_list_placeholder)
//				.crossFade()
				.into(new CityDetailTarget(mImageView, mCollapsingToolbar, mToolbar));


		mGoogleMapsFragment.getMapAsync(this);

		buildFakeWeatherData();
	}

	@Override
	public void onMapReady(GoogleMap map) {
		map.addMarker(
				new MarkerOptions()
						.position(new LatLng(mCity.latitude, mCity.longitude))
		);
	}

	private void setFragment(SupportMapFragment fragment) {
		mGoogleMapsFragment = fragment;
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content, fragment, "fragment")
				.commitAllowingStateLoss();

		getSupportFragmentManager().executePendingTransactions();
	}

	private void buildFakeWeatherData() {
		LayoutInflater inflater = LayoutInflater.from(this);
		for (int i = 0; i < 7; i++) {
			View v = inflater.inflate(R.layout.item_city_detail_weather, mWeatherLayout, false);
			((TextView) v.findViewById(R.id.item_city_detail_weather_min)).setText(getFormattedTemperature(this, getRandom(5, 10)));
			((TextView) v.findViewById(R.id.item_city_detail_weather_max)).setText(getFormattedTemperature(this, getRandom(10, 20)));
			((TextView) v.findViewById(R.id.item_city_detail_weather_day)).setText(getAbbreviatedDay(getDayMidnight(i)));
			((ImageView) v.findViewById(R.id.item_city_detail_weather_icon)).setImageResource(getIcon(getRandom(0, 7)));

			mWeatherLayout.addView(v);
		}
	}

	private static CharSequence getFormattedTemperature(Context context, float temp) {
		String unit = "°" + context.getString(R.string.temperature_unit_celsius);

		String value = Integer.toString(Math.round(temp));

		SpannableStringBuilder sb = new SpannableStringBuilder(value + unit);
		sb.setSpan(new RelativeSizeSpan((float) 0.5), sb.length() - unit.length(), sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		sb.setSpan(new SuperscriptSpan(), sb.length() - unit.length(), sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

		return sb;
	}

	private int getRandom(int min, int max) {
		return mRandom.nextInt(max - min + 1) + min;
	}

	private String getAbbreviatedDay(long date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date);

		String day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
		return day.substring(0, 1).toUpperCase() + day.substring(1);
	}

	private static long getDayMidnight(int days) {
		// today
		Calendar date = Calendar.getInstance();
		// reset hour, minutes, seconds and millis
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 1);
		date.set(Calendar.MILLISECOND, 0);

		// next day
		date.add(Calendar.DATE, days);

		return date.getTimeInMillis();
	}

	@DrawableRes
	private int getIcon(int val) {
		switch (val) {
			case 0:
				return R.drawable.art_clear;
			case 1:
				return R.drawable.art_clouds;
			case 2:
				return R.drawable.art_fog;
			case 3:
				return R.drawable.art_light_clouds;
			case 4:
				return R.drawable.art_light_rain;
			case 5:
				return R.drawable.art_rain;
			case 6:
				return R.drawable.art_snow;
			case 7:
				return R.drawable.art_storm;
			default:
				throw new IllegalArgumentException("Illegal : " + val);

		}
	}
}
