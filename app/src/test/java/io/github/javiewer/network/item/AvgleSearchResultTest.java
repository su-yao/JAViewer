package io.github.javiewer.network.item;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class AvgleSearchResultTest {

    @Test
    public void firstVideoReturnsNullWhenMissing() {
        AvgleSearchResult empty = new AvgleSearchResult();
        assertNull(empty.firstVideo());

        empty.success = true;
        assertNull(empty.firstVideo());

        empty.response = new AvgleSearchResult.Response();
        assertNull(empty.firstVideo());

        empty.response.videos = new ArrayList<>();
        assertNull(empty.firstVideo());
    }

    @Test
    public void firstVideoReturnsFirstItem() {
        AvgleSearchResult.Response.Video video = new AvgleSearchResult.Response.Video();
        video.vid = "abc";
        AvgleSearchResult result = new AvgleSearchResult();
        result.success = true;
        result.response = new AvgleSearchResult.Response();
        result.response.videos = Collections.singletonList(video);
        assertNotNull(result.firstVideo());
        assertSame(video, result.firstVideo());
    }
}
