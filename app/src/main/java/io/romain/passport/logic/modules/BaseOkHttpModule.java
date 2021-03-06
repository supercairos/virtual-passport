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
package io.romain.passport.logic.modules;

import android.accounts.AccountManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.romain.passport.logic.helpers.AccountHelper;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BaseOkHttpModule {

	private static final long TIMEOUT = 180;

	protected static OkHttpClient.Builder getBaseOkHttpClientBuilder(Context context, AccountManager manager) {
		return new OkHttpClient.Builder()
				.addInterceptor(new HeaderInterceptor(manager))
				.connectTimeout(TIMEOUT, TimeUnit.SECONDS)
				.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
				.cache(new Cache(context.getCacheDir(), 10 * 1024 * 1024));
	}

	private static final class HeaderInterceptor implements Interceptor {

		private static final int MAX_STALE = 60 * 3;

		private final AccountManager mManager;

		public HeaderInterceptor(AccountManager manager) {
			mManager = manager;
		}

		@Override
		public Response intercept(Chain chain) throws IOException {
			Request request = chain.request();
			Request.Builder builder = request.newBuilder()
					.addHeader("User-Agent", "VirtualPassport-Client {Android-" + Build.VERSION.SDK_INT + "} {" + Build.DEVICE + "}");

			if (request.header("Cache-Control") == null) {
				builder.cacheControl(
						new CacheControl.Builder()
								.maxStale(MAX_STALE, TimeUnit.MINUTES)
								.build()
				);
			}

			String s = AccountHelper.peekToken(mManager);
			if (!TextUtils.isEmpty(s)) {
				builder.addHeader("Authorization", "Bearer " + s);
			}

			return chain.proceed(builder.build());
		}
	}

}
