package io.github.javiewer.network.provider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.javiewer.adapter.item.DownloadLink;
import io.github.javiewer.adapter.item.MagnetLink;
import io.github.javiewer.adapter.item.TorrentFile;
import io.github.javiewer.network.BtSearch;
import io.github.javiewer.network.BtSearchSite;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class BtSearchLinkProvider extends DownloadLinkProvider {

    private static final Pattern HASH = Pattern.compile("(?:btih:|/information/|/detail/|/hash/|/bt/|/doc/|id=)([a-fA-F0-9]{32,40})", Pattern.CASE_INSENSITIVE);
    private static final Pattern MAGNET = Pattern.compile("magnet:\\?xt=urn:btih:[a-fA-F0-9]{32,40}", Pattern.CASE_INSENSITIVE);
    private static final Pattern SIZE = Pattern.compile("(\\d+(?:\\.\\d+)?\\s*(?:GiB|MiB|KiB|GB|MB|KB|B))", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2})");

    private final BtSearchSite site;

    public BtSearchLinkProvider(BtSearchSite site) {
        this.site = site;
    }

    public BtSearchSite getSite() {
        return site;
    }

    @Override
    public Call<ResponseBody> search(String keyword, int page) {
        if (site == null || !site.supportsPage(page)) {
            return null;
        }
        String url = site.buildSearchUrl(keyword, page);
        if ("post".equalsIgnoreCase(site.method)) {
            return BtSearch.INSTANCE.post(url, site.buildPostData(keyword), site.referer);
        }
        return BtSearch.INSTANCE.get(url, site.referer);
    }

    @Override
    public List<DownloadLink> parseDownloadLinks(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty() || site == null) {
            return new ArrayList<>();
        }
        Document doc = Jsoup.parse(htmlContent);
        String parser = site.parser;
        if (BtSearchSite.PARSER_NYAA.equals(parser)) {
            return parseNyaa(doc);
        }
        if (BtSearchSite.PARSER_TORRENTKITTY.equals(parser)) {
            return parseTorrentKitty(doc);
        }
        if (BtSearchSite.PARSER_CILIGOU.equals(parser)) {
            return parseCiligou(doc);
        }
        if (BtSearchSite.PARSER_STAROK.equals(parser)) {
            return parseStarok(doc);
        }
        if (BtSearchSite.PARSER_DOBT.equals(parser)) {
            return parseDobt(doc);
        }
        if (BtSearchSite.PARSER_SOKITTY.equals(parser)) {
            return parseSokitty(doc);
        }
        if (BtSearchSite.PARSER_BTAPP.equals(parser)) {
            return parseBtapp(doc);
        }
        if (BtSearchSite.PARSER_U9A9.equals(parser)) {
            return parseU9a9(doc);
        }
        if (BtSearchSite.PARSER_BTMOVI.equals(parser)) {
            return parseBtmovi(doc);
        }
        if (BtSearchSite.PARSER_TTCL.equals(parser)) {
            return parseTtcl(doc);
        }
        if (BtSearchSite.PARSER_FOXR.equals(parser)) {
            return parseFoxr(doc);
        }
        if (BtSearchSite.PARSER_BTDIG.equals(parser)) {
            return parseBtdig(doc);
        }
        return parseGeneric(doc);
    }

    @Override
    public Call<ResponseBody> get(String url) {
        if (url == null || url.trim().isEmpty() || site == null) {
            return null;
        }
        return BtSearch.INSTANCE.get(url, site.referer);
    }

    @Override
    public MagnetLink parseMagnetLink(String htmlContent) {
        if (htmlContent == null) {
            return MagnetLink.create(null);
        }
        Matcher matcher = MAGNET.matcher(htmlContent);
        if (matcher.find()) {
            return MagnetLink.create(matcher.group());
        }
        String hash = firstHash(htmlContent);
        if (hash != null) {
            return MagnetLink.create(magnetOf(hash));
        }
        return MagnetLink.create(null);
    }

    static List<DownloadLink> parseNyaa(Document doc) {
        List<DownloadLink> links = new ArrayList<>();
        Elements rows = doc.select("tr.default, tr.success");
        if (rows.isEmpty()) {
            for (Element row : doc.select("tr")) {
                if (row.selectFirst("td.col-name a.t-name") != null) {
                    rows.add(row);
                }
            }
        }
        for (Element row : rows) {
            try {
                Element magnetA = row.selectFirst("a[href^=magnet]");
                if (magnetA == null) {
                    continue;
                }
                Element nameA = first(row, "td[colspan=2] a", "td.col-name a.t-name", "a[title]");
                String name = textOf(nameA);
                if (name.isEmpty()) {
                    continue;
                }
                String size = "";
                String date = "";
                Element sizeTd = row.selectFirst("td.col-size");
                Element dateTd = row.selectFirst("td.col-date");
                if (sizeTd == null || dateTd == null) {
                    Elements tds = row.select("td");
                    if (tds.size() > 4) {
                        size = tds.get(3).text().trim();
                        date = tds.get(4).text().trim();
                    }
                } else {
                    size = sizeTd.text().trim();
                    date = dateTd.text().trim();
                }
                links.add(DownloadLink.create(name, size, date, null, magnetA.attr("href"), parseInlineFiles(row)));
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    static List<DownloadLink> parseTorrentKitty(Document doc) {
        List<DownloadLink> links = new ArrayList<>();
        for (Element row : doc.select("tr")) {
            try {
                Element magnetA = row.selectFirst("a[href^=magnet]");
                if (magnetA == null) {
                    continue;
                }
                Element nameTd = row.selectFirst("td.name");
                String name = textOf(nameTd);
                if (name.isEmpty()) {
                    continue;
                }
                String size = textOf(row.selectFirst("td.size"));
                String date = textOf(row.selectFirst("td.date"));
                links.add(DownloadLink.create(name, size, date, null, magnetA.attr("href")));
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    static List<DownloadLink> parseCiligou(Document doc) {
        List<DownloadLink> links = new ArrayList<>();
        Elements titles = doc.select(".SearchListTitle_list_title");
        Elements infos = doc.select(".Search_list_info");
        for (int i = 0; i < titles.size(); i++) {
            try {
                Element titleTag = titles.get(i).selectFirst("a[href*=information]");
                if (titleTag == null) {
                    continue;
                }
                String name = titleTag.text().trim();
                String hash = firstHash(titleTag.attr("href"));
                if (name.isEmpty() || hash == null) {
                    continue;
                }
                String size = "";
                String date = "";
                if (i < infos.size()) {
                    String infoText = infos.get(i).text();
                    size = firstGroup(SIZE, infoText);
                    date = firstGroup(DATE, infoText);
                }
                links.add(DownloadLink.create(name, size, date, null, magnetOf(hash), parseInlineFiles(titles.get(i).parent())));
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    static List<DownloadLink> parseStarok(Document doc) {
        List<DownloadLink> links = new ArrayList<>();
        for (Element item : doc.select(".list-group-item")) {
            try {
                Element titleTag = item.selectFirst(".title a");
                if (titleTag == null) {
                    continue;
                }
                String name = titleTag.text().trim();
                String hash = firstHash(titleTag.attr("href"));
                if (name.isEmpty() || hash == null) {
                    continue;
                }
                String foo = textOf(item.selectFirst(".foo"));
                List<TorrentFile> files = new ArrayList<>();
                for (Element sub : item.select(".sub-title")) {
                    String fileName = sub.text().trim();
                    if (!fileName.isEmpty()) {
                        files.add(TorrentFile.create(fileName, ""));
                    }
                }
                links.add(DownloadLink.create(name, firstGroup(SIZE, foo), firstGroup(DATE, foo), null, magnetOf(hash), files));
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    static List<DownloadLink> parseDobt(Document doc) {
        List<DownloadLink> links = new ArrayList<>();
        for (Element item : doc.select(".ssbox")) {
            try {
                Element titleTag = item.selectFirst(".title h3 a");
                if (titleTag == null) {
                    continue;
                }
                String name = titleTag.text().trim();
                Element sbar = item.selectFirst(".sbar");
                String magnet = null;
                if (sbar != null) {
                    Element magnetTag = sbar.selectFirst("a[href*=magnet]");
                    if (magnetTag != null) {
                        magnet = magnetTag.attr("href");
                    }
                }
                if (magnet == null) {
                    String hash = firstHash(titleTag.attr("href"));
                    if (hash != null) {
                        magnet = magnetOf(hash);
                    }
                }
                if (name.isEmpty() || magnet == null) {
                    continue;
                }
                String sbarText = sbar == null ? "" : sbar.text();
                links.add(DownloadLink.create(name, firstGroup(SIZE, sbarText), firstGroup(DATE, sbarText), null, magnet));
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    static List<DownloadLink> parseSokitty(Document doc) {
        List<DownloadLink> links = new ArrayList<>();
        for (Element title : doc.select(".list-title")) {
            try {
                String href = title.attr("href");
                if (!href.startsWith("/bt/")) {
                    continue;
                }
                String name = title.text().trim();
                String hash = firstHash(href);
                if (name.isEmpty() || hash == null) {
                    continue;
                }
                String parentText = title.parent() == null ? "" : title.parent().text();
                links.add(DownloadLink.create(name, firstGroup(SIZE, parentText), firstGroup(DATE, parentText), null, magnetOf(hash)));
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    static List<DownloadLink> parseBtapp(Document doc) {
        List<DownloadLink> links = new ArrayList<>();
        for (Element row : doc.select("tbody tr")) {
            try {
                Elements tds = row.select("td");
                if (tds.size() < 5) {
                    continue;
                }
                Element titleTag = tds.get(1).selectFirst("a");
                if (titleTag == null) {
                    continue;
                }
                String name = titleTag.text().trim();
                String hash = firstHash(titleTag.attr("href"));
                if (name.isEmpty() || hash == null) {
                    continue;
                }
                String size = tds.size() > 3 ? tds.get(3).text().trim() : "";
                String date = tds.size() > 4 ? tds.get(4).text().trim() : "";
                links.add(DownloadLink.create(name, size, date, null, magnetOf(hash)));
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    static List<DownloadLink> parseU9a9(Document doc) {
        List<DownloadLink> links = new ArrayList<>();
        for (Element row : doc.select("table tr")) {
            try {
                Elements tds = row.select("td");
                if (tds.size() < 5) {
                    continue;
                }
                Element titleTag = tds.get(1).selectFirst("a");
                Element magnetTag = tds.get(2).selectFirst("a[href*=magnet]");
                if (titleTag == null || magnetTag == null) {
                    continue;
                }
                String name = titleTag.text().trim();
                if (name.isEmpty()) {
                    continue;
                }
                String size = tds.get(3).text().trim();
                String date = tds.get(4).text().trim();
                if (date.contains(" ")) {
                    date = date.split(" ")[0];
                }
                links.add(DownloadLink.create(name, size, date, null, magnetTag.attr("href")));
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    static List<DownloadLink> parseBtmovi(Document doc) {
        List<DownloadLink> links = new ArrayList<>();
        for (Element item : doc.select("div.search-item")) {
            try {
                Element a = first(item, "div.item-title h3 a", "a[href*=/bt/]");
                if (a == null) {
                    continue;
                }
                String name = a.text().trim();
                String hash = firstHash(a.attr("href"));
                if (name.isEmpty() || hash == null) {
                    continue;
                }
                String size = "";
                String date = "";
                Element bar = item.selectFirst("div.item-bar");
                if (bar != null) {
                    for (Element span : bar.select("span")) {
                        String txt = span.text().trim();
                        if (txt.contains("创建时间")) {
                            date = txt.replaceAll("创建时间[:：]\\s*", "");
                        } else if (txt.contains("文件大小")) {
                            size = txt.replaceAll("文件大小[:：]\\s*", "");
                        }
                    }
                }
                links.add(DownloadLink.create(name, size, date, null, magnetOf(hash)));
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    static List<DownloadLink> parseTtcl(Document doc) {
        List<DownloadLink> links = new ArrayList<>();
        for (Element panel : doc.select("div.search-panel")) {
            try {
                Element a = panel.selectFirst("a.list-title[href^=/bt/]");
                if (a == null) {
                    continue;
                }
                String name = a.text().trim();
                String hash = firstHash(a.attr("href"));
                if (name.isEmpty() || hash == null) {
                    continue;
                }
                String size = "";
                String date = "";
                Element footer = panel.selectFirst("div.panel-footer");
                if (footer != null) {
                    Elements spans = footer.select("span.info-item");
                    if (spans.size() >= 1) {
                        size = spans.get(0).text().trim();
                    }
                    if (spans.size() >= 3) {
                        date = spans.get(2).text().trim();
                    }
                }
                links.add(DownloadLink.create(name, size, date, null, magnetOf(hash)));
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    static List<DownloadLink> parseFoxr(Document doc) {
        List<DownloadLink> links = new ArrayList<>();
        for (Element box : doc.select("div.search-box")) {
            try {
                Element a = box.selectFirst("a[href^=/doc/]");
                if (a == null) {
                    continue;
                }
                String name = a.attr("title").trim();
                if (name.isEmpty()) {
                    name = a.text().trim();
                }
                String hash = firstHash(a.attr("href"));
                if (name.isEmpty() || hash == null) {
                    continue;
                }
                String size = "";
                String date = "";
                for (Element p : box.select("div.layui-colla-content p")) {
                    String txt = p.text();
                    Element sp = p.selectFirst("span");
                    String val = sp == null ? "" : sp.text().trim();
                    if (txt.contains("创建时间")) {
                        date = val;
                    } else if (txt.contains("文件大小")) {
                        size = val;
                    }
                }
                links.add(DownloadLink.create(name, size, date, null, magnetOf(hash)));
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    static List<DownloadLink> parseBtdig(Document doc) {
        List<DownloadLink> links = new ArrayList<>();
        for (Element item : doc.select("div.one_result")) {
            try {
                Element magnetA = item.selectFirst("a[href^=magnet]");
                Element nameA = first(item, "div.torrent_name a", "a[href^=magnet]");
                if (magnetA == null) {
                    continue;
                }
                String name = textOf(nameA);
                if (name.isEmpty()) {
                    name = magnetA.attr("title").trim();
                }
                if (name.isEmpty()) {
                    continue;
                }
                String size = cleanSpace(textOf(item.selectFirst("span.torrent_size, .torrent_size")));
                String date = cleanSpace(textOf(item.selectFirst("span.torrent_age, .torrent_age")));
                links.add(DownloadLink.create(name, size, date, null, magnetA.attr("href"), parseBtdigFiles(item)));
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    static List<DownloadLink> parseGeneric(Document doc) {
        List<DownloadLink> links = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Element a : doc.select("a[href^=magnet]")) {
            String magnet = a.attr("href");
            String hash = firstHash(magnet);
            if (hash == null || !seen.add(hash.toLowerCase(Locale.US))) {
                continue;
            }
            String name = a.text().trim();
            if (name.isEmpty() && a.parent() != null) {
                name = a.parent().text().trim();
                if (name.length() > 80) {
                    name = name.substring(0, 80);
                }
            }
            if (name.isEmpty()) {
                continue;
            }
            String context = a.parent() == null ? "" : a.parent().text();
            links.add(DownloadLink.create(name, firstGroup(SIZE, context), firstGroup(DATE, context), null, magnet));
        }
        return links;
    }

    private static List<TorrentFile> parseBtdigFiles(Element item) {
        List<TorrentFile> files = new ArrayList<>();
        Element excerpt = item.selectFirst("div.torrent_excerpt, .torrent_excerpt, div.torrent_files, .torrent_files");
        if (excerpt == null) {
            return files;
        }
        String[] parts = excerpt.html().split("(?i)<br\\s*/?>");
        for (String part : parts) {
            String name = Jsoup.parse(part).text().trim();
            if (name.isEmpty()) {
                continue;
            }
            files.add(TorrentFile.create(name, firstGroup(SIZE, name)));
        }
        return files;
    }

    private static List<TorrentFile> parseInlineFiles(Element root) {
        List<TorrentFile> files = new ArrayList<>();
        if (root == null) {
            return files;
        }
        for (Element file : root.select(".file, .filename, .sub-title, li.file")) {
            String name = file.text().trim();
            if (!name.isEmpty()) {
                files.add(TorrentFile.create(name, firstGroup(SIZE, name)));
            }
        }
        return files;
    }

    private static Element first(Element root, String... selectors) {
        for (String selector : selectors) {
            Element el = root.selectFirst(selector);
            if (el != null) {
                return el;
            }
        }
        return null;
    }

    private static String textOf(Element el) {
        return el == null ? "" : el.text().trim();
    }

    private static String cleanSpace(String value) {
        return value == null ? "" : value.replace('\u00a0', ' ').trim();
    }

    static String firstHash(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = HASH.matcher(value);
        return matcher.find() ? matcher.group(1) : null;
    }

    static String magnetOf(String hash) {
        return "magnet:?xt=urn:btih:" + hash;
    }

    static String firstGroup(Pattern pattern, String text) {
        if (text == null) {
            return "";
        }
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "";
    }
}
