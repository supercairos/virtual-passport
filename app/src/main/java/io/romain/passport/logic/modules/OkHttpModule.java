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

import android.accounts.AccountManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.romain.passport.BuildConfig;
import io.romain.passport.logic.helpers.AccountHelper;
import io.romain.passport.model.exceptions.NetworkException;
import io.romain.passport.utils.Dog;

@Module
public class OkHttpModule {

	private static final long TIMEOUT = 120;

	@Provides
	@Singleton
	protected static OkHttpClient getOkHttpClient(Context context, Gson gson, AccountManager manager) {
		OkHttpClient client = new OkHttpClient();
		client.setConnectTimeout(TIMEOUT, TimeUnit.SECONDS);
		client.interceptors().add(new HeaderInterceptor(manager));
		client.interceptors().add(
				new HttpLoggingInterceptor(message -> Dog.tag("OkHttp").d(message))
						.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.BASIC)
		);
		client.networkInterceptors().add(new NetworkExceptionInterceptor(gson));

		// Remove cleartext in the future
		client.setConnectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT));
		if (context != null) {
			Cache cache = new Cache(context.getCacheDir(), 10 * 1024 * 1024); // 10 MiB
			client.setCache(cache);
		}

		return client;
	}

	private static final class HeaderInterceptor implements Interceptor {

		private static final long MAX_STALE = 60 * 60 * 3;

		private final AccountManager mManager;

		public HeaderInterceptor(AccountManager manager) {
			mManager = manager;
		}

		@Override
		public Response intercept(Interceptor.Chain chain) throws IOException {
			Request request = chain.request();
			Request.Builder builder = request.newBuilder()
					.addHeader("User-Agent", "VirtualPassport-Client {Android-" + Build.VERSION.SDK_INT + "}");

			if (!request.httpUrl().encodedPath().contains("users/login") && !request.httpUrl().encodedPath().contains("users/register")) {
				builder.addHeader("Cache-Control", "public, max-stale=" + String.valueOf(MAX_STALE)); // tolerate 3 hours stale
			}

			String s = AccountHelper.peekToken(mManager);
			if (!TextUtils.isEmpty(s)) {
				builder.addHeader("Authorization", "Bearer " + s);
			}

			return chain.proceed(builder.build());
		}
	}

	private static final class NetworkExceptionInterceptor implements Interceptor {

		private final Gson mGson;

		public NetworkExceptionInterceptor(Gson gson) {
			mGson = gson;
		}

		@Override
		public Response intercept(Chain chain) throws IOException {
			Response response = chain.proceed(chain.request());
			if (response != null && !response.isSuccessful()) {

				//Try to get response body
				try {
					NetworkException exception = mGson.fromJson(response.body().charStream(), NetworkException.class);

					if (exception.status != NetworkException.UNSET && !TextUtils.isEmpty(exception.getMessage())) {
						exception.code = response.code();
						throw exception;
					}
				} catch (IOException e) {
					Dog.e(e, "IOException");
				} catch (JsonSyntaxException e) {
					Dog.v("JsonSyntaxException");
					// Ignore
				}
			}

			return response;
		}
	}
}
