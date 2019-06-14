package com.example.waitara_mumbi.barcodereader;

import retrofit2.Call;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface TicketClient {
    @Headers("Accept: application/json")
    @POST("events/oauth/token")// POST request (with the endpoint in brackets) when this method is called
    @FormUrlEncoded //ndicate that the request will have its MIME type (a header field that identifies the format of the body of an HTTP request or response)
    Call<Authentication> getAccesstoken (
            @Field("client_id")String clientId,//@Field("key") annotation has a parameter name that matches the name that the API expects
            @Field("client_secret") String clientSecret,
            @Field("grant_type") String grantType,
            @Field("username") String username,
            @Field("password") String password
    );

    @Headers("Accept: application/json")
    @POST("events/api/check")
    @FormUrlEncoded
    Call<CheckTicket> checkTicket(@Field("ticket_no") String ticket_no);

    @Headers("Accept: application/json")
    @POST("events/api/mark_ticket")
    @FormUrlEncoded
    Call<ResponseBody> markTicket(@Field("ticket_no")String ticket_no);
}
