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

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

@AutoValue
public abstract class CityAutocomplete implements Parcelable{

    // api endpoint
    private static final String MODULE = "/cities";

    // endpoints
    private static final String GET_FORECAST = MODULE + "/search";

    @SerializedName("name")
    public abstract String name();

    @SerializedName("placeid")
    public abstract String id();

    public static TypeAdapter<CityAutocomplete> fromJson(Gson gson) {
        return new AutoValue_CityAutocomplete.GsonTypeAdapter(gson);
    }

    public interface CityAutocompleteService {
        @GET(GET_FORECAST)
        Call<List<CityAutocomplete>> complete(@Query("query") String name);
    }
}
