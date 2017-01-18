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

package com.imgtec.sesame.presentation.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.imgtec.di.HasComponent;
import com.imgtec.sesame.R;
import com.imgtec.sesame.data.DataService;
import com.imgtec.sesame.data.api.pojo.Log;
import com.imgtec.sesame.data.api.pojo.Logs;
import com.imgtec.sesame.presentation.AbstractDataCallback;
import com.imgtec.sesame.presentation.ActivityComponent;
import com.imgtec.sesame.presentation.UiHelper;
import com.imgtec.sesame.presentation.adapters.LogsAdapter;
import com.imgtec.sesame.presentation.helpers.RecyclerItemClickSupport;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;

/**
 *
 */
public class LogsFragment extends BaseFragment {

  @BindView(R.id.recyclerView) RecyclerView recyclerView;

  @Inject DataService dataService;
  @Inject @Named("Main") Handler mainHandler;
  @Inject
  UiHelper uiHelper;

  private LogsAdapter adapter;

  public LogsFragment() {
    // Required empty public constructor
  }

  public static LogsFragment newInstance() {
    LogsFragment fragment = new LogsFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_logs, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setupAdapter();
    requestLogs();
  }

  @Override
  protected void setComponent() {
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);
  }

  private void setupAdapter() {
    final DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
    adapter = new LogsAdapter();
    recyclerView.setAdapter(adapter);
    recyclerView.setItemAnimator(itemAnimator);
    recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
        DividerItemDecoration.VERTICAL));
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    RecyclerItemClickSupport.addTo(recyclerView)
        .setOnItemClickListener((recyclerView1, position, view) -> {


        });
  }

  private void requestLogs() {
    dataService.requestLogs(new RequestLogsCallback(LogsFragment.this, mainHandler));
  }

  static class RequestLogsCallback extends AbstractDataCallback<LogsFragment,DataService, Logs> {


    public RequestLogsCallback(LogsFragment fragment, Handler mainHandler) throws IllegalArgumentException {
      super(fragment, mainHandler);
    }

    @Override
    protected void onSuccess(LogsFragment fragment, DataService service, Logs result) {
      if (result != null) {
        fragment.load(result.getLogs());
      }
    }

    @Override
    protected void onFailure(LogsFragment fragment, DataService service, Throwable t) {
      fragment.uiHelper.showToast("Failed to request logs! "+ t.getMessage(), Toast.LENGTH_LONG);
    }
  }

  private void load(List<Log> items) {
    adapter.clear();
    for (Log l: items) {
      adapter.add(new LogsAdapter.LogItem(l));
    }
    adapter.notifyDataSetChanged();

  }
}
