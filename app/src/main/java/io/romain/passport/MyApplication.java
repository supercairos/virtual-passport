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
package io.romain.passport;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import com.example.romain.virtualpassport.BuildConfig;

import io.romain.passport.logic.components.ApplicationComponent;
import io.romain.passport.logic.components.DaggerApplicationComponent;
import io.romain.passport.logic.modules.ApplicationContextModule;
import io.romain.passport.utils.Dog;

public class MyApplication extends Application {

	private ApplicationComponent mApplicationComponent;
	public ApplicationComponent getApplicationComponent() {
		return mApplicationComponent;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Dog.bury(new Dog.DebugBone());

		if (BuildConfig.DEBUG) {
			enabledStrictMode();
		}

		mApplicationComponent = DaggerApplicationComponent.builder()
				.applicationContextModule(new ApplicationContextModule(this))
				.build();
	}

	private void enabledStrictMode() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
					.detectAll() //
					.penaltyFlashScreen() //
					.penaltyLog()
					.build());
		}
	}

	public static MyApplication getApplication(Context context) {
		return ((MyApplication) context.getApplicationContext());
	}

}
