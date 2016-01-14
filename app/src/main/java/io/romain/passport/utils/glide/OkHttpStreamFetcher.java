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
package io.romain.passport.utils.glide;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.util.ContentLengthInputStream;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Fetches an {@link InputStream} using the okhttp library.
 */
public class OkHttpStreamFetcher implements DataFetcher<InputStream> {
	private final OkHttpClient client;
	private final GlideUrl url;
	private InputStream stream;
	private ResponseBody responseBody;

	public OkHttpStreamFetcher(OkHttpClient client, GlideUrl url) {
		this.client = client;
		this.url = url;
	}

	@Override
	public InputStream loadData(Priority priority) throws Exception {
		Request.Builder requestBuilder = new Request.Builder()
				.url(url.toStringUrl());

		for (Map.Entry<String, String> headerEntry : url.getHeaders().entrySet()) {
			String key = headerEntry.getKey();
			requestBuilder.addHeader(key, headerEntry.getValue());
		}

		Request request = requestBuilder.build();

		Response response = client.newCall(request).execute();
		responseBody = response.body();
		if (!response.isSuccessful()) {
			throw new IOException("Request failed with code: " + response.code());
		}

		long contentLength = responseBody.contentLength();
		stream = ContentLengthInputStream.obtain(responseBody.byteStream(), contentLength);
		return stream;
	}

	@Override
	public void cleanup() {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				// Ignored
			}
		}
		if (responseBody != null) {
			try {
				responseBody.close();
			} catch (IOException e) {
				// Ignored.
			}
		}
	}

	@Override
	public String getId() {
		return url.getCacheKey();
	}

	@Override
	public void cancel() {
		// TODO: call cancel on the client when this method is called on a background thread. See #257
	}
}
