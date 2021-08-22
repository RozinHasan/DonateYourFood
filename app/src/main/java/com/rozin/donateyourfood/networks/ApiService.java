package com.rozin.donateyourfood.networks;


import com.rozin.donateyourfood.models.DeviceListModel;
import com.rozin.donateyourfood.models.FirebaseResponse;
import com.rozin.donateyourfood.models.PushRawModel;
import com.rozin.donateyourfood.models.rawPushModel;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {


    @GET("GetRegisteredDevices.php")
    Call<DeviceListModel> getAllDevices();




    @Headers("Content-Type: application/json")
    @POST("sendMultiplePush.php")
    Call<FirebaseResponse> sendMultiplePush(
            @Body rawPushModel body
    );

    @Headers("Content-Type: application/json")
    @POST("pushNotification/public/api/push")
    Call<ResponseBody> sendPushNotification(
            @Body PushRawModel body
    );



//    @FormUrlEncoded
//    @POST("sendMultiplePush.php")
//    Call<FirebaseResponse> sendMultiplePush(
//            @Field("title") String title,
//            @Field("message") String message,
//            @Field("iconUrl") String iconUrl
//    );
//

}
