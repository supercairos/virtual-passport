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
package io.romain.passport.utils;

import android.content.ContentResolver;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okio.BufferedSink;

public class UriRequestBody extends RequestBody {
	private final Uri mUri;
	private final ContentResolver mContentResolver;

	/**
	 * Constructs a new typed file.
	 *
	 * @throws NullPointerException if file or mimeType is null
	 */
	public UriRequestBody(ContentResolver resolver, Uri uri) {
		if (resolver == null) {
			throw new NullPointerException("resolver");
		}
		if (uri == null) {
			throw new NullPointerException("uri");
		}

		mContentResolver = resolver;
		mUri = uri;
	}

	@Override
	public MediaType contentType() {
		String mime = mContentResolver.getType(mUri);
		if (TextUtils.isEmpty(mime)) {
			String extension = MimeTypeMap.getFileExtensionFromUrl(mUri.toString());
			if (!TextUtils.isEmpty(extension)) {
				mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			} else {
				mime = "text/plain";
			}
		}

		return MediaType.parse(mime + "; charset=utf-8");
	}

	@Override
	public long contentLength() throws IOException {
		return getInputStream(mUri).available();
	}

	@Override
	public void writeTo(BufferedSink sink) throws IOException {
		byte[] buffer = new byte[1024]; // Adjust if you want
		InputStream input = new BufferedInputStream(getInputStream(mUri), 1024);

		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			sink.write(buffer, 0, bytesRead);
		}
	}

	private InputStream getInputStream(Uri uri) throws FileNotFoundException {
		String scheme = uri.getScheme();
		if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme) ||
				ContentResolver.SCHEME_CONTENT.equals(scheme) ||
				ContentResolver.SCHEME_FILE.equals(scheme)) {
			return mContentResolver.openInputStream(uri);
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
