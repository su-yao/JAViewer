package io.github.javiewer.network.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.javiewer.adapter.item.TorrentFile;

public class WhatsLinkResult {

    public String error;
    public String name;
    public String type;
    public String file_type;
    public JsonElement size;
    public JsonElement count;
    public JsonArray files;
    public JsonArray file_list;

    public String getName() {
        return name;
    }

    public String getSizeText() {
        return formatSize(size);
    }

    public List<TorrentFile> toFiles() {
        List<TorrentFile> out = new ArrayList<>();
        appendFiles(out, files, "");
        if (out.isEmpty()) {
            appendFiles(out, file_list, "");
        }
        return out;
    }

    private static void appendFiles(List<TorrentFile> out, JsonArray array, String parent) {
        if (array == null) {
            return;
        }
        for (JsonElement element : array) {
            if (element == null || !element.isJsonObject()) {
                continue;
            }
            JsonObject obj = element.getAsJsonObject();
            String fileName = firstString(obj, "name", "file_name", "filename", "n", "path");
            if (fileName == null || fileName.trim().isEmpty()) {
                continue;
            }
            fileName = fileName.trim().replace('\\', '/');
            JsonArray nested = firstArray(obj, "files", "file_list", "children");
            String kind = firstString(obj, "type", "file_type", "kind");
            boolean directory = nested != null
                    || "folder".equalsIgnoreCase(kind)
                    || "directory".equalsIgnoreCase(kind)
                    || "dir".equalsIgnoreCase(kind);
            String path = parent == null || parent.isEmpty() ? fileName : parent + "/" + fileName;
            String fileSize = formatSize(firstElement(obj, "size", "file_size", "s", "length"));
            out.add(TorrentFile.create(fileName, fileSize, path, directory));
            if (nested != null) {
                appendFiles(out, nested, path);
            }
        }
    }

    private static JsonArray firstArray(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && obj.get(key).isJsonArray()) {
                return obj.getAsJsonArray(key);
            }
        }
        return null;
    }

    private static JsonElement firstElement(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key);
            }
        }
        return null;
    }

    private static String firstString(JsonObject obj, String... keys) {
        JsonElement element = firstElement(obj, keys);
        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }
        return element.getAsString();
    }

    public static String formatSize(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return "";
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return humanSize(element.getAsLong());
        }
        String text = element.getAsString();
        if (text == null) {
            return "";
        }
        text = text.trim();
        if (text.matches("^\\d+$")) {
            try {
                return humanSize(Long.parseLong(text));
            } catch (NumberFormatException ignored) {
                return text;
            }
        }
        return text;
    }

    public static String humanSize(long bytes) {
        if (bytes < 0) {
            bytes = 0;
        }
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        double value = bytes;
        int unit = 0;
        while (value >= 1024 && unit < units.length - 1) {
            value /= 1024d;
            unit++;
        }
        if (unit == 0) {
            return ((long) value) + " B";
        }
        return String.format(Locale.US, "%.2f %s", value, units[unit]);
    }
}
