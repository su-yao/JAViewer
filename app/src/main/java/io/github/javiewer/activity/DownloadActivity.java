package io.github.javiewer.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.javiewer.R;
import io.github.javiewer.adapter.ViewPagerAdapter;
import io.github.javiewer.fragment.DownloadFragment;
import io.github.javiewer.network.BtSearchSite;
import io.github.javiewer.util.Intents;

public class DownloadActivity extends SecureActivity {

    @BindView(R.id.download_toolbar)
    public Toolbar mToolbar;

    @BindView(R.id.download_tabs)
    public TabLayout mTabLayout;

    @BindView(R.id.download_view_pager)
    public ViewPager mViewPager;

    @BindView(R.id.download_fab_browser)
    public FloatingActionButton mFabBrowser;

    public String keyword;

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        ButterKnife.bind(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null) {
            bundle = new Bundle();
        }
        this.keyword = bundle.getString("keyword");
        if (this.keyword == null) {
            this.keyword = "";
        }

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(this.keyword);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        for (BtSearchSite site : BtSearchSite.TABS) {
            Fragment fragment = new DownloadFragment();
            Bundle tabBundle = (Bundle) bundle.clone();
            tabBundle.putString("provider", site.id);
            fragment.setArguments(tabBundle);
            adapter.addFragment(fragment, site.name);
        }

        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(1);

        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mTabLayout.setupWithViewPager(mViewPager);

        mFabBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCurrentSiteInBrowser();
            }
        });
    }

    private void openCurrentSiteInBrowser() {
        int index = mViewPager.getCurrentItem();
        if (index < 0 || index >= BtSearchSite.TABS.size()) {
            Toast.makeText(this, "当前站点不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        BtSearchSite site = BtSearchSite.TABS.get(index);
        String url = site.buildSearchUrl(keyword, 1);
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, "无法打开搜索站点", Toast.LENGTH_SHORT).show();
            return;
        }
        Intents.openUri(this, url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
