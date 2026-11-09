package io.github.javiewer.fragment;


import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import io.github.javiewer.adapter.DownloadLinkAdapter;
import io.github.javiewer.adapter.item.DownloadLink;
import io.github.javiewer.network.provider.DownloadLinkProvider;
import io.github.javiewer.view.decoration.DownloadItemDecoration;
import io.github.javiewer.view.listener.BasicOnScrollListener;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class DownloadFragment extends RecyclerFragment<DownloadLink, LinearLayoutManager> {

    public DownloadLinkProvider provider;

    public String keyword;

    public DownloadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = new Bundle();
        }
        this.provider = DownloadLinkProvider.getProvider(bundle.getString("provider"));
        this.keyword = bundle.getString("keyword");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        this.setLayoutManager(new LinearLayoutManager(this.getContext()));
        this.setAdapter(new ScaleInAnimationAdapter(new DownloadLinkAdapter(this.getItems(), this.getActivity(), this.provider)));
        mRecyclerView.addItemDecoration(new DownloadItemDecoration());
        mRecyclerView.setClipToPadding(false);
        mRecyclerView.setPadding(
                mRecyclerView.getPaddingLeft(),
                mRecyclerView.getPaddingTop(),
                mRecyclerView.getPaddingRight(),
                io.github.javiewer.view.ViewUtil.dpToPx(88)
        );

        RecyclerView.ItemAnimator animator = new SlideInUpAnimator();
        animator.setAddDuration(300);
        mRecyclerView.setItemAnimator(animator);

        this.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getOnScrollListener() != null) {
                    getOnScrollListener().refresh();
                }
            }
        });

        this.addOnScrollListener(new BasicOnScrollListener<DownloadLink>() {
            @Override
            public Call<ResponseBody> newCall(int page) {
                return DownloadFragment.this.newCall(page);
            }

            @Override
            public RecyclerView.LayoutManager getLayoutManager() {
                return DownloadFragment.this.getLayoutManager();
            }

            @Override
            public SwipeRefreshLayout getRefreshLayout() {
                return DownloadFragment.this.mRefreshLayout;
            }

            @Override
            public RecyclerView.Adapter getAdapter() {
                return DownloadFragment.this.getAdapter();
            }

            @Override
            public List<DownloadLink> getItems() {
                return DownloadFragment.this.getItems();
            }

            @Override
            public void onResult(ResponseBody response) throws Exception {
                super.onResult(response);
                if (!isAdded()) {
                    return;
                }
                if (provider == null) {
                    setEnd(true);
                    toastSearchFailed();
                    return;
                }
                List<DownloadLink> downloads = provider.parseDownloadLinks(response.string());

                int pos = getItems().size();

                if (downloads.isEmpty()) {
                    setEnd(true);
                    if (pos == 0) {
                        toastSearchFailed();
                    }
                } else {
                    getItems().addAll(downloads);
                    RecyclerView.Adapter adapter = getAdapter();
                    if (adapter != null) {
                        adapter.notifyItemRangeInserted(pos, downloads.size());
                    }
                }
            }

            @Override
            public void onExceptionCaught(Throwable t) {
                if (isAdded() && getItems().isEmpty()) {
                    toastSearchFailed();
                }
            }
        });

        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || mRefreshLayout == null || getOnRefreshListener() == null) {
                    return;
                }
                mRefreshLayout.setRefreshing(true);
                getOnRefreshListener().onRefresh();
            }
        });

        super.onActivityCreated(savedInstanceState);
    }

    public Call<ResponseBody> newCall(int page) {
        if (this.provider == null) {
            return null;
        }
        return this.provider.search(this.keyword, page);
    }

    private void toastSearchFailed() {
        if (!isAdded() || getActivity() == null) {
            return;
        }
        Toast.makeText(getActivity(), "搜索失败，可切换其他站点重试", Toast.LENGTH_SHORT).show();
    }
}
