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

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.DateFormat;

import io.romain.passport.BuildConfig;
import io.romain.passport.data.City;
import io.romain.passport.data.CityAutocomplete;
import io.romain.passport.data.Comment;
import io.romain.passport.data.Forecast;
import io.romain.passport.data.Profile;
import io.romain.passport.utils.Dog;

public class BaseGsonModule {

	static GsonBuilder getBaseGsonBuilder() {
		if(BuildConfig.DEBUG) Dog.d("Called()");

		return new GsonBuilder()
				.excludeFieldsWithoutExposeAnnotation()
				.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
				.registerTypeAdapterFactory(new PassportAdapterFactory())
				.setDateFormat(DateFormat.LONG)
				.setVersion(1.0);
	}

	private static class PassportAdapterFactory implements TypeAdapterFactory {

		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			Class<? super T> rawType = type.getRawType();
			if (City.class.equals(rawType)) {
				return (TypeAdapter<T>) City.getTypeAdapter(gson);
			} else if (CityAutocomplete.class.equals(rawType)) {
				return (TypeAdapter<T>) CityAutocomplete.getTypeAdapter(gson);
			} else if (Forecast.class.equals(rawType)) {
				return (TypeAdapter<T>) Forecast.getTypeAdapter(gson);
			} else if (Profile.class.equals(rawType)) {
				return (TypeAdapter<T>) Profile.getTypeAdapter(gson);
			} else if (Comment.class.equals(rawType)) {
				return (TypeAdapter<T>) Comment.getTypeAdapter(gson);
			} else if (Uri.class.equals(rawType)) {
				return (TypeAdapter<T>) new UriAdapter();
			}

			return null;
		}
	}

	private static class UriAdapter extends TypeAdapter<Uri> {
		public Uri read(JsonReader reader) throws IOException {
			if (reader.peek() == JsonToken.NULL) {
				reader.nextNull();
				return null;
			}

			return Uri.parse(reader.nextString());
		}

		public void write(JsonWriter writer, Uri value) throws IOException {
			if (value == null) {
				writer.nullValue();
				return;
			}

			writer.value(value.toString());
		}

	}
}
