/*
 * <b>Copyright (c) 2017, Imagination Technologies Limited and/or its affiliated group companies
 *  and/or licensors. </b>
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are permitted
 *  provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this list of conditions
 *      and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of
 *      conditions and the following disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors may be used to
 *      endorse or promote products derived from this software without specific prior written
 *      permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 *  WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.imgtec.sesame.app;

import android.content.SharedPreferences;
import android.os.Handler;

import com.imgtec.di.PerApp;
import com.imgtec.sesame.data.api.CredentialsWrapper;
import com.imgtec.sesame.data.DataModule;
import com.imgtec.sesame.data.DataService;
import com.imgtec.sesame.data.api.HostWrapper;
import com.imgtec.sesame.data.api.ApiModule;
import com.imgtec.sesame.data.api.RestApiService;
import com.imgtec.sesame.presentation.helpers.NetworkHelper;

import javax.inject.Named;

import dagger.Component;
import okhttp3.OkHttpClient;

@PerApp
@Component(
    modules = {
        ApplicationModule.class,
        DataModule.class,
        ApiModule.class
    }
)
public interface ApplicationComponent {

  final class Initializer {

    private Initializer() {}

    static ApplicationComponent init(App application) {
      return DaggerApplicationComponent
          .builder()
          .applicationModule(new ApplicationModule(application))
          .dataModule(new DataModule())
          .build();
    }
  }

  App inject(App app);

  SharedPreferences getSharedPreferences();

  @Named("Main") Handler getHandler();

  HostWrapper getHostWrapper();

  CredentialsWrapper getCredentialsWrapper();

  DataService getDataService();

  NetworkHelper getNetworkHelper();

  RestApiService getRestApiService();

}

