package io.github.javiewer.network.provider;

import org.junit.Test;

import java.util.List;

import io.github.javiewer.adapter.item.DownloadLink;
import io.github.javiewer.adapter.item.MagnetLink;
import io.github.javiewer.network.BtSearchSite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BtSearchLinkProviderTest {

    @Test
    public void factoryUsesBtSites() {
        DownloadLinkProvider provider = DownloadLinkProvider.getProvider("sukebei");
        assertTrue(provider instanceof BtSearchLinkProvider);
        assertEquals("sukebei", ((BtSearchLinkProvider) provider).getSite().id);
        assertNotNull(DownloadLinkProvider.getProvider("ciligou"));
        assertNotNull(DownloadLinkProvider.getProvider("torrentkitty"));
        DownloadLinkProvider btdig = DownloadLinkProvider.getProvider("btdig");
        assertTrue(btdig instanceof BtSearchLinkProvider);
        assertEquals("btdig", ((BtSearchLinkProvider) btdig).getSite().id);
        assertFalse(BtSearchSite.TABS.contains(BtSearchSite.BTDIG));
        assertEquals("sukebei", BtSearchSite.TABS.get(0).id);
    }

    @Test
    public void parseNyaaHtml() {
        String html = "<table><tr class=\"default\">"
                + "<td></td>"
                + "<td colspan=\"2\"><a>DFE-118 sample</a></td>"
                + "<td><a href=\"magnet:?xt=urn:btih:ABCDEF1234567890ABCDEF1234567890ABCDEF12\">magnet</a></td>"
                + "<td>1.5 GiB</td>"
                + "<td>2026-11-01</td>"
                + "<td>12</td>"
                + "</tr></table>";
        List<DownloadLink> links = BtSearchLinkProvider.parseNyaa(org.jsoup.Jsoup.parse(html));
        assertEquals(1, links.size());
        assertEquals("DFE-118 sample", links.get(0).getTitle());
        assertEquals("1.5 GiB", links.get(0).getSize());
        assertEquals("2026-11-01", links.get(0).getDate());
        assertTrue(links.get(0).hasMagnetLink());
        assertEquals("magnet:?xt=urn:btih:ABCDEF1234567890ABCDEF1234567890ABCDEF12", links.get(0).getMagnetLink());
    }

    @Test
    public void parseTorrentKittyHtml() {
        String html = "<table><tr>"
                + "<td class=\"name\">ABC-123.mp4</td>"
                + "<td class=\"size\">700 MB</td>"
                + "<td class=\"date\">2026-01-02</td>"
                + "<td><a href=\"magnet:?xt=urn:btih:1111111111111111111111111111111111111111\">m</a></td>"
                + "</tr></table>";
        List<DownloadLink> links = BtSearchLinkProvider.parseTorrentKitty(org.jsoup.Jsoup.parse(html));
        assertEquals(1, links.size());
        assertEquals("ABC-123.mp4", links.get(0).getTitle());
        assertEquals("700 MB", links.get(0).getSize());
        assertTrue(links.get(0).hasMagnetLink());
    }

    @Test
    public void parseCiligouHtml() {
        String html = "<div class=\"SearchListTitle_list_title\">"
                + "<a href=\"/information/abcdefabcdefabcdefabcdefabcdefabcdefabcd\">磁力名</a>"
                + "</div>"
                + "<div class=\"Search_list_info\">文件大小：1.2 GB 创建时间：2026-03-04</div>";
        List<DownloadLink> links = BtSearchLinkProvider.parseCiligou(org.jsoup.Jsoup.parse(html));
        assertEquals(1, links.size());
        assertEquals("磁力名", links.get(0).getTitle());
        assertEquals("1.2 GB", links.get(0).getSize());
        assertEquals("2026-03-04", links.get(0).getDate());
        assertEquals("magnet:?xt=urn:btih:abcdefabcdefabcdefabcdefabcdefabcdefabcd", links.get(0).getMagnetLink());
    }

    @Test
    public void parseBtappHtml() {
        String html = "<table><tbody><tr>"
                + "<td>1</td>"
                + "<td><a href=\"view.php?id=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\">片源名</a></td>"
                + "<td>x</td>"
                + "<td>2.00 GB</td>"
                + "<td>2026-05-06</td>"
                + "</tr></tbody></table>";
        List<DownloadLink> links = BtSearchLinkProvider.parseBtapp(org.jsoup.Jsoup.parse(html));
        assertEquals(1, links.size());
        assertEquals("片源名", links.get(0).getTitle());
        assertEquals("2.00 GB", links.get(0).getSize());
        assertEquals("2026-05-06", links.get(0).getDate());
        assertTrue(links.get(0).hasMagnetLink());
    }

    @Test
    public void parseStarokInlineFiles() {
        String html = "<div class=\"list-group-item\">"
                + "<div class=\"title\"><a href=\"/detail/bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb\">合集</a></div>"
                + "<div class=\"foo\">文件大小：800 MB 创建时间：2026-07-08</div>"
                + "<div class=\"sub-title\">a.mp4</div>"
                + "<div class=\"sub-title\">b.jpg</div>"
                + "</div>";
        List<DownloadLink> links = BtSearchLinkProvider.parseStarok(org.jsoup.Jsoup.parse(html));
        assertEquals(1, links.size());
        assertEquals("合集", links.get(0).getTitle());
        assertEquals(2, links.get(0).getFiles().size());
        assertEquals("a.mp4", links.get(0).getFiles().get(0).getName());
        assertFalse(links.get(0).isFilesResolved());
    }

    @Test
    public void parseEmptyAndNull() {
        BtSearchLinkProvider provider = new BtSearchLinkProvider(BtSearchSite.SUKEBEI);
        assertTrue(provider.parseDownloadLinks(null).isEmpty());
        assertTrue(provider.parseDownloadLinks("").isEmpty());
        assertTrue(provider.parseDownloadLinks("<html></html>").isEmpty());
    }

    @Test
    public void parseMagnetFromHtml() {
        BtSearchLinkProvider provider = new BtSearchLinkProvider(BtSearchSite.SUKEBEI);
        MagnetLink magnet = provider.parseMagnetLink("<a href=\"magnet:?xt=urn:btih:cccccccccccccccccccccccccccccccccccccccc&dn=x\">m</a>");
        assertEquals("magnet:?xt=urn:btih:cccccccccccccccccccccccccccccccccccccccc", magnet.getMagnetLink());
        assertNull(provider.parseMagnetLink(null).getMagnetLink());
    }

    @Test
    public void parseBtdigHtml() {
        String html = "<div class=\"one_result\">"
                + "<div class=\"torrent_name\"><a href=\"/search?q=hash\">ABP-001 FHD</a></div>"
                + "<div class=\"torrent_excerpt\">ABP-001.mp4<br>ABP-001.jpg</div>"
                + "<span class=\"torrent_size\">1.50&nbsp;GB</span>"
                + "<span class=\"torrent_age\">2 years ago</span>"
                + "<a href=\"magnet:?xt=urn:btih:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee&amp;dn=ABP-001\">m</a>"
                + "</div>";
        List<DownloadLink> links = BtSearchLinkProvider.parseBtdig(org.jsoup.Jsoup.parse(html));
        assertEquals(1, links.size());
        assertEquals("ABP-001 FHD", links.get(0).getTitle());
        assertEquals("1.50 GB", links.get(0).getSize());
        assertEquals("2 years ago", links.get(0).getDate());
        assertEquals("magnet:?xt=urn:btih:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee", links.get(0).getMagnetLink());
        assertEquals(2, links.get(0).getFiles().size());
        assertEquals("ABP-001.mp4", links.get(0).getFiles().get(0).getName());
    }

    @Test
    public void btdigPageIsZeroBased() {
        String page1 = BtSearchSite.BTDIG.buildSearchUrl("ABP-001", 1);
        String page2 = BtSearchSite.BTDIG.buildSearchUrl("ABP-001", 2);
        assertTrue(page1.contains("p=0"));
        assertTrue(page2.contains("p=1"));
        assertTrue(page1.contains("q=ABP-001"));
        assertTrue(BtSearchSite.BTDIG.supportsPage(1));
        assertTrue(BtSearchSite.BTDIG.supportsPage(3));
        assertEquals("https://btdig.com", BtSearchSite.BTDIG.baseUrl);
    }

    @Test
    public void secondPageUnsupportedWithoutPlaceholder() {
        assertTrue(BtSearchSite.BTAPP.supportsPage(1));
        assertFalse(BtSearchSite.BTAPP.supportsPage(2));
        assertTrue(BtSearchSite.SUKEBEI.supportsPage(2));
        assertTrue(BtSearchSite.CILIGOU.supportsPage(3));
    }

    @Test
    public void downloadLinkHasMagnetWithoutAmp() {
        DownloadLink link = DownloadLink.create("t", "1MB", "2026-01-01", null,
                "magnet:?xt=urn:btih:dddddddddddddddddddddddddddddddddddddddd");
        assertTrue(link.hasMagnetLink());
        assertFalse(link.getMagnetLink().contains("&"));
    }
}
