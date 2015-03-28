package com.alangeorge.android.bloodhound;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LATITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_LONGITUDE;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_COLUMN_TIME_STRING;
import static com.alangeorge.android.bloodhound.model.provider.LocationContentProvider.LOCATIONS_CONTENT_URI;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class LocationUpdateIntentService extends IntentService {
    private static final String TAG = "LocationUpdateIntntSrvc";

    public LocationUpdateIntentService() {
        super("LocationUpdateIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent(" + intent + ")");
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            Location location = bundle.getParcelable(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
            recordLocation(location);
            postLocationToServer(location);
        }
    }

    private void postLocationToServer(Location location) {
        Log.d(TAG, "postLocationToServer()");
        if (!App.getGcmRegistrationId().isEmpty()) {

            LocationUpdateRequest request = new LocationUpdateRequest();
            request.setDeviceId(App.getGcmRegistrationId());
            request.setLatitude(location.getLatitude());
            request.setLongitude(location.getLongitude());
            request.setAccuracy(location.getAccuracy());

            GsonBuilder builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation();
            builder.setDateFormat(DateFormat.LONG);
            builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    return new Date(json.getAsJsonPrimitive().getAsLong());
                }
            });

            builder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                @Override
                public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
                    return src == null ? null : new JsonPrimitive(src.getTime());
                }
            });

            Gson gson = builder.create();

            String requestJson = gson.toJson(request);

            Log.d(TAG, "requestJson: " + requestJson);

            StringBuilder stringBuilder = new StringBuilder();

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
            HttpConnectionParams.setSoTimeout(httpParams, 5000);
            HttpClient httpClient = new DefaultHttpClient(httpParams);

            HttpPost httpPost = new HttpPost(getString(R.string.location_server_url));
            ByteArrayEntity postEntity = new ByteArrayEntity(requestJson.getBytes());
            postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpPost.setEntity(postEntity);

            try {
                HttpResponse response = httpClient.execute(httpPost);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream inputStream = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    inputStream.close();
                } else {
                    Log.d(TAG, "Failed on location update JSON post: http status = " + statusCode);
                    return;
                }
            } catch (Exception e) {
                Log.d(TAG, "Failed on location update JSON post: " + e.getLocalizedMessage());
                return;
            }

            // {"result":"success"}  or  {"result":"failed"}
            Log.d(TAG, "JSON returned: " + stringBuilder.toString());
            Map<String, String> response = gson.fromJson(stringBuilder.toString(), new TypeToken<Map<String, String>>() {
            }.getType());

            Log.d(TAG, "response:" + response);
        } else {
            Log.e(TAG, "unable to post location: gcm id not set");
        }
    }

    private void recordLocation(Location location) {
        Date now = new Date();

        ContentValues values = new ContentValues();
        values.put(LOCATIONS_COLUMN_LATITUDE, location.getLatitude());
        values.put(LOCATIONS_COLUMN_LONGITUDE, location.getLongitude());
        values.put(LOCATIONS_COLUMN_TIME, now.getTime());
        values.put(LOCATIONS_COLUMN_TIME_STRING, now.toString());

        App.context.getContentResolver().insert(LOCATIONS_CONTENT_URI, values);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class LocationUpdateRequest {
        @Expose
        private String deviceId;
        @Expose
        private double latitude;
        @Expose
        private double longitude;
        @Expose
        private double accuracy;

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getAccuracy() {
            return accuracy;
        }

        public void setAccuracy(double accuracy) {
            this.accuracy = accuracy;
        }

        @Override
        public String toString() {
            return "LocationUpdateRequest{" +
                    "deviceId='" + deviceId + '\'' +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", accuracy=" + accuracy +
                    '}';
        }
    }
}
