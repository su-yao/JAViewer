package io.github.javiewer.adapter.item;

import java.io.Serializable;

public class TorrentFile implements Serializable {

    protected String name;
    protected String size;
    protected String path;
    protected boolean directory;

    public static TorrentFile create(String name, String size) {
        String raw = name == null ? "" : name.trim().replace('\\', '/');
        boolean directory = raw.endsWith("/");
        if (directory && raw.length() > 1) {
            raw = raw.substring(0, raw.length() - 1);
        }
        String display = raw;
        int slash = raw.lastIndexOf('/');
        if (slash >= 0 && slash < raw.length() - 1) {
            display = raw.substring(slash + 1);
        }
        return create(display, size, raw, directory);
    }

    public static TorrentFile create(String name, String size, String path, boolean directory) {
        TorrentFile file = new TorrentFile();
        file.name = name == null ? "" : name;
        file.size = size == null ? "" : size;
        file.path = path == null || path.trim().isEmpty() ? file.name : path.replace('\\', '/');
        file.directory = directory;
        return file;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public String getPath() {
        return path == null || path.trim().isEmpty() ? name : path;
    }

    public boolean isDirectory() {
        return directory;
    }

    public int getDepth() {
        String value = getPath();
        if (value == null || value.isEmpty()) {
            return 0;
        }
        int depth = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '/' || c == '\\') {
                depth++;
            }
        }
        if (value.endsWith("/") || value.endsWith("\\")) {
            depth = Math.max(0, depth - 1);
        }
        return depth;
    }
}
