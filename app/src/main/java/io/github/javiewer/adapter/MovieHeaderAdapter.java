package io.github.javiewer.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.javiewer.R;
import io.github.javiewer.activity.MovieListActivity;
import io.github.javiewer.adapter.item.MovieDetail;
import io.github.javiewer.view.ViewUtil;

/**
 * Project: JAViewer
 */

public class MovieHeaderAdapter extends RecyclerView.Adapter<MovieHeaderAdapter.ViewHolder> {

    public boolean first = true;
    private List<MovieDetail.Header> headers;
    private Activity mParentActivity;
    private ImageView mIcon;

    public MovieHeaderAdapter(List<MovieDetail.Header> headers, Activity mParentActivity, ImageView mIcon) {
        this.headers = headers;
        this.mParentActivity = mParentActivity;
        this.mIcon = mIcon;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_header, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final MovieDetail.Header header = headers.get(position);
        holder.itemView.setOnLongClickListener(null);
        holder.itemView.setOnTouchListener(null);
        holder.mHeaderName.setOnLongClickListener(null);
        holder.mHeaderName.setOnTouchListener(null);
        holder.mHeaderValue.setOnLongClickListener(null);
        holder.mHeaderValue.setOnTouchListener(null);
        holder.mHeaderValue.setOnClickListener(null);
        holder.mHeaderValue.setClickable(false);
        holder.mHeaderValue.setPaintFlags(holder.mHeaderValue.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        holder.mHeaderValue.setTextColor(holder.defaultValueColor);

        if (header.name != null && header.value != null) {
            View.OnLongClickListener copyListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    copyHeader(header);
                    return true;
                }
            };
            View.OnTouchListener keepPress = new KeepLongPressTouchListener();
            holder.itemView.setOnLongClickListener(copyListener);
            holder.mHeaderName.setLongClickable(true);
            holder.mHeaderValue.setLongClickable(true);
            holder.mHeaderName.setOnLongClickListener(copyListener);
            holder.mHeaderValue.setOnLongClickListener(copyListener);
            holder.itemView.setOnTouchListener(keepPress);
            holder.mHeaderName.setOnTouchListener(keepPress);
            holder.mHeaderValue.setOnTouchListener(keepPress);

            holder.mHeaderName.setText(header.name);
            holder.mHeaderValue.setText(header.value);

            if (header.getLink() != null) {
                holder.mHeaderValue.setPaintFlags(holder.mHeaderValue.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                holder.mHeaderValue.setTextColor(ResourcesCompat.getColor(this.mParentActivity.getResources(), R.color.colorAccent, null));
                holder.mHeaderValue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mParentActivity, MovieListActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("title", header.getName() + " " + header.getValue());
                        bundle.putString("link", header.getLink());
                        intent.putExtras(bundle);
                        mParentActivity.startActivity(intent);
                    }
                });
            }

            if (first) {
                ViewUtil.alignIconToView(mIcon, holder.mHeaderName);
                first = false;
            }
        }
    }

    @Override
    public int getItemCount() {
        return headers == null ? 0 : headers.size();
    }

    private void copyHeader(MovieDetail.Header header) {
        String value = header.value == null ? "" : header.value;
        ClipboardManager clip = (ClipboardManager) mParentActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clip != null) {
            clip.setPrimaryClip(ClipData.newPlainText(header.name, value));
        }
        Toast.makeText(mParentActivity, "已复制" + (header.name == null ? "" : header.name), Toast.LENGTH_SHORT).show();
    }

    private static class KeepLongPressTouchListener implements View.OnTouchListener {
        private float downX;
        private float downY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                    requestDisallowIntercept(v, true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dy = Math.abs(event.getY() - downY);
                    float dx = Math.abs(event.getX() - downX);
                    int slop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
                    if (dy > slop && dy > dx) {
                        requestDisallowIntercept(v, false);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    requestDisallowIntercept(v, false);
                    break;
                default:
                    break;
            }
            return false;
        }

        private static void requestDisallowIntercept(View v, boolean disallow) {
            ViewParent parent = v.getParent();
            while (parent != null) {
                parent.requestDisallowInterceptTouchEvent(disallow);
                parent = parent.getParent();
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.header_name)
        public TextView mHeaderName;

        @BindView(R.id.header_value)
        public TextView mHeaderValue;

        public final int defaultValueColor;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            defaultValueColor = mHeaderValue.getCurrentTextColor();
        }
    }
}
