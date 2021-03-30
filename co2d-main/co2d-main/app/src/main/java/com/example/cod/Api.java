package com.example.cod;

import java.time.OffsetDateTime;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface Api {

    String BASE_URL = "https://eu1.cloud.thethings.network/api/v3/as/applications/esp32-unirostock/devices/heltec-lora-esp32-1/";
    String AUTHORIZATION_TOKEN = "NNSXS.6XOXTZLP4F7WN35AFIG4DPXUC7PR4TFS4FRDOCA.QGOYS3D6EJDIRGZQB5KIZUME6C26H3IROA72XYUGVRYLVIYY4BBQ";


    @GET("packages/storage/uplink_message")
    Call<ResponseBody> getCensorData(@Query("limit") int limit, @Query("order") String order, @Query("field_mask") String field_mask);
}
