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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.imgtec.di.HasComponent;
import com.imgtec.sesame.R;
import com.imgtec.sesame.data.DataService;
import com.imgtec.sesame.data.api.pojo.DoorsStatistics;
import com.imgtec.sesame.data.api.pojo.StatsEntry;
import com.imgtec.sesame.presentation.AbstractDataCallback;
import com.imgtec.sesame.presentation.ActivityComponent;
import com.imgtec.sesame.presentation.UiHelper;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.OnClick;

/**
 *
 */
public class StatisticsFragment extends BaseFragment {

  @Inject DataService dataService;
  @Inject @Named("Main") Handler mainHandler;
  @Inject
  UiHelper uiHelper;

  @BindView(R.id.statistics) TextView text;

  public StatisticsFragment() {
    // Required empty public constructor
  }

  public static StatisticsFragment newInstance() {
    StatisticsFragment fragment = new StatisticsFragment();
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
    return inflater.inflate(R.layout.fragment_statistics, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    requestStatistics();
  }

  @Override
  protected void setComponent() {
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);
  }

  private void requestStatistics() {
    dataService.requestStatistics(new StatisticsCallback(StatisticsFragment.this, mainHandler));
  }

  private void showStatistics(DoorsStatistics statistics) {
    text.setText(format(statistics));
  }


  @OnClick(R.id.reset_statistics)
  void onResetStatistics() {
    dataService.resetStatistics(new ResetStatsCallback(StatisticsFragment.this, mainHandler));
  }

  private String format(DoorsStatistics statistics) {
    StringBuilder sb = new StringBuilder();
    sb.append(getString(R.string.since)).append(":\t").append(statistics.getSince()).append("\n");
    sb.append("\n");
    sb.append(getString(R.string.openings)).append(":\n");
    format(statistics.getOpening(), sb);
    sb.append("\n");
    sb.append(getString(R.string.closings)).append(":\n");
    format(statistics.getClosing(), sb);
    return sb.toString();
  }

  private void format(StatsEntry statsEntry, StringBuilder sb) {
    sb.append("\t").append(getString(R.string.min)).append(":\t").append(format(statsEntry.getMin())).append("\n");
    sb.append("\t").append(getString(R.string.max)).append(":\t").append(format(statsEntry.getMax())).append("\n");
    sb.append("\t").append(getString(R.string.avg)).append(":\t").append(format(statsEntry.getAvg())).append("\n");
  }

  private static String format(Double value) {
    return java.text.NumberFormat.getNumberInstance().format(value);
  }


  static class StatisticsCallback extends AbstractDataCallback<StatisticsFragment,
      DataService, DoorsStatistics> {

    StatisticsCallback(StatisticsFragment fragment, Handler mainHandler) throws IllegalArgumentException {
      super(fragment, mainHandler);
    }

    @Override
    protected void onSuccess(StatisticsFragment fragment, DataService service, DoorsStatistics result) {
      fragment.showStatistics(result);
    }

    @Override
    protected void onFailure(StatisticsFragment fragment, DataService service, Throwable t) {
      fragment.uiHelper.showToast("Requesting statistics failed!" + t.getMessage(), Toast.LENGTH_LONG);
    }
  }

  static class ResetStatsCallback extends AbstractDataCallback<StatisticsFragment, DataService, Void> {

    public ResetStatsCallback(StatisticsFragment fragment, Handler mainHandler) throws IllegalArgumentException {
      super(fragment, mainHandler);
    }

    @Override
    protected void onSuccess(StatisticsFragment fragment, DataService service, Void result) {
      fragment.requestStatistics();
    }

    @Override
    protected void onFailure(StatisticsFragment fragment, DataService service, Throwable t) {
      fragment.uiHelper.showToast("Reset statistics failed! "+ t.getMessage(), Toast.LENGTH_SHORT);
    }
  }
}
