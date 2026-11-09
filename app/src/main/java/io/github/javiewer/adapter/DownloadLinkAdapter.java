package io.github.javiewer.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.javiewer.R;
import io.github.javiewer.adapter.item.DownloadLink;
import io.github.javiewer.adapter.item.MagnetLink;
import io.github.javiewer.adapter.item.TorrentFile;
import io.github.javiewer.network.WhatsLink;
import io.github.javiewer.network.item.WhatsLinkResult;
import io.github.javiewer.network.provider.DownloadLinkProvider;
import io.github.javiewer.util.Intents;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadLinkAdapter extends ItemAdapter<DownloadLink, DownloadLinkAdapter.ViewHolder> {

    private Activity mParentActivity;

    private DownloadLinkProvider provider;

    public DownloadLinkAdapter(List<DownloadLink> links, Activity mParentActivity, DownloadLinkProvider provider) {
        super(links);
        this.mParentActivity = mParentActivity;
        this.provider = provider;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_download, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final DownloadLink link = getItems().get(position);

        holder.parse(link);
        holder.mTextMagnet.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String magnet = link.hasMagnetLink() ? link.getMagnetLink() : "";
                if (magnet.isEmpty()) {
                    toast("暂无磁力链接");
                    return true;
                }
                copyMagnet(magnet);
                return true;
            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (link.hasMagnetLink()) {
                    onMagnetGet(link, link.getMagnetLink());
                    return;
                }
                if (provider == null) {
                    toast("磁力链接获取失败");
                    return;
                }
                if (!Intents.isAlive(mParentActivity)) {
                    return;
                }
                final ProgressDialog mDialog = new ProgressDialog(mParentActivity);
                mDialog.setTitle("请稍后");
                mDialog.setMessage("正在获取磁力链接");
                mDialog.setIndeterminate(true);
                mDialog.setCancelable(true);
                mDialog.show();

                Call<ResponseBody> call = provider.get(link.getLink());
                if (call == null) {
                    dismissDialog(mDialog);
                    toast("磁力链接获取失败");
                    return;
                }
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.body() == null) {
                                onFailure(call, new IOException("empty magnet response"));
                                return;
                            }
                            MagnetLink magnetLink = provider.parseMagnetLink(response.body().string());
                            String magnet = magnetLink.getMagnetLink();
                            if (magnet == null || magnet.isEmpty()) {
                                toast("磁力链接获取失败");
                                return;
                            }
                            link.setMagnetLink(magnet);
                            onMagnetGet(link, magnet);
                        } catch (Throwable e) {
                            onFailure(call, e);
                        } finally {
                            dismissDialog(mDialog);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        dismissDialog(mDialog);
                        toast("磁力链接获取失败");
                    }
                });
            }
        });

        if (link.hasMagnetLink() && !link.isFilesResolved()) {
            fetchFiles(link, holder.getAdapterPosition());
        }
    }

    private void fetchFiles(final DownloadLink link, final int position) {
        link.setFilesResolved(true);
        Call<WhatsLinkResult> call = WhatsLink.INSTANCE.preview(link.getMagnetLink());
        call.enqueue(new Callback<WhatsLinkResult>() {
            @Override
            public void onResponse(Call<WhatsLinkResult> call, Response<WhatsLinkResult> response) {
                if (!Intents.isAlive(mParentActivity)) {
                    return;
                }
                WhatsLinkResult body = response.body();
                if (body == null) {
                    return;
                }
                List<TorrentFile> files = body.toFiles();
                if (!files.isEmpty()) {
                    link.setFiles(files);
                }
                if ((link.getTitle() == null || link.getTitle().trim().isEmpty()) && body.getName() != null) {
                    link.setTitle(body.getName());
                }
                if ((link.getSize() == null || link.getSize().trim().isEmpty()) && body.getSizeText() != null && !body.getSizeText().isEmpty()) {
                    link.setSize(body.getSizeText());
                }
                if (position >= 0 && position < getItems().size() && getItems().get(position) == link) {
                    notifyItemChanged(position);
                }
            }

            @Override
            public void onFailure(Call<WhatsLinkResult> call, Throwable t) {
            }
        });
    }

    public void onMagnetGet(DownloadLink link, final String magnetLink) {
        if (!Intents.isAlive(mParentActivity)) {
            return;
        }
        if (magnetLink != null && !magnetLink.isEmpty()) {
            new AlertDialog.Builder(mParentActivity)
                    .setTitle(link == null || link.getTitle() == null || link.getTitle().isEmpty() ? "磁力链接" : link.getTitle())
                    .setMessage(magnetLink)
                    .setNeutralButton("复制到剪贴板", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            copyMagnet(magnetLink);
                        }
                    })
                    .setPositiveButton("打开", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intents.openUri(mParentActivity, magnetLink);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            toast("磁力链接获取失败");
        }
    }

    private void copyMagnet(String magnetLink) {
        if (!Intents.isAlive(mParentActivity)) {
            return;
        }
        ClipboardManager clip = (ClipboardManager) mParentActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clip != null) {
            clip.setPrimaryClip(ClipData.newPlainText("magnet-link", magnetLink));
        }
        toast("已复制磁力链接");
    }

    private void toast(String message) {
        if (Intents.isAlive(mParentActivity)) {
            Toast.makeText(mParentActivity, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void dismissDialog(ProgressDialog dialog) {
        if (dialog != null && dialog.isShowing() && Intents.isAlive(mParentActivity)) {
            dialog.dismiss();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.download_title)
        public TextView mTextTitle;

        @BindView(R.id.download_magnet)
        public TextView mTextMagnet;

        @BindView(R.id.download_size)
        public TextView mTextSize;

        @BindView(R.id.download_date)
        public TextView mTextDate;

        @BindView(R.id.download_files)
        public LinearLayout mFiles;

        @BindView(R.id.layout_download)
        public View mView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void parse(DownloadLink link) {
            mTextTitle.setText(empty(link.getTitle(), "未命名磁力"));
            mTextSize.setText(empty(link.getSize(), ""));
            mTextDate.setText(empty(link.getDate(), ""));
            if (link.hasMagnetLink()) {
                mTextMagnet.setText(link.getMagnetLink());
            } else {
                mTextMagnet.setText("点击获取磁力链接");
            }
            mTextMagnet.setVisibility(View.VISIBLE);
            bindFiles(link.getFiles());
        }

        private void bindFiles(List<TorrentFile> files) {
            mFiles.removeAllViews();
            if (files == null || files.isEmpty()) {
                mFiles.setVisibility(View.GONE);
                return;
            }
            mFiles.setVisibility(View.VISIBLE);
            Context context = mFiles.getContext();
            int max = Math.min(files.size(), 8);
            for (int i = 0; i < max; i++) {
                TorrentFile file = files.get(i);
                TextView row = new TextView(context);
                row.setTextSize(12);
                String prefix = file.isDirectory() ? "[目录] " : "";
                String size = empty(file.getSize(), "");
                if (size.isEmpty()) {
                    row.setText(prefix + empty(file.getName(), file.getPath()));
                } else {
                    row.setText(prefix + empty(file.getName(), file.getPath()) + "  " + size);
                }
                row.setPadding(0, 2, 0, 2);
                mFiles.addView(row);
            }
            if (files.size() > max) {
                TextView more = new TextView(context);
                more.setTextSize(12);
                more.setText("另有 " + (files.size() - max) + " 个文件");
                more.setPadding(0, 2, 0, 2);
                mFiles.addView(more);
            }
        }

        private static String empty(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value;
        }
    }
}
