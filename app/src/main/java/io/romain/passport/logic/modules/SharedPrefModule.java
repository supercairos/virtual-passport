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

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import io.romain.passport.BuildConfig;
import io.romain.passport.logic.helpers.SharedPrefHelper;
import io.romain.passport.utils.Dog;

@Module
public class SharedPrefModule {

	@Provides
	SharedPrefHelper getSharedPrefManager(Context context) {
		if(BuildConfig.DEBUG) Dog.d("Called()");

		return new SharedPrefHelper(context);
	}
}
