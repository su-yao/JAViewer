package io.github.javiewer.network;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import io.github.javiewer.JAViewer;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public final class SiteApi {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson GSON = new Gson();

    private SiteApi() {
    }

    public static RequestBody json(Object... args) {
        return RequestBody.create(JSON, GSON.toJson(args));
    }

    public static Call<ResponseBody> home(int page) {
        Map<String, Object> params = new HashMap<>();
        params.put("lang", BasicService.LANG);
        return JAViewer.SERVICE.getMovies(json(params, BasicService.PAGE_SIZE, page));
    }

    public static Call<ResponseBody> released(int page) {
        return filter("released", null, page);
    }

    public static Call<ResponseBody> popular(int page) {
        return filter("popular", null, page);
    }

    public static Call<ResponseBody> actresses(int page) {
        Map<String, Object> params = new HashMap<>();
        params.put("lang", BasicService.LANG);
        params.put("actresses", true);
        return JAViewer.SERVICE.getStars(json(params, BasicService.STAR_PAGE_SIZE, page));
    }

    public static Call<ResponseBody> genres() {
        return JAViewer.SERVICE.getGenres(json(BasicService.LANG));
    }

    public static Call<ResponseBody> movieDetail(String link) {
        return JAViewer.SERVICE.getMovie(json(idFrom(link, "movie:"), BasicService.LANG));
    }

    public static Call<ResponseBody> filter(String type, String id, int page) {
        return JAViewer.SERVICE.getFilterMovies(json(type, id, BasicService.LANG, BasicService.PAGE_SIZE, page));
    }

    public static Call<ResponseBody> search(String keyword, int page) {
        Map<String, Object> params = new HashMap<>();
        params.put("search", keyword);
        params.put("lang", BasicService.LANG);
        return JAViewer.SERVICE.search(json(params, BasicService.PAGE_SIZE, page));
    }

    public static Call<ResponseBody> fromLink(String link, int page) {
        if (link == null) {
            return home(page);
        }
        if (link.startsWith("search:")) {
            return search(link.substring("search:".length()), page);
        }
        if (link.startsWith("star:")) {
            return filter("star", link.substring("star:".length()), page);
        }
        if (link.startsWith("genre:")) {
            return filter("genre", link.substring("genre:".length()), page);
        }
        if (link.startsWith("studio:")) {
            return filter("studio", link.substring("studio:".length()), page);
        }
        if (link.startsWith("director:")) {
            return filter("director", link.substring("director:".length()), page);
        }
        if (link.startsWith("label:")) {
            return filter("label", link.substring("label:".length()), page);
        }
        if (link.startsWith("series:")) {
            return filter("series", link.substring("series:".length()), page);
        }
        int searchIndex = link.indexOf("/search/");
        if (searchIndex >= 0) {
            String keyword = link.substring(searchIndex + "/search/".length());
            int slash = keyword.indexOf('/');
            if (slash >= 0) {
                keyword = keyword.substring(0, slash);
            }
            return search(keyword, page);
        }
        return home(page);
    }

    public static String idFrom(String link, String prefix) {
        if (link == null) {
            return "";
        }
        if (link.startsWith(prefix)) {
            return link.substring(prefix.length());
        }
        int slash = link.lastIndexOf('/');
        if (slash >= 0 && slash < link.length() - 1) {
            return link.substring(slash + 1);
        }
        return link;
    }
}
