package io.github.javiewer.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;

import io.github.javiewer.network.SiteApi;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Project: JAViewer
 */
public class MovieListFragment extends MovieFragment {

    public String link;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        this.link = bundle == null ? null : bundle.getString("link");
    }

    @Override
    public Call<ResponseBody> newCall(int page) {
        return SiteApi.fromLink(this.link, page);
    }
}
