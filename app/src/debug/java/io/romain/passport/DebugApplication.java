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

import android.os.StrictMode;

import com.facebook.stetho.Stetho;

import io.romain.passport.utils.Dog;
import io.romain.passport.utils.StethoBone;

public class DebugApplication extends MyApplication {

	@Override
	public void onCreate() {
		super.onCreate();

		Dog.bury(new Dog.DebugBone());
		Dog.bury(new StethoBone());

		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
				.detectAll() //
				.penaltyFlashScreen() //
				.penaltyLog()
				.build()
		);

		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectAll()
				.penaltyLog()
				.build()
		);

		Stetho.initialize(
				Stetho.newInitializerBuilder(this)
						.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
						.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
						.build()
		);

	}
}