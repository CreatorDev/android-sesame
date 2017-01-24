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

import com.imgtec.sesame.data.api.DoorsHelper;
import com.imgtec.sesame.data.api.HostWrapper;
import com.imgtec.sesame.data.api.RestApiService;
import com.imgtec.sesame.data.api.pojo.Api;
import com.imgtec.sesame.data.api.pojo.DoorsAction;
import com.imgtec.sesame.data.api.pojo.DoorsEntrypoint;
import com.imgtec.sesame.data.api.pojo.DoorsState;
import com.imgtec.sesame.data.api.pojo.DoorsStatistics;
import com.imgtec.sesame.data.api.pojo.Logs;
import com.imgtec.sesame.utils.Condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.ResponseBody;
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
  private final AtomicReference<DoorsEntrypoint> entrypoint;
  private boolean pollingEnabled;

  private DoorsState doorState;

  public DataServiceImpl(ScheduledExecutorService executorService,
                         Handler handler,
                         HostWrapper hostWrapper,
                         RestApiService apiService) {
    super();
    this.executor = executorService;
    this.handler = handler;
    this.hostWrapper = hostWrapper;
    this.apiService = apiService;
    this.entrypoint = new AtomicReference<>(null);
  }

  @Override
  public void requestApi(DataCallback<DataService, Api> callback) {
    executor.execute(() -> {
      try {
        Response<Api> api = apiService.api(hostWrapper.getHost()).execute();
        ResponseBody error = api.errorBody();
        if (error != null) {
          throw new IOException(error.string());
        }
        callback.onSuccess(DataServiceImpl.this, api.body());
      } catch (IOException e) {
        callback.onFailure(DataServiceImpl.this, e);
      }
    });
  }

  @Override
  public void requestLogs(final DataCallback<DataService, Logs> callback) {

    executor.execute(new EntryPointRequestor<DataService, Logs>(
        DataServiceImpl.this, apiService, hostWrapper, callback) {

      @Override
      Response<Logs> onExecute(RestApiService service,
                          HostWrapper hostWrapper,
                          DoorsEntrypoint endpoint) throws IOException {

        String logsUrl = endpoint.getLinkByRel("logs").getHref();
        Response<Logs> logs = service.logs(logsUrl, 50, 0).execute();

        return logs;
      }
    });
  }

  @Override
  public void requestStatistics(DataCallback<DataService, DoorsStatistics> callback) {

    executor.execute(new EntryPointRequestor<DataService, DoorsStatistics>(
        DataServiceImpl.this, apiService, hostWrapper, callback) {

      @Override
      Response<DoorsStatistics> onExecute(RestApiService service,
                          HostWrapper hostWrapper,
                          DoorsEntrypoint endpoint) throws IOException {

        Response<DoorsStatistics> stats = apiService
            .statistics(endpoint.getLinkByRel("stats").getHref())
            .execute();

        return stats;
      }
    });
  }

  @Override
  public void requestState(final DataCallback<DataService, DoorsState> callback) {
    executor.execute(new EntryPointRequestor<DataService, DoorsState>(
        DataServiceImpl.this, apiService, hostWrapper, callback) {

      @Override
      Response<DoorsState> onExecute(RestApiService service, HostWrapper hostWrapper, DoorsEntrypoint endpoint) throws IOException {
        Response<DoorsState> state = apiService.state(endpoint.getLinkByRel("state").getHref())
            .execute();

        DoorsState s = state.body();
        if (DoorsHelper.isDoorOpened(s) || DoorsHelper.isDoorClosed(s)) {
          synchronized (DataServiceImpl.this) {
            doorState = s;
          }
        }

        return state;
      }
    });
  }

  @Override
  public void clearCache() {
    entrypoint.set(null);
  }

  @Override
  public AtomicReference<DoorsEntrypoint> getCachedEntryPoint() {
    return entrypoint;
  }

  @Override
  public void startPollingDoorState(DataCallback<DataService, DoorsState> callback) {
    synchronized (this) {
      if (!pollingEnabled) {
        pollingEnabled = true;
        schedulePollingTask(callback);
        logger.debug("Polling task started!");
      }
    }
  }

  @Override
  public void stopPollingDoorState() {
    synchronized (this) {
      if (pollingEnabled) {
        pollingEnabled = false;
        logger.debug("Polling task stopped!");
      }
    }
  }

  @Override
  public synchronized DoorsState getLastDoorsState() {
    return doorState;
  }

  @Override
  public void performOperate() {
    executor.execute(() -> {

      try {
        AtomicReference<DoorsEntrypoint> entrypoint = cacheEntryPointAndGet();
        Response<Void> response = apiService.operate(entrypoint.get().getLinkByRel("operate").getHref()).execute();

        ResponseBody err = response.errorBody();
        if (err != null) {
          throw new IOException(err.string());
        }
        //notify
      } catch (Exception e) {
        e.printStackTrace();
        //notify
      }
    });
  }

  @Override
  public void openDoors(DataCallback<DataService, DoorsAction> callback) {

    executor.execute(new EntryPointRequestor<DataService, DoorsAction>(
        DataServiceImpl.this, apiService, hostWrapper, callback) {


      @Override
      Response<DoorsAction> onExecute(RestApiService service, HostWrapper hostWrapper, DoorsEntrypoint entrypoint) throws IOException {
        logger.debug("Performing OPEN DOOR operation");
        Response<DoorsAction> action = apiService.open(entrypoint.getLinkByRel("open").getHref()).execute();

        return action;
      }
    });
  }

  @Override
  public void closeDoors(DataCallback<DataService, DoorsAction> callback) {
    executor.execute(new EntryPointRequestor<DataService, DoorsAction>(
        DataServiceImpl.this, apiService, hostWrapper, callback) {

      @Override
      Response<DoorsAction> onExecute(RestApiService service, HostWrapper hostWrapper, DoorsEntrypoint entrypoint) throws IOException {
        logger.debug("Performing CLOSE DOOR operation");
        Response<DoorsAction> action = apiService.close(entrypoint.getLinkByRel("close").getHref()).execute();

        return action;
      }
    });
  }

  @Override
  public void resetStatistics(DataCallback<DataService, Void> callback) {
    executor.execute(new EntryPointRequestor<DataService, Void>(
        DataServiceImpl.this, apiService, hostWrapper, callback){

      @Override
      Response<Void> onExecute(RestApiService service, HostWrapper hostWrapper, DoorsEntrypoint entrypoint) throws IOException {
        Response<Void> logs = apiService.deleteStatistics(entrypoint.getLinkByRel("stats").getHref()).execute();
        return logs;
      }
    });
  }

  private AtomicReference<DoorsEntrypoint> cacheEntryPointAndGet() throws IOException {
    AtomicReference<DoorsEntrypoint> entrypoint = null;
    synchronized (DataServiceImpl.this) {
      entrypoint = getCachedEntryPoint();

      if (entrypoint.get() == null) {
        logger.debug("Entrypoint missing, requesting...");
        entrypoint.set(getEntrypoint(apiService, hostWrapper));
      }
    }

    if (entrypoint.get() == null) {
      throw new IllegalStateException("Entrypoint is null!");
    }
    return entrypoint;
  }

  private void schedulePollingTask(DataCallback<DataService, DoorsState> callback) {
    executor.schedule(new PollingTask(callback), 2, TimeUnit.SECONDS);
  }

  /**
   * Performs a set of http request synchronously to get {@link DoorsEntrypoint}.
   * This method should be called from worker thread.
   * @throws IOException if a problem occurs while interacting with the server
   */
  private static DoorsEntrypoint getEntrypoint(RestApiService service, HostWrapper hostWrapper) throws IOException {
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
  static abstract class EntryPointRequestor<S extends DataService, T> implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
    private final S service;
    private final RestApiService restService;
    private final HostWrapper hostWrapper;
    private final DataCallback<S, T> callback;

    EntryPointRequestor(S service, RestApiService restService, HostWrapper hostWrapper,
                        DataCallback<S, T> callback) {
      this.service = service;
      this.restService = restService;
      this.hostWrapper = hostWrapper;
      this.callback = callback;
    }

    @Override
    public void run() {
      try {

        Condition.check(callback != null, "Callback cannot be NULL");

        AtomicReference<DoorsEntrypoint> entrypoint = null;
        synchronized (service) {
          entrypoint = service.getCachedEntryPoint();

          if (entrypoint.get() == null) {
            logger.debug("Entrypoint missing, requesting...");
            entrypoint.set(getEntrypoint(restService, hostWrapper));
          }
        }

        if (entrypoint.get() == null) {
          throw new IllegalStateException("Entrypoint is null!");
        }

        Response<T> response = onExecute(restService, hostWrapper, entrypoint.get());

        final ResponseBody error = response.errorBody();
        if (error != null) {
          throw new IOException(error.string());
        }

        callback.onSuccess(service, response.body());
      }
      catch (Exception e) {
        logger.error("Executing task failed!", e);
        notifyFailure(service, callback, e);
      }
    }

    abstract Response<T> onExecute(RestApiService service, HostWrapper hostWrapper, DoorsEntrypoint entrypoint) throws IOException;
  }

  private class PollingTask implements Runnable {

    private final DataCallback<DataService, DoorsState> callback;

    public PollingTask(DataCallback<DataService, DoorsState> callback) {
      this.callback = callback;
    }

    @Override
    public void run() {
      logger.debug("Executing polling task: {}", this);

      boolean polling;
      requestState(callback);
      synchronized (DataServiceImpl.this) {
        polling = pollingEnabled;
      }

      if (polling) {
        schedulePollingTask(callback);
      }
      logger.debug("Polling task finished: {}", this);
    }
  }
}
