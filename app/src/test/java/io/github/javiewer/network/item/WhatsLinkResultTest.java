package io.github.javiewer.network.item;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.List;

import io.github.javiewer.adapter.item.TorrentFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WhatsLinkResultTest {

    @Test
    public void parseFilesAndHumanSize() {
        String json = "{"
                + "\"name\":\"DFE-118\","
                + "\"size\":1073741824,"
                + "\"files\":["
                + "{\"name\":\"DFE-118.mp4\",\"size\":1048576},"
                + "{\"filename\":\"cover.jpg\",\"file_size\":\"2048\"}"
                + "]"
                + "}";
        WhatsLinkResult result = new Gson().fromJson(json, WhatsLinkResult.class);
        assertEquals("DFE-118", result.getName());
        assertEquals("1.00 GB", result.getSizeText());
        List<TorrentFile> files = result.toFiles();
        assertEquals(2, files.size());
        assertEquals("DFE-118.mp4", files.get(0).getName());
        assertEquals("1.00 MB", files.get(0).getSize());
        assertEquals("cover.jpg", files.get(1).getName());
        assertEquals("2.00 KB", files.get(1).getSize());
    }

    @Test
    public void parseFileListFallback() {
        String json = "{\"file_list\":[{\"n\":\"a.txt\",\"s\":100}]}";
        WhatsLinkResult result = new Gson().fromJson(json, WhatsLinkResult.class);
        List<TorrentFile> files = result.toFiles();
        assertEquals(1, files.size());
        assertEquals("a.txt", files.get(0).getName());
        assertEquals("100 B", files.get(0).getSize());
    }

    @Test
    public void emptyFiles() {
        WhatsLinkResult result = new Gson().fromJson("{}", WhatsLinkResult.class);
        assertTrue(result.toFiles().isEmpty());
        assertEquals("", result.getSizeText());
    }

    @Test
    public void nestedFolderFiles() {
        String json = "{"
                + "\"files\":[{"
                + "\"name\":\"folder\","
                + "\"type\":\"directory\","
                + "\"files\":[{\"name\":\"a.mp4\",\"size\":2048}]"
                + "}]"
                + "}";
        List<TorrentFile> files = new Gson().fromJson(json, WhatsLinkResult.class).toFiles();
        assertEquals(2, files.size());
        assertTrue(files.get(0).isDirectory());
        assertEquals("folder", files.get(0).getName());
        assertEquals("folder", files.get(0).getPath());
        assertEquals("a.mp4", files.get(1).getName());
        assertEquals("folder/a.mp4", files.get(1).getPath());
        assertEquals("2.00 KB", files.get(1).getSize());
        assertEquals(1, files.get(1).getDepth());
    }
}
