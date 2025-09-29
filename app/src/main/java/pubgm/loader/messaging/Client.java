package pubgm.loader.messaging;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class Client {

    private static Retrofit retrofit = null;

    public static Retrofit getClient(String url){
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
