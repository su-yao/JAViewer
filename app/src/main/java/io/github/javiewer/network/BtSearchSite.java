package io.github.javiewer.network;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BT-Aggregate-Search active site configs used by the download FAB.
 */
public class BtSearchSite {

    public static final String PARSER_CILIGOU = "ciligou";
    public static final String PARSER_STAROK = "starok";
    public static final String PARSER_DOBT = "dobt";
    public static final String PARSER_SOKITTY = "sokitty";
    public static final String PARSER_BTAPP = "btapp";
    public static final String PARSER_U9A9 = "u9a9";
    public static final String PARSER_NYAA = "nyaa";
    public static final String PARSER_TORRENTKITTY = "torrentkitty";
    public static final String PARSER_BTMOVI = "btmovi";
    public static final String PARSER_TTCL = "ttcl";
    public static final String PARSER_FOXR = "foxr";
    public static final String PARSER_BTDIG = "btdig";
    public static final String PARSER_GENERIC = "generic";

    public final String id;
    public final String name;
    public final String baseUrl;
    public final String searchUrl;
    public final String parser;
    public final String referer;
    public final String method;
    public final Map<String, String> postData;

    public BtSearchSite(String id, String name, String baseUrl, String searchUrl, String parser, String referer) {
        this(id, name, baseUrl, searchUrl, parser, referer, "get", null);
    }

    public BtSearchSite(String id, String name, String baseUrl, String searchUrl, String parser,
                        String referer, String method, Map<String, String> postData) {
        this.id = id;
        this.name = name;
        this.baseUrl = baseUrl;
        this.searchUrl = searchUrl;
        this.parser = parser;
        this.referer = referer;
        this.method = method == null ? "get" : method;
        this.postData = postData;
    }

    public boolean supportsPage(int page) {
        return page <= 1 || searchUrl.contains("{page}") || searchUrl.contains("{page0}");
    }

    public String buildSearchUrl(String keyword, int page) {
        String encoded = encodeKeyword(keyword);
        int pageIndex = Math.max(page - 1, 0);
        return searchUrl
                .replace("{kw}", encoded)
                .replace("{page0}", String.valueOf(pageIndex))
                .replace("{page}", String.valueOf(page));
    }

    public Map<String, String> buildPostData(String keyword) {
        if (postData == null || postData.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> data = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : postData.entrySet()) {
            data.put(entry.getKey(), entry.getValue().replace("{kw}", keyword == null ? "" : keyword.trim()));
        }
        return data;
    }

    public static String encodeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        try {
            return URLEncoder.encode(keyword.trim(), "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return keyword.trim();
        }
    }

    public static final BtSearchSite BTDIG = new BtSearchSite(
            "btdig", "BTDigg",
            "https://btdig.com",
            "https://btdig.com/search?q={kw}&p={page0}&order=0",
            PARSER_BTDIG, "https://btdig.com/"
    );

    public static final BtSearchSite SUKEBEI = new BtSearchSite(
            "sukebei", "Sukebei",
            "https://sukebei.nyaa.si",
            "https://sukebei.nyaa.si/?f=0&c=0_0&q={kw}&p={page}",
            PARSER_NYAA, "https://sukebei.nyaa.si/"
    );

    public static final BtSearchSite CILIGOU = new BtSearchSite(
            "ciligou", "磁力狗",
            "https://ciligou.de",
            "https://cdn.ciligou.in:39520/search?word={kw}&page={page}",
            PARSER_CILIGOU, "https://ciligou.de/"
    );

    public static final BtSearchSite TORRENTKITTY = new BtSearchSite(
            "torrentkitty", "TorrentKitty",
            "https://cn.torrentkitty.tv",
            "https://cn.torrentkitty.tv/search/{kw}?page={page}",
            PARSER_TORRENTKITTY, "https://cn.torrentkitty.tv/"
    );

    public static final BtSearchSite BTAPP = new BtSearchSite(
            "btapp", "磁力片",
            "https://t12.btapp.top",
            "https://so.btapp.top/so.php?word={kw}&host=t12.btapp.top",
            PARSER_BTAPP, "https://t12.btapp.top/"
    );

    public static final BtSearchSite BTMOVI = new BtSearchSite(
            "btmovi", "磁力蜘蛛",
            "https://btmovi.cyou",
            "https://btmovi.cyou/so/{kw}.html",
            PARSER_BTMOVI, "https://btmovi.cyou/"
    );

    public static final BtSearchSite STAROK = new BtSearchSite(
            "starok", "磁力星球",
            "https://so.starcs.top",
            "https://so.starcs.top/search?word={kw}&page={page}&host=so1.starok.top",
            PARSER_STAROK, "https://so1.starok.top/"
    );

    public static final BtSearchSite DOBT = new BtSearchSite(
            "dobt", "dobt",
            "https://hn.dobt.cc",
            "https://doc2.htmcdn.com:39988/search?word={kw}&host=hn.dobt.cc&v=1",
            PARSER_DOBT, "https://hn.dobt.cc/"
    );

    public static final BtSearchSite CILIMAO = new BtSearchSite(
            "cilimao", "磁力猫",
            "https://cilimao.de",
            "https://cdn.cilimao.fun:39520/search?word={kw}&host=cilimao.de",
            PARSER_CILIGOU, "https://cilimao.de/"
    );

    public static final List<BtSearchSite> TABS;

    static {
        List<BtSearchSite> tabs = new ArrayList<>();
        tabs.add(SUKEBEI);
        tabs.add(CILIGOU);
        tabs.add(TORRENTKITTY);
        tabs.add(BTAPP);
        tabs.add(BTMOVI);
        tabs.add(STAROK);
        tabs.add(DOBT);
        tabs.add(CILIMAO);
        TABS = Collections.unmodifiableList(tabs);
    }

    public static BtSearchSite find(String id) {
        if (id == null) {
            return null;
        }
        String key = id.toLowerCase().trim();
        for (BtSearchSite site : TABS) {
            if (site.id.equals(key)) {
                return site;
            }
        }
        if (BTDIG.id.equals(key)) {
            return BTDIG;
        }
        return null;
    }
}
