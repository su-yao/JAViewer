package io.github.javiewer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.javiewer.R;
import io.github.javiewer.adapter.item.TorrentFile;
import io.github.javiewer.view.ViewUtil;

public class TorrentFileAdapter extends ItemAdapter<TorrentFile, TorrentFileAdapter.ViewHolder> {

    public TorrentFileAdapter(List<TorrentFile> files) {
        super(files);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_torrent_file, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getItems().get(position));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.torrent_file_name)
        public TextView mName;

        @BindView(R.id.torrent_file_size)
        public TextView mSize;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bind(TorrentFile file) {
            int indent = ViewUtil.dpToPx(12 * Math.min(file.getDepth(), 8));
            itemView.setPadding(indent, itemView.getPaddingTop(), itemView.getPaddingRight(), itemView.getPaddingBottom());
            String name = file.getName();
            if (name == null || name.trim().isEmpty()) {
                name = file.getPath();
            }
            if (file.isDirectory()) {
                mName.setText("[目录] " + name);
                mSize.setText("");
            } else {
                mName.setText(name);
                String size = file.getSize();
                mSize.setText(size == null ? "" : size);
            }
        }
    }
}
