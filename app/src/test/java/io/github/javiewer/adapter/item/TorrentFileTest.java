package io.github.javiewer.adapter.item;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TorrentFileTest {

    @Test
    public void createSplitsPath() {
        TorrentFile file = TorrentFile.create("folder/sub/a.mp4", "1.00 MB");
        assertEquals("a.mp4", file.getName());
        assertEquals("folder/sub/a.mp4", file.getPath());
        assertEquals("1.00 MB", file.getSize());
        assertFalse(file.isDirectory());
        assertEquals(2, file.getDepth());
    }

    @Test
    public void createDirectoryFromTrailingSlash() {
        TorrentFile dir = TorrentFile.create("folder/sub/", "");
        assertEquals("sub", dir.getName());
        assertEquals("folder/sub", dir.getPath());
        assertTrue(dir.isDirectory());
        assertEquals(1, dir.getDepth());
    }
}
