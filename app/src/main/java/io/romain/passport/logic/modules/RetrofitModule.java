/*
 *    Copyright 2015 Romain
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
package io.romain.passport.logic.modules;

import com.google.gson.Gson;

import dagger.Module;
import dagger.Provides;
import io.romain.passport.BuildConfig;
import io.romain.passport.utils.Dog;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class RetrofitModule {

	private static final String PROTOCOL = "http";
	private static final String HOST = "passport-supercairos.rhcloud.com";
	private static final int PORT = 80;

	public static final HttpUrl BASE_URL = new HttpUrl.Builder()
			.scheme(PROTOCOL)
			.host(HOST)
			.port(PORT)
			.build();

	@Provides
	Retrofit getRetrofit(OkHttpClient client, Gson gson) {
		if (BuildConfig.DEBUG) Dog.d("Called()");

		return new Retrofit.Builder()
				.baseUrl(BASE_URL)
				.addConverterFactory(GsonConverterFactory.create(gson))
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.client(client)
				.build();
	}

}
