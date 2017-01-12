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

package com.imgtec.sesame.data;

import android.os.Handler;

import com.imgtec.sesame.data.api.HostWrapper;
import com.imgtec.sesame.data.api.RestApiService;
import com.imgtec.sesame.data.api.pojo.Api;
import com.imgtec.sesame.data.api.pojo.DoorsEntrypoint;
import com.imgtec.sesame.data.api.pojo.DoorsStatistics;
import com.imgtec.sesame.data.api.pojo.Log;
import com.imgtec.sesame.data.api.pojo.Logs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
 */
public class DataServiceImpl implements DataService {

  private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

  private final ScheduledExecutorService executor;
  private final Handler handler;
  private final HostWrapper hostWrapper;
  private final RestApiService apiService;

  public DataServiceImpl(ScheduledExecutorService executorService,
                         Handler handler,
                         HostWrapper hostWrapper,
                         RestApiService apiService) {
    super();
    this.executor = executorService;
    this.handler = handler;
    this.hostWrapper = hostWrapper;
    this.apiService = apiService;
  }

  @Override
  public void performSync() {
    Call<Api> api = apiService.api(hostWrapper.getHost());
    api.enqueue(new Callback<Api>() {
      @Override
      public void onResponse(Call<Api> call, Response<Api> response) {

        String doorsUrl = response.body().getLinkByRel("doors").getHref();
        Call<DoorsEntrypoint> doorsEndpoints = apiService.entrypoint(doorsUrl);
        doorsEndpoints.enqueue(new Callback<DoorsEntrypoint>() {
          @Override
          public void onResponse(Call<DoorsEntrypoint> call, Response<DoorsEntrypoint> response) {

            final DoorsEntrypoint ep = response.body();
            String operateUrl = ep.getLinkByRel("operate").getHref();
            apiService.operate(operateUrl).enqueue(new Callback<Void>() {
              @Override
              public void onResponse(Call<Void> call, Response<Void> response) {

              }

              @Override
              public void onFailure(Call<Void> call, Throwable t) {

              }
            });

            String statsUrl = ep.getLinkByRel("stats").getHref();;
            apiService.statistics(statsUrl).enqueue(new Callback<DoorsStatistics>() {
              @Override
              public void onResponse(Call<DoorsStatistics> call, Response<DoorsStatistics> response) {

              }

              @Override
              public void onFailure(Call<DoorsStatistics> call, Throwable t) {

              }
            });

            String logsUrl = ep.getLinkByRel("logs").getHref();
            apiService.logs(logsUrl, null, null).enqueue(new Callback<Logs>() {
              @Override
              public void onResponse(Call<Logs> call, Response<Logs> response) {

              }

              @Override
              public void onFailure(Call<Logs> call, Throwable t) {

              }
            });
          }

          @Override
          public void onFailure(Call<DoorsEntrypoint> call, Throwable t) {

          }
        });


      }

      @Override
      public void onFailure(Call<Api> call, Throwable t) {

      }
    });


  }


  @Override
  public void requestLogs(final DataCallback<DataService, List<Log>> callback) {
    Call<Api> api = apiService.api(hostWrapper.getHost());
    api.enqueue(new Callback<Api>() {
      @Override
      public void onResponse(Call<Api> call, Response<Api> response) {

        String doorsUrl = response.body().getLinkByRel("doors").getHref();
        Call<DoorsEntrypoint> doorsEndpoints = apiService.entrypoint(doorsUrl);
        doorsEndpoints.enqueue(new Callback<DoorsEntrypoint>() {
          @Override
          public void onResponse(Call<DoorsEntrypoint> call, Response<DoorsEntrypoint> response) {

            final DoorsEntrypoint ep = response.body();
            String operateUrl = ep.getLinkByRel("operate").getHref();
            apiService.operate(operateUrl).enqueue(new Callback<Void>() {
              @Override
              public void onResponse(Call<Void> call, Response<Void> response) {

              }

              @Override
              public void onFailure(Call<Void> call, Throwable t) {

              }
            });

            String logsUrl = ep.getLinkByRel("logs").getHref();
            apiService.logs(logsUrl, null, null).enqueue(new Callback<Logs>() {
              @Override
              public void onResponse(Call<Logs> call, Response<Logs> response) {
                Logs logs = response.body();
                callback.onSuccess(DataServiceImpl.this, logs.getLogs());
              }

              @Override
              public void onFailure(Call<Logs> call, Throwable t) {
                callback.onFailure(DataServiceImpl.this, t);
              }
            });
          }

          @Override
          public void onFailure(Call<DoorsEntrypoint> call, Throwable t) {

          }
        });


      }

      @Override
      public void onFailure(Call<Api> call, Throwable t) {

      }
    });


  }
}
