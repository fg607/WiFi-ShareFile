// Generated code from Butter Knife. Do not modify!
package com.example.wifiapp.fragment;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class SendFragment$$ViewBinder<T extends com.example.wifiapp.fragment.SendFragment> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296279, "field 'mFileNameTextView'");
    target.mFileNameTextView = finder.castView(view, 2131296279, "field 'mFileNameTextView'");
    view = finder.findRequiredView(source, 2131296269, "field 'mSocketStateTextView'");
    target.mSocketStateTextView = finder.castView(view, 2131296269, "field 'mSocketStateTextView'");
    view = finder.findRequiredView(source, 2131296273, "field 'mTranslateStateTextView'");
    target.mTranslateStateTextView = finder.castView(view, 2131296273, "field 'mTranslateStateTextView'");
    view = finder.findRequiredView(source, 2131296283, "field 'mAppNameTextView'");
    target.mAppNameTextView = finder.castView(view, 2131296283, "field 'mAppNameTextView'");
    view = finder.findRequiredView(source, 2131296281, "field 'mAppChooseRelativeLayout'");
    target.mAppChooseRelativeLayout = finder.castView(view, 2131296281, "field 'mAppChooseRelativeLayout'");
    view = finder.findRequiredView(source, 2131296280, "field 'mFolderImg'");
    target.mFolderImg = finder.castView(view, 2131296280, "field 'mFolderImg'");
    view = finder.findRequiredView(source, 2131296274, "field 'mProgressBar'");
    target.mProgressBar = finder.castView(view, 2131296274, "field 'mProgressBar'");
    view = finder.findRequiredView(source, 2131296285, "field 'mSendButton'");
    target.mSendButton = finder.castView(view, 2131296285, "field 'mSendButton'");
    view = finder.findRequiredView(source, 2131296284, "field 'mRefreshButton'");
    target.mRefreshButton = finder.castView(view, 2131296284, "field 'mRefreshButton'");
  }

  @Override public void unbind(T target) {
    target.mFileNameTextView = null;
    target.mSocketStateTextView = null;
    target.mTranslateStateTextView = null;
    target.mAppNameTextView = null;
    target.mAppChooseRelativeLayout = null;
    target.mFolderImg = null;
    target.mProgressBar = null;
    target.mSendButton = null;
    target.mRefreshButton = null;
  }
}
