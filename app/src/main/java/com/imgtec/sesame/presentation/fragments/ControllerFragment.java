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


import android.graphics.drawable.GradientDrawable;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.imgtec.di.HasComponent;
import com.imgtec.sesame.R;
import com.imgtec.sesame.data.Configuration;
import com.imgtec.sesame.data.DataService;
import com.imgtec.sesame.data.Preferences;
import com.imgtec.sesame.data.api.CredentialsWrapper;
import com.imgtec.sesame.data.api.HostWrapper;
import com.imgtec.sesame.data.api.pojo.Api;
import com.imgtec.sesame.data.api.pojo.DoorsState;
import com.imgtec.sesame.presentation.AbstractDataCallback;
import com.imgtec.sesame.presentation.ActivityComponent;
import com.imgtec.sesame.presentation.helpers.NetworkHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.OnClick;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 *
 */
public class ControllerFragment extends BaseFragment {

  @Inject Preferences preferences;
  @Inject DataService dataService;
  @Inject HostWrapper hostWrapper;
  @Inject CredentialsWrapper credentialsWrapper;
  @Inject @Named("Main") Handler mainHandler;
  @Inject NetworkHelper networkHelper;

  @BindView(R.id.operate) Button operate;
  @BindView(R.id.status_message) TextView statusMessage;

  private AlertDialog configurationDialog;

  public ControllerFragment() {
    // Required empty public constructor
  }

  public static ControllerFragment newInstance() {
    ControllerFragment fragment = new ControllerFragment();
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
    return inflater.inflate(R.layout.fragment_controller, container, false);
  }

  @Override
  protected void setComponent() {
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    networkHelper.addNetworkStateListener(networkListener);

    if (preferences.getConfiguration() == null) {
      showConfigurationDialog();
    }
    else {
      syncWithWebapp();
    }
  }

  @Override
  public void onPause() {
    dataService.stopPollingDoorState();
    networkHelper.removeNetworkStateListener(networkListener);
    super.onPause();
  }

  private void syncWithWebapp() {
    if (networkHelper.isOnline()) {
      dataService.startPollingDoorState(new DoorsStateCallback(this, mainHandler));
    }
    else {
      dataService.stopPollingDoorState();
      updateOfflineState();
    }
  }

  private void updateOnlineState() {
    applyBgColor(operate, android.R.color.holo_green_light);
  }

  private void updateOfflineState() {
    applyBgColor(operate, R.color.secondary_color_dark_grey);
  }

  @OnClick(R.id.settings)
  void onSettingsClicked() {
    if (configurationDialog == null) {
      showConfigurationDialog();
    }
  }

  @OnClick(R.id.operate)
  void onOperateClicked(Button btn) {
    if (btn == operate) {
      applyBgColor(btn, android.R.color.holo_green_light);
    }
  }

  private void applyBgColor(Button btn, int color) {
    GradientDrawable bgShape = (GradientDrawable) btn.getBackground();
    bgShape.setColor(ContextCompat.getColor(getContext(), color));
  }


  private void showConfigurationDialog() {

    LayoutInflater inflater = LayoutInflater.from(getContext());
    final View dialogView = inflater.inflate(R.layout.configuration_dialog, null);
    final EditText host = (EditText) dialogView.findViewById(R.id.host);
    final EditText secret = (EditText) dialogView.findViewById(R.id.secret);

    Configuration configuration = preferences.getConfiguration();
    if (configuration != null) {
      host.setText(configuration.getHost());
      secret.setText(configuration.getSecret());
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
    builder
        .setTitle(R.string.enter_credentials)
        .setView(dialogView)
        .setNegativeButton(R.string.cancel, (dialog, which) -> {
          dialog.dismiss();
          configurationDialog = null;
        })
        .setPositiveButton(R.string.ok, (dialog, which) -> {

          String hostStr = host.getText().toString();
          String secretStr = secret.getText().toString();

          if (TextUtils.isEmpty(hostStr) || TextUtils.isEmpty(secretStr) ) {
            Toast.makeText(getContext(), "Host or secret is missing", Toast.LENGTH_LONG).show();
            return;
          }

          hostStr = verifyHostOrFix(hostStr);

          final String token = Jwts.builder()
              .setSubject("Subject")
              .signWith(SignatureAlgorithm.HS256, secretStr.getBytes())
              .compact();

          Configuration configuration1 = new Configuration(hostStr, secretStr, token);
          preferences.saveConfiguration(configuration1);

          //update wrapper
          hostWrapper.setHost(hostStr);
          credentialsWrapper.setSecret(secretStr);
          credentialsWrapper.setToken(token);

          dataService.clearCache();

          syncWithWebapp();

          dialog.dismiss();
          configurationDialog = null;
        });

    configurationDialog = builder.create();
    configurationDialog.show();
  }

  private String verifyHostOrFix(String hostStr) {
    Uri uri = Uri.parse(hostStr);
    if (uri.getScheme() == null) {
      return "https://" + hostStr;
    }
    return hostStr;
  }

  private void updateStatusMessage(final String message) {
    if (message != null) {
      statusMessage.setText(message);
    }
  }

  /**
   *
   */
  static class DoorsStateCallback extends AbstractDataCallback<ControllerFragment, DataService, DoorsState> {

    Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    public DoorsStateCallback(ControllerFragment fragment, Handler mainHandler) throws IllegalArgumentException {
      super(fragment, mainHandler);
    }

    @Override
    protected void onSuccess(ControllerFragment fragment, DataService service, DoorsState result) {
      if (result != null) {
        fragment.updateOnlineState();
        fragment.updateStatusMessage(result.getState());
      }
    }

    @Override
    protected void onFailure(ControllerFragment fragment, DataService service, Throwable t) {
      logger.warn("Requesting door state failed! {}", t.getMessage());
      fragment.updateOfflineState();
      fragment.updateStatusMessage(t.getMessage());
    }
  }


  NetworkHelper.NetworkStateListener networkListener = (NetworkInfo.State state) -> syncWithWebapp();
}
