package io.github.javiewer.network;

import java.util.Map;

import io.github.javiewer.JAViewer;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface BtSearch {

    String BASE_URL = "https://sukebei.nyaa.si/";

    BtSearch INSTANCE = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(JAViewer.HTTP_CLIENT)
            .build()
            .create(BtSearch.class);

    @GET
    Call<ResponseBody> get(@Url String url, @Header("Referer") String referer);

    @FormUrlEncoded
    @POST
    Call<ResponseBody> post(@Url String url, @FieldMap Map<String, String> fields, @Header("Referer") String referer);
}
