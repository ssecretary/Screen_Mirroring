package com.screen.mirroring.casttotv.tv.cast.screencast.retrofit;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

   public String search_url="search?output=toolbar&";

    @GET(search_url)
    Call<ResponseBody> getSearchUrl(@Query("q") String strSearch);

}
