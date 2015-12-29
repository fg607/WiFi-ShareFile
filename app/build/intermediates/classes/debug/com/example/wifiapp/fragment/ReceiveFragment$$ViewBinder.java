// Generated code from Butter Knife. Do not modify!
package com.example.wifiapp.fragment;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class ReceiveFragment$$ViewBinder<T extends com.example.wifiapp.fragment.ReceiveFragment> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296269, "field 'mServiceStateTextView'");
    target.mServiceStateTextView = finder.castView(view, 2131296269, "field 'mServiceStateTextView'");
    view = finder.findRequiredView(source, 2131296273, "field 'mTranslateStateTextView'");
    target.mTranslateStateTextView = finder.castView(view, 2131296273, "field 'mTranslateStateTextView'");
    view = finder.findRequiredView(source, 2131296271, "field 'mRetryButton'");
    target.mRetryButton = finder.castView(view, 2131296271, "field 'mRetryButton'");
    view = finder.findRequiredView(source, 2131296274, "field 'mProgressBar'");
    target.mProgressBar = finder.castView(view, 2131296274, "field 'mProgressBar'");
  }

  @Override public void unbind(T target) {
    target.mServiceStateTextView = null;
    target.mTranslateStateTextView = null;
    target.mRetryButton = null;
    target.mProgressBar = null;
  }
}
