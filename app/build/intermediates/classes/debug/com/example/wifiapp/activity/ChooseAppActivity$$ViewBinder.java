// Generated code from Butter Knife. Do not modify!
package com.example.wifiapp.activity;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class ChooseAppActivity$$ViewBinder<T extends com.example.wifiapp.activity.ChooseAppActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296258, "field 'mListView'");
    target.mListView = finder.castView(view, 2131296258, "field 'mListView'");
  }

  @Override public void unbind(T target) {
    target.mListView = null;
  }
}
