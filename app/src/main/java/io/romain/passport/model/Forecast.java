/*
 *    Copyright 2016 Romain
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.romain.passport.model;


import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import io.romain.passport.model.wrappers.WeatherWrapper;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

@AutoValue
public abstract class Forecast implements Parcelable {

    // api endpoint
    private static final String MODULE = "/weather";

    // endpoints
    private static final String GET_FORECAST = MODULE + "/forecast/{latitude}/{longitude}";

    @SerializedName("min")
    public abstract float min();

    @SerializedName("max")
    public abstract float max();

    @SerializedName("date")
    public abstract long date();

    @SerializedName("icon")
    public abstract int icon();

	public static TypeAdapter<Forecast> fromJson(Gson gson) {
		return new AutoValue_Forecast.GsonTypeAdapter(gson);
	}

    public interface ForecastService {
        @GET(GET_FORECAST)
        Call<WeatherWrapper> getForecast(@Path("latitude") double latitude, @Path("longitude") double longitude);
    }

}
