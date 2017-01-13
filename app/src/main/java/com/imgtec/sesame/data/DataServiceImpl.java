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
import com.imgtec.sesame.data.api.pojo.StatsEntry;
import com.imgtec.sesame.utils.Condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

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
    executor.execute(() -> {
      try {
        Response<Api> api = apiService.api(hostWrapper.getHost()).execute();
      } catch (IOException e) {
        notifyFailure(DataServiceImpl.this, null, e);
      }
    });
  }

  @Override
  public void requestLogs(final DataCallback<DataService, List<Log>> callback) {

    executor.execute(new EndpointRequestor<DataService, List<Log>>(
        DataServiceImpl.this, apiService, hostWrapper, callback) {

      @Override
      List<Log> onExecute(RestApiService service,
                          HostWrapper hostWrapper,
                          DoorsEntrypoint endpoint) throws IOException {

        String logsUrl = endpoint.getLinkByRel("logs").getHref();
        Response<Logs> logs = service.logs(logsUrl, null, null).execute();

        return logs.body().getLogs();
      }
    });
  }

  @Override
  public void requestStatistics(DataCallback<DataService, DoorsStatistics> callback) {

    executor.execute(new EndpointRequestor<DataService, DoorsStatistics>(
        DataServiceImpl.this, apiService, hostWrapper, callback) {

      @Override
      DoorsStatistics onExecute(RestApiService service,
                          HostWrapper hostWrapper,
                          DoorsEntrypoint endpoint) throws IOException {

        Response<DoorsStatistics> stats = apiService
            .statistics(endpoint.getLinkByRel("stats").getHref())
            .execute();

        return stats.body();
      }
    });
  }

  /**
   * Performs a set of http request synchronously to get {@link DoorsEntrypoint}.
   * This method should be called from worker thread.
   * @throws IOException if a problem occurre while interacting with the server
   */
  private static DoorsEntrypoint getEndpoint(RestApiService service, HostWrapper hostWrapper) throws IOException {
    Response<Api> api = service.api(hostWrapper.getHost()).execute();

    String doorsUrl = api.body().getLinkByRel("doors").getHref();
    Response<DoorsEntrypoint> endpoints = service.entrypoint(doorsUrl).execute();
    return endpoints.body();
  }

  private static <S, T> void notifyFailure(S service, DataCallback<S, T> callback, Throwable t) {
    if (callback != null) {
      callback.onFailure(service, t);
    }
  }

  /**
   * Base task that provides {@link DoorsEntrypoint} and lets implementer to perform rest request
   * with proper resource.
   * @param <S> data service
   * @param <T> expected response type
   */
  static abstract class EndpointRequestor<S, T> implements Runnable {

    private final S service;
    private final RestApiService restService;
    private final HostWrapper hostWrapper;
    private final DataCallback<S, T> callback;

    EndpointRequestor(S service, RestApiService restService, HostWrapper hostWrapper,
                      DataCallback<S, T> callback) {
      this.service = service;
      this.restService = restService;
      this.hostWrapper = hostWrapper;
      this.callback = callback;
    }

    @Override
    public void run() {
      try {
        final DoorsEntrypoint endpoint = getEndpoint(restService, hostWrapper);
        T t = onExecute(restService, hostWrapper, endpoint);

        callback.onSuccess(service, t);
      }
      catch (IOException e) {
        notifyFailure(service, callback, e);
      }
    }

    abstract T onExecute(RestApiService service, HostWrapper hostWrapper, DoorsEntrypoint endpoint) throws IOException;
  }
}
