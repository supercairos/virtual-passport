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

import com.facebook.stetho.okhttp3.StethoInterceptor;

import dagger.Module;
import dagger.Provides;
import io.romain.passport.utils.Dog;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
public class OkHttpModule {

	@Provides
	protected OkHttpClient getOkHttpClient(Context context, AccountManager manager) {
		return BaseOkHttpModule
				.getBaseOkHttpClientBuilder(context, manager)
				.addInterceptor(
						new HttpLoggingInterceptor(message -> Dog.tag("OkHttp").d(message))
								.setLevel(HttpLoggingInterceptor.Level.HEADERS)
				)
				.addNetworkInterceptor(new StethoInterceptor())
				.build();
	}

}
