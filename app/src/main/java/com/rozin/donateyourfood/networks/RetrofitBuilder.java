package com.rozin.donateyourfood.networks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitBuilder {
    private static Retrofit retrofit = null;

    private static Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    OkHttpClient client = new OkHttpClient().newBuilder()
            .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    final Request original = chain.request();

                    final Request authorized = original.newBuilder()
                            .addHeader("Cookie", "cookie-name=cookie-value")
                            .build();

                    return chain.proceed(authorized);
                }
            })
            .build();

    private RetrofitBuilder() {
    }

    // builds OkHttpClient with logging Interceptor
    private static OkHttpClient buildClient() {
        return new OkHttpClient
                .Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
    }
    // builds OkHttpClient with logging Interceptor
    private static OkHttpClient buildClient2() {
        return new OkHttpClient
                .Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        final Request original = chain.request();
                        final Request authorized = original.newBuilder()
                                .addHeader("Cookie", "__test=70c0687072200b46b26437267d228070; expires=Friday, January 1, 2038 at 5:55:55 AM; path=/")
                                .build();

                        return chain.proceed(authorized);
                    }
                })
                .build();
    }

    public static Retrofit buildRetrofit() {
        if (retrofit == null) {
            synchronized (RetrofitBuilder.class) {
                if (retrofit == null) {
                    retrofit = new Retrofit.Builder()
                            .client(buildClient())
                            .baseUrl("http://103.54.150.38/")
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }

        return retrofit;
    }

    private void byetHostCall(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://donateyourfood.synergize.co/GetRegisteredDevices.php")
                .addHeader("Cookie", "__test=70c0687072200b46b26437267d228070; expires=Friday, January 1, 2038 at 5:55:55 AM; path=/")
                .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                JSONArray array = new JSONArray(response.body().string());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


//    public static Retrofit buildEventRetrofit() {
//        if (retrofit == null) {
//            synchronized (RetrofitBuilder.class) {
//                if (retrofit == null) {
//                    retrofit = new Retrofit.Builder()
//                            .client(buildClient())
//                            .baseUrl(EVENT_BASE_URL)
//                            .addConverterFactory(GsonConverterFactory.create(gson))
//                            .build();
//                }
//            }
//        }
//
//        return retrofit;
//    }

}