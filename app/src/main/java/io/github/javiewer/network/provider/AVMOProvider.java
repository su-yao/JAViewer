package io.github.javiewer.network.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.javiewer.adapter.item.Actress;
import io.github.javiewer.adapter.item.Genre;
import io.github.javiewer.adapter.item.Movie;
import io.github.javiewer.adapter.item.MovieDetail;
import io.github.javiewer.adapter.item.Screenshot;

/**
 * Project: JAViewer
 */
public class AVMOProvider {

    private static final Map<String, String> GENRE_TYPE_NAMES = new LinkedHashMap<>();

    static {
        GENRE_TYPE_NAMES.put("-1", "其他");
        GENRE_TYPE_NAMES.put("0", "主题");
        GENRE_TYPE_NAMES.put("1", "角色");
        GENRE_TYPE_NAMES.put("2", "服装");
        GENRE_TYPE_NAMES.put("3", "身体");
        GENRE_TYPE_NAMES.put("4", "性行为");
        GENRE_TYPE_NAMES.put("5", "玩法");
        GENRE_TYPE_NAMES.put("6", "类别");
        GENRE_TYPE_NAMES.put("7", "其他");
    }

    public static List<Movie> parseMovies(String html) {
        if (isBlank(html)) {
            return new ArrayList<>();
        }
        if (isJson(html)) {
            return parseMoviesJson(html);
        }

        Document document = Jsoup.parse(html);
        List<Movie> movies = new ArrayList<>();

        for (Element box : document.select("a[class*=movie-box]")) {
            Element img = box.select("div.photo-frame > img").first();
            Element span = box.select("div.photo-info > span").first();
            if (img == null || span == null) {
                continue;
            }

            boolean hot = span.getElementsByTag("i").size() > 0;
            Elements date = span.select("date");

            movies.add(
                    Movie.create(
                            img.attr("title"),
                            date.size() > 0 ? date.get(0).text() : "",
                            date.size() > 1 ? date.get(1).text() : "",
                            img.attr("src"),
                            box.attr("href"),
                            hot
                    )
            );
        }

        return movies;
    }

    public static List<Actress> parseActresses(String html) {
        if (isBlank(html)) {
            return new ArrayList<>();
        }
        if (isJson(html)) {
            return parseActressesJson(html);
        }

        Document document = Jsoup.parse(html);
        List<Actress> actresses = new ArrayList<>();

        for (Element box : document.select("a[class*=avatar-box]")) {
            Element img = box.select("div.photo-frame > img").first();
            Element span = box.select("div.photo-info > span").first();
            if (img == null || span == null) {
                continue;
            }

            actresses.add(
                    Actress.create(
                            span.text(),
                            img.attr("src"),
                            box.attr("href")
                    ));
        }

        return actresses;
    }

    public static MovieDetail parseMoviesDetail(String html) {
        if (isBlank(html)) {
            return new MovieDetail();
        }
        if (isJson(html)) {
            return parseMovieDetailJson(html);
        }

        Document document = Jsoup.parse(html);
        MovieDetail movie = new MovieDetail();

        {
            Element title = document.select("div.container > h3").first();
            Element cover = document.select("[class=bigImage]").first();
            movie.title = title != null ? title.text() : "";
            movie.coverUrl = cover != null ? cover.attr("href") : "";
        }

        {
            for (Element box : document.select("[class*=sample-box]")) {
                Element img = box.getElementsByTag("img").first();
                if (img == null) {
                    continue;
                }
                movie.screenshots.add(
                        Screenshot.create(
                                img.attr("src"),
                                box.attr("href")
                        )
                );
            }
        }

        {
            for (Element box : document.select("[class*=avatar-box]")) {
                Element img = box.getElementsByTag("img").first();
                movie.actresses.add(
                        Actress.create(
                                box.text(),
                                img != null ? img.attr("src") : "",
                                box.attr("href")
                        )
                );
            }
        }

        {
            Element info = document.select("div.info").first();
            if (info != null) {
                for (Element p : info.select("p:not([class*=header]):has(span:not([class=genre]))")) {
                    String[] strings = p.text().split(":");
                    movie.headers.add(MovieDetail.Header.create(
                            strings[0].trim(),
                            strings.length > 1 ? strings[1].trim() : "",
                            null
                    ));
                }

                {
                    List<String> headerNames = new ArrayList<>();
                    List<String[]> headerAttr = new ArrayList<>();

                    for (Element p : info.select("p[class*=header]")) {
                        headerNames.add(p.text().replace(":", ""));
                    }

                    for (Element a : info.select("p > a")) {
                        headerAttr.add(new String[]{a.text(), a.attr("href")});
                    }

                    for (int i = 0; i < Math.min(headerNames.size(), headerAttr.size()); i++) {
                        movie.headers.add(
                                MovieDetail.Header.create(
                                        headerNames.get(i),
                                        headerAttr.get(i)[0].trim(),
                                        headerAttr.get(i)[1].trim()
                                )
                        );
                    }
                }

                for (Element a : info.select("* > [class=genre] > a")) {
                    movie.genres.add(
                            Genre.create(
                                    a.text(),
                                    a.attr("href")
                            )
                    );
                }
            }
            return movie;
        }
    }

