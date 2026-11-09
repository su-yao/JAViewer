package io.github.javiewer;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import cn.jzvd.JZVideoPlayer;
import io.github.javiewer.adapter.item.DataSource;
import io.github.javiewer.fragment.ActressesFragment;
import io.github.javiewer.fragment.HomeFragment;
import io.github.javiewer.fragment.PopularFragment;
import io.github.javiewer.fragment.ReleasedFragment;
import io.github.javiewer.fragment.favourite.FavouriteTabsFragment;
import io.github.javiewer.fragment.genre.GenreTabsFragment;
import io.github.javiewer.network.BasicService;
import io.github.javiewer.util.ExoPlayerImpl;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

/**
 * Project: JAViewer
 */

public class JAViewer extends Application {

    public static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.230 Mobile Safari/537.36";
    public static final List<DataSource> DATA_SOURCES = new ArrayList<>();
    public static final Map<Integer, Class<? extends Fragment>> FRAGMENTS = new HashMap<Integer, Class<? extends Fragment>>() {{
        put(R.id.nav_home, HomeFragment.class);
        put(R.id.nav_popular, PopularFragment.class);
        put(R.id.nav_released, ReleasedFragment.class);
        put(R.id.nav_actresses, ActressesFragment.class);
        put(R.id.nav_genre, GenreTabsFragment.class);
        put(R.id.nav_favourite, FavouriteTabsFragment.class);
    }};
    public static Configurations CONFIGURATIONS;
    public static BasicService SERVICE;
    public static Map<String, String> hostReplacements = new HashMap<>();
    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request original = chain.request();

            HttpUrl url = replaceUrl(original.url());
            Request.Builder builder = original.newBuilder()
                    .url(url)
                    .header("User-Agent", USER_AGENT);
            String host = url.host();
            if (host.contains("avmoo") || host.contains("avsox") || host.contains("avheat")) {
                String origin = url.scheme() + "://" + host;
                builder.header("X-Requested-With", "XMLHttpRequest")
                        .header("Accept", "application/json, text/plain, */*")
                        .header("Origin", origin)
                        .header("Referer", origin + "/cn");
            }
            Request request = builder.build();

            try {
                return chain.proceed(request);
            } catch (SSLException e) {
                HttpUrl retryUrl = url.newBuilder().scheme("http").build();
                if ("https".equals(url.scheme()) && isAvmooFamily(retryUrl.host()) && !retryUrl.equals(url)) {
                    return chain.proceed(request.newBuilder().url(retryUrl).build());
                }
                throw e;
            }
        }
    })
            .cookieJar(new CookieJar() {
                private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    cookieStore.put(url, cookies);
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    List<Cookie> cookies = cookieStore.get(url);
                    return cookies != null ? cookies : new ArrayList<Cookie>();
                }
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    static {
        JZVideoPlayer.setMediaInterface(new ExoPlayerImpl());
    }

    public static DataSource getDataSource() {
        return JAViewer.CONFIGURATIONS.getDataSource();
    }

    public static void recreateService() {
        SERVICE = new Retrofit.Builder()
                .baseUrl(JAViewer.getDataSource().getLink())
                .client(JAViewer.HTTP_CLIENT)
                .build()
                .create(BasicService.class);
    }

    public static File getStorageDir() {
        File dir = new File(Environment.getExternalStorageDirectory(), "JAViewer/");
        dir.mkdirs();
        return dir;
    }

    public static HttpUrl replaceUrl(HttpUrl url) {
        HttpUrl.Builder builder = url.newBuilder();
        String host = url.url().getHost();
        if (hostReplacements.containsKey(host)) {
            builder.host(hostReplacements.get(host));
            host = hostReplacements.get(host);
        }
        if (isAvmooFamily(host) && "https".equals(url.scheme())) {
            builder.scheme("http");
        }
        return builder.build();
    }

    private static boolean isAvmooFamily(String host) {
        if (host == null) {
            return false;
        }
        return host.contains("avmoo") || host.contains("avsox") || host.contains("avheat") || host.contains("tellme.pw");
    }

    public static <T> T parseJson(Class<T> beanClass, JsonReader reader) throws JsonParseException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(reader, beanClass);
    }

    public static <T> T parseJson(Class<T> beanClass, String json) throws JsonParseException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(json, beanClass);
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static boolean Objects_equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static void a(Context context) {
        io.github.javiewer.util.Intents.openUri(context, "https://qr.alipay.com/a6x05027ymf6n8kl0qkoa54");
    }

    public static String b(String s1, String s2) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(String.format("%s%sBrynhildr", s1, s2).getBytes());
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
