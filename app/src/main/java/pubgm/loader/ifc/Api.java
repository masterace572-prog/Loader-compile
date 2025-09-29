package pubgm.loader.ifc;

import pubgm.loader.messaging.Response;
import pubgm.loader.messaging.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public interface Api {
    @Headers(
            {
                    "Content-Type:application/json",
                  //  "Authorization:key=AAAAPtHITYM:APA91bFkK42E4XGNwXpF0eKcnl8a6VX6rFG1mfVxwF9wTmjzVM5MQcC6sBUgTRcisZweWbcCeiDBFzpM2YFX8TE4pMtgw9bF5-aFVcAzPlZiy-ausbD0uVfqUdf43827qzMPRsxHNj0K"
            }
    )

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
