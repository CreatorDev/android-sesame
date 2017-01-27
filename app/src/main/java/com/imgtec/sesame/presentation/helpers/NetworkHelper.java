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

package com.imgtec.sesame.presentation.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 *
 */
public class NetworkHelper {

  public interface NetworkStateListener {

    void onNetworkStateChanged(NetworkInfo.State state);
  }

  final Logger logger = LoggerFactory.getLogger(getClass());
  final Context context;
  final Set<NetworkStateListener> listeners = new CopyOnWriteArraySet<>();

  public NetworkHelper(Context context) {
    super();
    this.context = context;
    registerNetworkStateReceiver(networkReceiver);
  }

  public boolean isOnline() {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();
    return (netInfo != null && netInfo.isConnected()); //null in air plain mode
  }

  public void addNetworkStateListener(final NetworkStateListener l) {
    listeners.add(l);
  }

  public void removeNetworkStateListener(final NetworkStateListener l) {
    listeners.remove(l);
  }

  final void registerNetworkStateReceiver(BroadcastReceiver receiver) {
    final IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    context.registerReceiver(receiver, intentFilter);
  }

  final void unregisterNetworkStateReceiver(BroadcastReceiver receiver) {
    context.unregisterReceiver(receiver);
  }

  BroadcastReceiver networkReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {

      final NetworkInfo info = intent.getExtras().getParcelable("networkInfo");
      for (NetworkStateListener l: listeners) {
        l.onNetworkStateChanged(info.getState());
      }
    }
  };
}
