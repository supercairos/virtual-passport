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
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import io.romain.passport.R;
import io.romain.passport.model.City;
import io.romain.passport.model.Forecast;
import io.romain.passport.model.wrappers.WeatherWrapper;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.glide.CityDetailTarget;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CityDetailActivity extends BaseActivity implements OnMapReadyCallback {

    public static final String EXTRA_CITY = "extra_city";

    @Bind(R.id.city_detail_toolbar)
    Toolbar mToolbar;
    @Bind(R.id.city_detail_collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.city_detail_picture)
    ImageView mImageView;

    // Weather
    @Bind(R.id.city_detail_weather_frame)
    LinearLayout mWeatherLayout;
    @Bind(R.id.city_detail_weather_scroll_progress)
    ProgressBar mWeatherSpinner;
    @Bind(R.id.city_detail_weather_empty_view)
    TextView mWeatherEmptyView;
    @Bind(R.id.city_detail_weather_scroll_view)
    HorizontalScrollView mWeatherScrollView;

    private SupportMapFragment mGoogleMapsFragment;
    private City mCity;


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

        mCollapsingToolbar.setTitle(mCity.name + ", " + mCity.country);
        Glide.with(this)
                .load(mCity.picture)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fallback(R.drawable.no_picture_found)
                .error(R.drawable.no_picture_found)
//				.crossFade()
                .into(new CityDetailTarget(mImageView, mCollapsingToolbar, mToolbar));


        mGoogleMapsFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWeatherData();
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

    private void getWeatherData() {
        mWeatherEmptyView.setVisibility(View.GONE);
        mWeatherSpinner.setVisibility(View.VISIBLE);
        mWeatherScrollView.setVisibility(View.INVISIBLE);
        Forecast.ForecastService service = mRetrofit.create(Forecast.ForecastService.class);
        Call<WeatherWrapper> call = service.getForecast(mCity.latitude, mCity.longitude);
        call.enqueue(new Callback<WeatherWrapper>() {

            @Override
            public void onResponse(Call<WeatherWrapper> call, Response<WeatherWrapper> response) {
                if (response.isSuccessful()) {
                    List<Forecast> forecasts = response.body().forecasts;
                    if (!forecasts.isEmpty()) {
                        mWeatherLayout.removeAllViews();
                        for (Forecast forecast : forecasts) {
                            View v = getLayoutInflater().inflate(R.layout.item_city_detail_weather, mWeatherLayout, false);
                            ((TextView) v.findViewById(R.id.item_city_detail_weather_min)).setText(getFormattedTemperature(CityDetailActivity.this, forecast.min));
                            ((TextView) v.findViewById(R.id.item_city_detail_weather_max)).setText(getFormattedTemperature(CityDetailActivity.this, forecast.max));
                            ((TextView) v.findViewById(R.id.item_city_detail_weather_day)).setText(getAbbreviatedDay(forecast.date));
                            ((ImageView) v.findViewById(R.id.item_city_detail_weather_icon)).setImageResource(getIcon(forecast.icon));

                            mWeatherLayout.addView(v);
                        }

                        mWeatherEmptyView.setVisibility(View.GONE);
                        mWeatherSpinner.setVisibility(View.GONE);
                        mWeatherScrollView.setVisibility(View.VISIBLE);
                    } else {
                        onFailure(call, null);
                    }
                } else {
                    onFailure(call, null);
                }
            }

            @Override
            public void onFailure(Call<WeatherWrapper> call, Throwable t) {
                Dog.d("Failure : " + t);
                mWeatherEmptyView.setVisibility(View.VISIBLE);
                mWeatherSpinner.setVisibility(View.GONE);
                mWeatherScrollView.setVisibility(View.INVISIBLE);
            }
        });
    }

    private static CharSequence getFormattedTemperature(Context context, float temp) {
        String unit = "°" + context.getString(R.string.temperature_unit_celsius);

        String value = Integer.toString(Math.round(temp));

        SpannableStringBuilder sb = new SpannableStringBuilder(value + unit);
        sb.setSpan(new RelativeSizeSpan((float) 0.5), sb.length() - unit.length(), sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(new SuperscriptSpan(), sb.length() - unit.length(), sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return sb;
    }

    private String getAbbreviatedDay(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);

        String day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        return day.substring(0, 1).toUpperCase() + day.substring(1);
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
