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


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.imgtec.di.HasComponent;
import com.imgtec.sesame.R;
import com.imgtec.sesame.presentation.ActivityComponent;

import butterknife.BindView;

/**
 *
 */
public class MainFragment extends BaseFragment {


  @BindView(R.id.tab_host) FragmentTabHost tabHost;

  public MainFragment() {
    // Required empty public constructor
  }

  public static MainFragment newInstance() {
    MainFragment fragment = new MainFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_main, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    tabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);

    tabHost.addTab(tabHost.newTabSpec("controller").setIndicator(getTabIndicator(getContext(), "controller")),
        ControllerFragment.class, null);
    tabHost.addTab(tabHost.newTabSpec("statistics").setIndicator(getTabIndicator(getContext(), "statistics")),
        StatisticsFragment.class, null);
    tabHost.addTab(tabHost.newTabSpec("logs").setIndicator(getTabIndicator(getContext(), "logs")),
        LogsFragment.class, null);
  }

  private View getTabIndicator(Context context, String title) {
    View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
    TextView tv = (TextView) view.findViewById(R.id.tab_title);
    tv.setText(title);
    return view;
  }

  @Override
  protected void setComponent() {
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);
  }
}
