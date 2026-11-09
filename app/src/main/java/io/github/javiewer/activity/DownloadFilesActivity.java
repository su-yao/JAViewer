package io.github.javiewer.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.javiewer.R;
import io.github.javiewer.adapter.TorrentFileAdapter;
import io.github.javiewer.adapter.item.DownloadLink;
import io.github.javiewer.adapter.item.TorrentFile;
import io.github.javiewer.network.WhatsLink;
import io.github.javiewer.network.item.WhatsLinkResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadFilesActivity extends SecureActivity {

    public static final String EXTRA_LINK = "download_link";

    @BindView(R.id.download_files_toolbar)
    public Toolbar mToolbar;

    @BindView(R.id.download_files_magnet)
    public TextView mMagnet;

    @BindView(R.id.download_files_date)
    public TextView mDate;

    @BindView(R.id.download_files_size)
    public TextView mSize;

    @BindView(R.id.download_files_status)
    public TextView mStatus;

    @BindView(R.id.download_files_list)
    public RecyclerView mList;

    private DownloadLink link;
    private final List<TorrentFile> files = new ArrayList<>();
    private TorrentFileAdapter adapter;

    public static Intent newIntent(Context context, DownloadLink link) {
        Intent intent = new Intent(context, DownloadFilesActivity.class);
        intent.putExtra(EXTRA_LINK, link);
        return intent;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_files);
        ButterKnife.bind(this);

        link = (DownloadLink) getIntent().getSerializableExtra(EXTRA_LINK);
        if (link == null) {
            finish();
            return;
        }

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(empty(link.getTitle(), "磁力详情"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDate.setText(empty(link.getDate(), ""));
        mSize.setText(empty(link.getSize(), ""));
        bindMagnet();

        adapter = new TorrentFileAdapter(files);
        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.setAdapter(adapter);

        files.addAll(link.getFiles());
        adapter.notifyDataSetChanged();
        updateStatus();

        mMagnet.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyMagnet();
                return true;
            }
        });
        mMagnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMagnet();
            }
        });

        if (files.isEmpty() && link.hasMagnetLink()) {
            fetchFiles();
        }
    }

    private void bindMagnet() {
        if (link.hasMagnetLink()) {
            mMagnet.setText(link.getMagnetLink());
            mMagnet.setVisibility(View.VISIBLE);
        } else {
            mMagnet.setText("暂无磁力链接");
            mMagnet.setVisibility(View.VISIBLE);
        }
    }

    private void fetchFiles() {
        mStatus.setText("正在获取文件列表");
        mStatus.setVisibility(View.VISIBLE);
        WhatsLink.INSTANCE.preview(link.getMagnetLink()).enqueue(new Callback<WhatsLinkResult>() {
            @Override
            public void onResponse(Call<WhatsLinkResult> call, Response<WhatsLinkResult> response) {
                WhatsLinkResult body = response.body();
                if (body == null) {
                    mStatus.setText("文件列表获取失败");
                    return;
                }
                List<TorrentFile> remote = body.toFiles();
                if (!remote.isEmpty()) {
                    link.setFiles(remote);
                    files.clear();
                    files.addAll(remote);
                    adapter.notifyDataSetChanged();
                }
                if ((link.getTitle() == null || link.getTitle().trim().isEmpty()) && body.getName() != null) {
                    link.setTitle(body.getName());
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(body.getName());
                    }
                }
                if ((link.getSize() == null || link.getSize().trim().isEmpty()) && body.getSizeText() != null && !body.getSizeText().isEmpty()) {
                    link.setSize(body.getSizeText());
                    mSize.setText(body.getSizeText());
                }
                updateStatus();
            }

            @Override
            public void onFailure(Call<WhatsLinkResult> call, Throwable t) {
                mStatus.setText("文件列表获取失败");
                mStatus.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateStatus() {
        if (files.isEmpty()) {
            mStatus.setText("暂无文件列表");
            mStatus.setVisibility(View.VISIBLE);
        } else {
            mStatus.setText(files.size() + " 个条目");
            mStatus.setVisibility(View.VISIBLE);
        }
    }

    private void copyMagnet() {
        if (!link.hasMagnetLink()) {
            Toast.makeText(this, "暂无磁力链接", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clip != null) {
            clip.setPrimaryClip(ClipData.newPlainText("magnet-link", link.getMagnetLink()));
        }
        Toast.makeText(this, "已复制磁力链接", Toast.LENGTH_SHORT).show();
    }

    private void openMagnet() {
        if (!link.hasMagnetLink()) {
            Toast.makeText(this, "暂无磁力链接", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link.getMagnetLink()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static String empty(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
