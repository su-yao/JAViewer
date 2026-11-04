package io.github.javiewer.network;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Project: JAViewer
 */
public interface BasicService {

    String LANGUAGE_NODE = "/cn";
    String LANG = "cn";
    int PAGE_SIZE = 60;
    int STAR_PAGE_SIZE = 50;

    @Headers("Content-Type: application/json")
    @POST("getMovies")
    Call<ResponseBody> getMovies(@Body RequestBody body);

    @Headers("Content-Type: application/json")
    @POST("getFilterMovies")
    Call<ResponseBody> getFilterMovies(@Body RequestBody body);

    @Headers("Content-Type: application/json")
    @POST("getStars")
    Call<ResponseBody> getStars(@Body RequestBody body);

    @Headers("Content-Type: application/json")
    @POST("getGenres")
    Call<ResponseBody> getGenres(@Body RequestBody body);

    @Headers("Content-Type: application/json")
    @POST("getMovie")
    Call<ResponseBody> getMovie(@Body RequestBody body);

    @Headers("Content-Type: application/json")
    @POST("search")
    Call<ResponseBody> search(@Body RequestBody body);

    @GET
    Call<ResponseBody> get(@Url String url);

}