    public static LinkedHashMap<String, List<Genre>> parseGenres(String html) {
        if (isBlank(html)) {
            return new LinkedHashMap<>();
        }
        if (isJson(html)) {
            return parseGenresJson(html);
        }

        LinkedHashMap<String, List<Genre>> map = new LinkedHashMap<>();

        Element container = Jsoup.parse(html).getElementsByClass("pt-10").first();
        if (container == null) {
            return map;
        }
        List<String> keys = new ArrayList<>();
        for (Element e : container.getElementsByTag("h4")) {
            keys.add(e.text());
        }

        List<List<Genre>> genres = new ArrayList<>();
        for (Element element : container.getElementsByClass("genre-box")) {
            List<Genre> list = new ArrayList<>();
            for (Element e : element.getElementsByTag("a")) {
                list.add(Genre.create(e.text(), e.attr("href")));
            }
            genres.add(list);
        }

        for (int i = 0; i < Math.min(keys.size(), genres.size()); i++) {
            map.put(keys.get(i), genres.get(i));
        }

        return map;
    }

    private static boolean isBlank(String body) {
        return body == null || body.trim().isEmpty();
    }

    private static boolean isJson(String body) {
        if (body == null) {
            return false;
        }
        String trim = body.trim();
        return trim.startsWith("{") || trim.startsWith("[");
    }

    private static JsonElement parseJson(String body) {
        try {
            return new JsonParser().parse(body.trim());
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    private static JsonElement dataOf(String body) {
        JsonElement root = parseJson(body);
        if (root.isJsonObject() && root.getAsJsonObject().has("data")) {
            return root.getAsJsonObject().get("data");
        }
        return root;
    }

    private static List<Movie> parseMoviesJson(String body) {
        List<Movie> movies = new ArrayList<>();
        JsonElement data = dataOf(body);
        if (data == null || !data.isJsonArray()) {
            return movies;
        }
        for (JsonElement element : data.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject obj = element.getAsJsonObject();
            String movieId = string(obj, "movieId");
            movies.add(Movie.create(
                    firstText(obj, "title", "title_cn", "title_ja", "title_tw", "title_en", "movieFanHao"),
                    string(obj, "movieFanHao"),
                    string(obj, "releaseDate"),
                    firstText(obj, "posterSmall", "posterLarge"),
                    movieId.isEmpty() ? null : "movie:" + movieId,
                    intValue(obj, "download") > 0
            ));
        }
        return movies;
    }

    private static List<Actress> parseActressesJson(String body) {
        List<Actress> actresses = new ArrayList<>();
        JsonElement data = dataOf(body);
        if (data == null || !data.isJsonArray()) {
            return actresses;
        }
        for (JsonElement element : data.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject obj = element.getAsJsonObject();
            String starId = string(obj, "starId");
            actresses.add(Actress.create(
                    firstText(obj, "starName", "starName_cn", "starName_ja", "starName_tw", "starName_en"),
                    firstText(obj, "avatarUrl", "avatar"),
                    starId.isEmpty() ? null : "star:" + starId
            ));
        }
        return actresses;
    }

    private static MovieDetail parseMovieDetailJson(String body) {
        MovieDetail movie = new MovieDetail();
        JsonElement data = dataOf(body);
        if (data == null || !data.isJsonObject()) {
            return movie;
        }
        JsonObject obj = data.getAsJsonObject();
        movie.title = firstText(obj, "title", "title_cn", "title_ja", "title_tw", "title_en", "movieFanHao");
        movie.coverUrl = firstText(obj, "posterLarge", "posterSmall");

        addHeader(movie, "识别码", string(obj, "movieFanHao"), null);
        addHeader(movie, "发行时间", string(obj, "releaseDate"), null);
        int length = intValue(obj, "length");
        if (length > 0) {
            addHeader(movie, "长度", length + "分钟", null);
        }
        addEntityHeader(movie, "制作商", obj.get("studio"), "studio");
        addEntityHeader(movie, "发行商", obj.get("label"), "label");
        addEntityHeader(movie, "系列", obj.get("series"), "series");
        addEntityHeader(movie, "导演", obj.get("director"), "director");

        appendGenres(movie.genres, obj.get("genre"));
        appendActresses(movie.actresses, obj);

        JsonArray small = array(obj, "sampleSmall");
        JsonArray large = array(obj, "sampleLarge");
        int count = Math.max(small.size(), large.size());
        for (int i = 0; i < count; i++) {
            String thumb = i < small.size() ? asString(small.get(i)) : "";
            String image = i < large.size() ? asString(large.get(i)) : thumb;
            if (thumb.isEmpty()) {
                thumb = image;
            }
            if (!thumb.isEmpty() || !image.isEmpty()) {
                movie.screenshots.add(Screenshot.create(thumb, image));
            }
        }
        return movie;
    }

    private static LinkedHashMap<String, List<Genre>> parseGenresJson(String body) {
        LinkedHashMap<String, List<Genre>> map = new LinkedHashMap<>();
        JsonElement data = dataOf(body);
        if (data == null || !data.isJsonObject()) {
            return map;
        }
        JsonObject obj = data.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String title = GENRE_TYPE_NAMES.containsKey(entry.getKey())
                    ? GENRE_TYPE_NAMES.get(entry.getKey())
                    : entry.getKey();
            List<Genre> list = new ArrayList<>();
            if (entry.getValue() != null && entry.getValue().isJsonArray()) {
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    JsonObject genre = element.getAsJsonObject();
                    String genreId = string(genre, "genreId");
                    list.add(Genre.create(
                            firstText(genre, "genreName", "genreName_cn", "genreName_ja", "genreName_tw", "genreName_en"),
                            genreId.isEmpty() ? null : "genre:" + genreId
                    ));
                }
            }
            if (!list.isEmpty()) {
                map.put(title, list);
            }
        }
        return map;
    }

