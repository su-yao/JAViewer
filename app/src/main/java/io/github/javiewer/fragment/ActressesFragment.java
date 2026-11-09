package io.github.javiewer.fragment;


import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.javiewer.adapter.ActressAdapter;
import io.github.javiewer.adapter.item.Actress;
import io.github.javiewer.network.SiteApi;
import io.github.javiewer.network.provider.AVMOProvider;
import io.github.javiewer.view.decoration.ActressItemDecoration;
import io.github.javiewer.view.listener.EndlessOnScrollListener;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class ActressesFragment extends RecyclerFragment<Actress, LinearLayoutManager> {

    public ActressesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        this.setLayoutManager(new LinearLayoutManager(this.getContext()));
        //this.setAdapter(new SlideInBottomAnimationAdapter(new ActressAdapter(getItems(), this.getActivity())));
        this.setAdapter(new ActressAdapter(getItems(), this.getActivity()));

        mRecyclerView.addItemDecoration(new ActressItemDecoration());

        /*RecyclerView.ItemAnimator animator = new SlideInUpAnimator();
        animator.setAddDuration(300);
        mRecyclerView.setItemAnimator(animator);*/

        this.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getOnScrollListener() != null) {
                    getOnScrollListener().refresh();
                }
            }
        });

        this.addOnScrollListener(new EndlessOnScrollListener<Actress>() {
            @Override
            public Call<ResponseBody> newCall(int page) {
                return ActressesFragment.this.newCall(page);
            }

            @Override
            public RecyclerView.LayoutManager getLayoutManager() {
                return ActressesFragment.this.getLayoutManager();
            }

            @Override
            public SwipeRefreshLayout getRefreshLayout() {
                return ActressesFragment.this.mRefreshLayout;
            }

            @Override
            public RecyclerView.Adapter getAdapter() {
                return ActressesFragment.this.getAdapter();
            }

            @Override
            public List<Actress> getItems() {
                return ActressesFragment.this.getItems();
            }

            @Override
            public void onResult(ResponseBody response) throws Exception {
                super.onResult(response);
                if (!isAdded()) {
                    return;
                }
                List<Actress> wrappers = AVMOProvider.parseActresses(response.string());

                if (wrappers.isEmpty()) {
                    setEnd(true);
                    return;
                }

                int pos = getItems().size();
                getItems().addAll(wrappers);
                RecyclerView.Adapter adapter = getAdapter();
                if (adapter != null) {
                    adapter.notifyItemRangeInserted(pos, wrappers.size());
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
        return SiteApi.actresses(page);
    }
}
