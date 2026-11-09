package io.github.javiewer.adapter.item;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DownloadLinkMagnetTest {

    @Test
    public void setMagnetLinkAfterCreate() {
        DownloadLink link = DownloadLink.create("t", "1MB", "2026-01-01", "http://example.com", null);
        assertFalse(link.hasMagnetLink());
        link.setMagnetLink("magnet:?xt=urn:btih:ABCDEF1234567890ABCDEF1234567890ABCDEF12&dn=x");
        assertTrue(link.hasMagnetLink());
        assertEquals("magnet:?xt=urn:btih:ABCDEF1234567890ABCDEF1234567890ABCDEF12", link.getMagnetLink());
    }
}