    private static void addHeader(MovieDetail movie, String name, String value, String link) {
        if (value == null || value.isEmpty()) {
            return;
        }
        movie.headers.add(MovieDetail.Header.create(name, value, link));
    }

    private static void addEntityHeader(MovieDetail movie, String name, JsonElement element, String type) {
        JsonObject obj = firstObject(element);
        if (obj == null) {
            return;
        }
        String value = firstText(obj, type + "Name", type + "Name_cn", type + "Name_ja", type + "Name_tw", type + "Name_en", "name");
        String id = string(obj, type + "Id");
        addHeader(movie, name, value, id.isEmpty() ? null : type + ":" + id);
    }

    private static void appendGenres(List<Genre> genres, JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonArray()) {
            for (JsonElement item : element.getAsJsonArray()) {
                appendGenres(genres, item);
            }
            return;
        }
        JsonObject obj = firstObject(element);
        if (obj == null) {
            return;
        }
        String genreId = string(obj, "genreId");
        String name = firstText(obj, "genreName", "genreName_cn", "genreName_ja", "genreName_tw", "genreName_en");
        if (!name.isEmpty()) {
            genres.add(Genre.create(name, genreId.isEmpty() ? null : "genre:" + genreId));
        }
    }

    private static void appendActresses(List<Actress> actresses, JsonObject movie) {
        JsonElement stars = movie.get("stars");
        if (stars == null || stars.isJsonNull()) {
            stars = movie.get("star");
        }
        if (stars == null || stars.isJsonNull()) {
            stars = movie.get("actresses");
        }
        if (stars == null || stars.isJsonNull()) {
            return;
        }
        if (stars.isJsonArray()) {
            for (JsonElement item : stars.getAsJsonArray()) {
                appendActress(actresses, item);
            }
            return;
        }
        appendActress(actresses, stars);
    }

    private static void appendActress(List<Actress> actresses, JsonElement element) {
        JsonObject obj = firstObject(element);
        if (obj == null) {
            return;
        }
        String starId = string(obj, "starId");
        String name = firstText(obj, "starName", "starName_cn", "starName_ja", "starName_tw", "starName_en");
        if (name.isEmpty()) {
            return;
        }
        actresses.add(Actress.create(
                name,
                firstText(obj, "avatarUrl", "avatar"),
                starId.isEmpty() ? null : "star:" + starId
        ));
    }

    private static JsonObject firstObject(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        if (element.isJsonArray() && element.getAsJsonArray().size() > 0) {
            JsonElement first = element.getAsJsonArray().get(0);
            if (first.isJsonObject()) {
                return first.getAsJsonObject();
            }
        }
        return null;
    }

    private static JsonArray array(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull() || !obj.get(key).isJsonArray()) {
            return new JsonArray();
        }
        return obj.getAsJsonArray(key);
    }

    private static String firstText(JsonObject obj, String... keys) {
        if (obj == null) {
            return "";
        }
        for (String key : keys) {
            String value = string(obj, key);
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private static String string(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return "";
        }
        return asString(obj.get(key));
    }

    private static String asString(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return "";
        }
        try {
            if (element.isJsonPrimitive()) {
                return element.getAsString().trim();
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private static int intValue(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return 0;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (Exception ignored) {
            try {
                return Integer.parseInt(obj.get(key).getAsString());
            } catch (Exception ignoredAgain) {
                return 0;
            }
        }
    }
}
