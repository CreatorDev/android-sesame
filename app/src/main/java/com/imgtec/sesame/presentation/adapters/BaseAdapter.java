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

package com.imgtec.sesame.presentation.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public abstract class BaseAdapter<V, K extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<K> {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BaseAdapter.class);

  protected List<V> data = new ArrayList<>();

  @Override
  public abstract K onCreateViewHolder(ViewGroup parent, int viewType);

  @Override
  public abstract void onBindViewHolder(K holder, int position);

  @Override
  public int getItemCount() {
    return data.size();
  }

  public void add(@NonNull V item) {

    if (!data.contains(item)) {
      data.add(item);
    }
  }

  public void addAll(@NonNull Collection<V> items) {
    for (V item : items) {
      add(item);
    }
  }

  public void addAllNonCopy(List<V> items) {
    this.data = items;
  }

  public int getPosition(V item) {
    return data.indexOf(item);
  }

  public V getItem(int position) {
    return data.get(position);
  }

  public List<V> getItems() {
    return new ArrayList<>(data);
  }

  public void remove(int position) {
    data.remove(position);
  }

  public void clear() {
    data.clear();
  }
}