package io.github.javiewer.network;

import io.github.javiewer.JAViewer;
import io.github.javiewer.network.item.WhatsLinkResult;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface WhatsLink {

    String BASE_URL = "https://whatslink.info/";

    WhatsLink INSTANCE = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(JAViewer.HTTP_CLIENT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WhatsLink.class);

    @GET("api/v1/link")
    @Headers({
            "Accept: application/json, text/plain, */*",
            "Referer: https://whatslink.info/"
    })
    Call<WhatsLinkResult> preview(@Query("url") String magnet);
}
