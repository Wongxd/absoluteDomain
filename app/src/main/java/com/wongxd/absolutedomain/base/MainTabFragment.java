package com.wongxd.absolutedomain.base;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.wongxd.absolutedomain.R;


public abstract class MainTabFragment extends BaseLazyFragment {

    private ViewStub viewStub;
    private View emptyView;
    private View mView;
    private boolean viewPrepared;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fgt_main_tab, null);
        viewStub = (ViewStub) root.findViewById(R.id.viewStub);
        emptyView = root.findViewById(R.id.tv_empty);
        return root;
    }

    @Override
    public void onFirstUserVisible() {
        super.onFirstUserVisible();
        if (getLayout() <= 0) {
            throw new Resources.NotFoundException("layout not found");
        }
        if (mView == null) {
            viewStub.setLayoutResource(getLayout());
            emptyView.setVisibility(View.GONE);
            try {
                mView = viewStub.inflate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mView == null) {
            return;
        }
        initView(mView);
        viewPrepared = true;
    }

    public boolean isViewPrepared() {
        return getRootView() != null && viewPrepared;
    }

    /**
     * 初始化View
     *
     * @param mView
     */
    protected void initView(View mView) {

    }

    public View getRootView() {
        return mView;
    }

    protected abstract int getLayout();
}