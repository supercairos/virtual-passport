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
package io.romain.passport.logic.components;

import javax.inject.Singleton;

import dagger.Component;
import io.romain.passport.logic.modules.AccountModule;
import io.romain.passport.logic.modules.ApplicationContextModule;
import io.romain.passport.logic.modules.GsonModule;
import io.romain.passport.logic.modules.OkHttpModule;
import io.romain.passport.logic.modules.RetrofitModule;
import io.romain.passport.logic.modules.SharedPrefModule;
import io.romain.passport.logic.services.BaseIntentService;
import io.romain.passport.logic.services.account.AuthenticatorService;
import io.romain.passport.ui.BaseActivity;
import io.romain.passport.ui.adaptater.CitySearchAdapter;
import io.romain.passport.ui.fragments.BaseFragment;
import io.romain.passport.ui.fragments.dialogs.BaseDialogFragment;
import io.romain.passport.utils.glide.GlideConfiguration;

@Singleton
@Component(modules = {AccountModule.class, SharedPrefModule.class, RetrofitModule.class, OkHttpModule.class, GsonModule.class, ApplicationContextModule.class})
public interface ApplicationComponent {
	void inject(BaseActivity activity);

	void inject(BaseFragment activity);

	void inject(BaseIntentService baseIntentService);

	void inject(BaseDialogFragment baseDialogFragment);

	void inject(GlideConfiguration configuration);

	void inject(AuthenticatorService.Authenticator authenticator);

	void inject(CitySearchAdapter citySearchAdapter);
}
