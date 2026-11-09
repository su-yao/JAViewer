package io.github.javiewer.adapter.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Project: JAViewer
 */
public class DownloadLink extends Linkable {
    protected String title;
    protected String size;
    protected String date;
    protected MagnetLink magnetLink;
    protected List<TorrentFile> files = new ArrayList<>();
    protected boolean filesResolved;

    public static DownloadLink create(String title, String size, String date, String link, String magnetLink) {
        return create(title, size, date, link, magnetLink, null);
    }

    public static DownloadLink create(String title, String size, String date, String link, String magnetLink, List<TorrentFile> files) {
        DownloadLink download = new DownloadLink();
        download.title = title;
        download.size = size;
        download.date = date;
        download.link = link;
        download.magnetLink = MagnetLink.create(magnetLink);
        if (files != null && !files.isEmpty()) {
            download.files.addAll(files);
        } else {
            download.filesResolved = false;
        }
        return download;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDate() {
        return date;
    }

    public boolean hasMagnetLink() {
        return magnetLink != null && magnetLink.getMagnetLink() != null;
    }

    public String getMagnetLink() {
        return magnetLink == null ? null : magnetLink.getMagnetLink();
    }

    public void setMagnetLink(String magnet) {
        this.magnetLink = MagnetLink.create(magnet);
    }

    public List<TorrentFile> getFiles() {
        return files == null ? Collections.<TorrentFile>emptyList() : files;
    }

    public void setFiles(List<TorrentFile> files) {
        this.files = files == null ? new ArrayList<TorrentFile>() : new ArrayList<TorrentFile>(files);
        this.filesResolved = true;
    }

    public boolean isFilesResolved() {
        return filesResolved;
    }

    public void setFilesResolved(boolean filesResolved) {
        this.filesResolved = filesResolved;
    }
}
