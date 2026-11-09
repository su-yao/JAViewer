package io.github.javiewer.adapter.item;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MagnetLinkTest {

    @Test
    public void createNull() {
        assertNull(MagnetLink.create(null).getMagnetLink());
    }

    @Test
    public void createEmpty() {
        assertNull(MagnetLink.create("").getMagnetLink());
        assertNull(MagnetLink.create("   ").getMagnetLink());
    }

    @Test
    public void createWithoutAmp() {
        String magnet = "magnet:?xt=urn:btih:ABCDEF1234567890ABCDEF1234567890ABCDEF12";
        assertEquals(magnet, MagnetLink.create(magnet).getMagnetLink());
    }

    @Test
    public void createStripsTrackers() {
        String magnet = "magnet:?xt=urn:btih:ABCDEF1234567890ABCDEF1234567890ABCDEF12&dn=name&tr=udp://tracker";
        assertEquals("magnet:?xt=urn:btih:ABCDEF1234567890ABCDEF1234567890ABCDEF12",
                MagnetLink.create(magnet).getMagnetLink());
    }

    @Test
    public void createUnescapesAmp() {
        String magnet = "magnet:?xt=urn:btih:ABCDEF1234567890ABCDEF1234567890ABCDEF12&amp;dn=name";
        assertEquals("magnet:?xt=urn:btih:ABCDEF1234567890ABCDEF1234567890ABCDEF12",
                MagnetLink.create(magnet).getMagnetLink());
    }
}
