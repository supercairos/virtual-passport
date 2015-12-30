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
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

@Module
public class RetrofitModule {

	public static final String PROTOCOL = "http";
	public static final String HOST = "passport-supercairos.rhcloud.com";
	public static final int PORT = 80;

	@Provides
	@Singleton
	Retrofit getRetrofit(OkHttpClient client, Gson gson) {
		return new Retrofit.Builder()
				.baseUrl(
						new HttpUrl.Builder()
								.scheme(PROTOCOL)
								.host(HOST)
								.port(PORT)
								.build()
				)
				.addConverterFactory(GsonConverterFactory.create(gson))
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.client(client)
				.build();
	}

}