package io.github.javiewer.network.provider;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;

import io.github.javiewer.adapter.item.Actress;
import io.github.javiewer.adapter.item.Genre;
import io.github.javiewer.adapter.item.Movie;
import io.github.javiewer.adapter.item.MovieDetail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AVMOProviderTest {

    @Test
    public void parseMoviesJson() {
        String body = "{\"code\":200,\"data\":[{"
                + "\"movieId\":\"nelvrjm\","
                + "\"movieFanHao\":\"DFE-118\","
                + "\"title\":\"三穴責め続け中出し漬け あおい藍\","
                + "\"releaseDate\":\"2026-11-02\","
                + "\"posterSmall\":\"https://example.com/ps.jpg\","
                + "\"download\":0"
                + "}]}";
        List<Movie> movies = AVMOProvider.parseMovies(body);
        assertEquals(1, movies.size());
        assertEquals("DFE-118", movies.get(0).getCode());
        assertEquals("三穴責め続け中出し漬け あおい藍", movies.get(0).getTitle());
        assertEquals("2026-11-02", movies.get(0).getDate());
        assertEquals("movie:nelvrjm", movies.get(0).getLink());
        assertFalse(movies.get(0).isHot());
    }

    @Test
    public void parseActressesJson() {
        String body = "{\"code\":200,\"data\":[{"
                + "\"starId\":\"jkzqbvn\","
                + "\"starName\":\"波多野結衣\","
                + "\"avatarUrl\":\"https://example.com/hatano_yui.jpg\""
                + "}]}";
        List<Actress> actresses = AVMOProvider.parseActresses(body);
        assertEquals(1, actresses.size());
        assertEquals("波多野結衣", actresses.get(0).getName());
        assertEquals("star:jkzqbvn", actresses.get(0).getLink());
        assertEquals("https://example.com/hatano_yui.jpg", actresses.get(0).getImageUrl());
    }

    @Test
    public void parseMovieDetailJson() {
        String body = "{\"code\":200,\"data\":{"
                + "\"movieId\":\"kdldmro\","
                + "\"movieFanHao\":\"SAVR-1219\","
                + "\"title\":\"【VR】爆乳セレブ妻のM男飼育\","
                + "\"releaseDate\":\"2026-11-01\","
                + "\"length\":84,"
                + "\"posterLarge\":\"https://example.com/pl.jpg\","
                + "\"studio\":{\"studioId\":\"pkpzavk\",\"studioName\":\"Studio A\"},"
                + "\"genre\":[{\"genreId\":\"g1\",\"genreName\":\"VR\"}],"
                + "\"star\":[{\"starId\":\"jkzqbvn\",\"starName\":\"波多野結衣\",\"avatarUrl\":\"https://example.com/a.jpg\"}],"
                + "\"sampleSmall\":[\"https://example.com/1.jpg\"],"
                + "\"sampleLarge\":[\"https://example.com/1l.jpg\"]"
                + "}}";
        MovieDetail detail = AVMOProvider.parseMoviesDetail(body);
        assertEquals("【VR】爆乳セレブ妻のM男飼育", detail.title);
        assertEquals("https://example.com/pl.jpg", detail.coverUrl);
        assertEquals(1, detail.screenshots.size());
        assertEquals("VR", detail.genres.get(0).getName());
        assertEquals("genre:g1", detail.genres.get(0).getLink());
        assertEquals("波多野結衣", detail.actresses.get(0).getName());
        assertEquals("star:jkzqbvn", detail.actresses.get(0).getLink());
        assertTrue(detail.headers.size() >= 3);
        assertEquals("识别码", detail.headers.get(0).getName());
        assertEquals("SAVR-1219", detail.headers.get(0).getValue());
    }

    @Test
    public void parseGenresJson() {
        String body = "{\"code\":200,\"data\":{"
                + "\"0\":[{\"genreId\":\"xlkbvbk\",\"genreName\":\"性感的\"}]"
                + "}}";
        LinkedHashMap<String, List<Genre>> map = AVMOProvider.parseGenres(body);
        assertTrue(map.containsKey("主题"));
        assertEquals("性感的", map.get("主题").get(0).getName());
        assertEquals("genre:xlkbvbk", map.get("主题").get(0).getLink());
    }

    @Test
    public void parseMoviesEmptyAndError() {
        assertTrue(AVMOProvider.parseMovies(null).isEmpty());
        assertTrue(AVMOProvider.parseMovies("").isEmpty());
        assertTrue(AVMOProvider.parseMovies("{\"code\":400}").isEmpty());
        assertTrue(AVMOProvider.parseMovies("{\"code\":200,\"data\":[]}").isEmpty());
        assertTrue(AVMOProvider.parseMovies("{not-json").isEmpty());
        assertTrue(AVMOProvider.parseMovies("<html><body></body></html>").isEmpty());
    }

    @Test
    public void parseActressesEmpty() {
        assertTrue(AVMOProvider.parseActresses(null).isEmpty());
        assertTrue(AVMOProvider.parseActresses("{\"code\":200,\"data\":null}").isEmpty());
    }

    @Test
    public void parseMovieDetailEmpty() {
        MovieDetail detail = AVMOProvider.parseMoviesDetail(null);
        assertTrue(detail.headers.isEmpty());
        assertTrue(detail.actresses.isEmpty());
        detail = AVMOProvider.parseMoviesDetail("{\"code\":404}");
        assertTrue(detail.headers.isEmpty());
    }

    @Test
    public void parseGenresEmpty() {
        assertTrue(AVMOProvider.parseGenres(null).isEmpty());
        assertTrue(AVMOProvider.parseGenres("{\"code\":200,\"data\":{}}").isEmpty());
    }
}
