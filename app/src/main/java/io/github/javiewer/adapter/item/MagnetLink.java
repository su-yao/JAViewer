package io.github.javiewer.adapter.item;

import java.io.Serializable;

/**
 * Project: JAViewer
 */
public class MagnetLink implements Serializable {

    protected String magnetLink;

    public static MagnetLink create(String magnetLink) {
        MagnetLink magnet = new MagnetLink();
        if (magnetLink == null) {
            return magnet;
        }
        String trimmed = magnetLink.trim().replace("&amp;", "&");
        if (trimmed.isEmpty()) {
            return magnet;
        }
        int amp = trimmed.indexOf('&');
        magnet.magnetLink = amp >= 0 ? trimmed.substring(0, amp) : trimmed;
        return magnet;
    }

    public String getMagnetLink() {
        return magnetLink;
    }
}
