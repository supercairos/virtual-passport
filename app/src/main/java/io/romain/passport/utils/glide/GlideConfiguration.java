/*
 * Copyright 2015 Romain
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.romain.passport.utils.glide;

import android.app.ActivityManager;
import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.GlideModule;

import java.io.InputStream;

import javax.inject.Inject;

import io.romain.passport.MyApplication;
import okhttp3.OkHttpClient;

/**
 * Configure Glide to set desired image quality.
 */
public class GlideConfiguration implements GlideModule {

	private static final int DISK_CACHE_SIZE = 150 * 1024 * 1024;

	@Inject
	OkHttpClient mClient;

	@Override
	public void applyOptions(Context context, GlideBuilder builder) {
		// Prefer higher quality images unless we're on a low RAM device
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		builder.setDecodeFormat(
				activityManager.isLowRamDevice() ? DecodeFormat.PREFER_RGB_565 : DecodeFormat.PREFER_ARGB_8888
		);

		builder.setDiskCache(new InternalCacheDiskCacheFactory(context, "glide", DISK_CACHE_SIZE));
	}

	@Override
	public void registerComponents(Context context, Glide glide) {
		MyApplication.getApplication(context).getApplicationComponent().inject(this);

		glide.register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(mClient));
	}
}
