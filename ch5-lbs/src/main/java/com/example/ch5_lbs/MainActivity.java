package com.example.ch5_lbs;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback //서버에서 실제 지도가 넘어온 순간 MAP 객체 획득
{

    TextView providerTextView;
    ImageView onOffImageView;
    TextView timeTextView;
    TextView locationTextView;
    TextView accuracyTextView;

    GoogleApiClient googleApiClient;//localtion provider connect, callback
    FusedLocationProviderApi fusedLocationProviderApi;//실제 위치 획득

    GoogleMap map;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        providerTextView = (TextView) findViewById(R.id.txt_location_provider);
        onOffImageView = (ImageView) findViewById(R.id.img_location_on_off);
        timeTextView = (TextView) findViewById(R.id.gps_time);
        locationTextView = (TextView) findViewById(R.id.gps_location);
        accuracyTextView = (TextView) findViewById(R.id.gps_accuracy);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        fusedLocationProviderApi = LocationServices.FusedLocationApi;
    }

    private void toast(String msg) {
        Toast t = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        t.show();
    }

    private String getDateTime(long time) {
        if (time == 0)
            return "";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return formatter.format(new java.util.Date(time));
    }

    private String convertDouble(double input) {
        DecimalFormat format = new DecimalFormat(".######");
        return format.format(input);
    }

    //위치 획득후 호출되어 화면에 다양한 정보 출력..
    private void updateInfo(Location location) {
        onOffImageView.setImageResource(R.drawable.on);
        timeTextView.setText(getDateTime(location.getTime()));
        locationTextView.setText("LAT: " + convertDouble(location.getLatitude()) + ", LNG:" + convertDouble((location.getLongitude())));
        accuracyTextView.setText(location.getAccuracy() + "m");
    }

    //위치 정보 획득후 호출되어 지도 제어..
    private void showMap(Location location) {
        //center 이동
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition position = new CameraPosition.Builder().target(latLng).zoom(16f).build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(position));

        //marker 올리자..
        map.clear(); //이전 마커 지우고
        map.addMarker(
                new MarkerOptions()
                        .position(latLng)
                        .title("MyLocation")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //location provider 이용시도..
        googleApiClient.connect();//결과는 callback 으로..
        if(map == null) {
            ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map_view)).getMapAsync(this);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //provider 이용 가능...
        Location location = fusedLocationProviderApi.getLastLocation(googleApiClient);

        if (location != null) {
            updateInfo(location);
            showMap(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        toast("onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        toast("onConnectionFailed");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        UiSettings settings = map.getUiSettings();
        settings.setZoomControlsEnabled(true);
    }
}