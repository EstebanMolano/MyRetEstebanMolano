package com.example.myret;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.location.Location;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private static final int REQUEST_CODE = 11 ;

    private GoogleMap mMap;
    private LocationManager manager;
    private Marker me;
    private ArrayList<Marker> marcadores;
    private Button btn_agregar;
    private EditText txt_nombrar;
    private TextView  txt_nombreLugar;
    public Dialog dialog;
    private TextView txt_lugarCercano;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        marcadores = new ArrayList<>();
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);


        txt_lugarCercano = findViewById(R.id.txt_lugarCercano);


        dialog = new Dialog(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_CODE);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(MapsActivity.this,"Hola", Toast.LENGTH_SHORT).show();
                //Using position get Value from arraylist
                if(marker.getId().equals(me.getId())) {

                    String msj = "Dirección: ";

                    try {
                        Geocoder geo = new Geocoder(MapsActivity.this.getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = geo.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                        if (addresses.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Esperando por la dirección...", Toast.LENGTH_LONG).show();
                        } else {
                            if (addresses.size() > 0) {
                                marker.setSnippet(msj + addresses.get(0).getAddressLine(0) + addresses.get(0).getAdminArea());

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // getFromLocation() may sometimes fail
                    }
                }

                else{
                    Location local = new Location("local");
                    local.setLatitude(me.getPosition().latitude);
                    local.setLongitude(me.getPosition().longitude);

                    Location local1 = new Location("local1");
                    local1.setLatitude(marker.getPosition().latitude);
                    local1.setLongitude(marker.getPosition().longitude);

                    float xd =local.distanceTo(local1);
                    marker.setSnippet("Este marcador esta a : " + xd);

                }


                return false;
            }
        });


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                dialog.setContentView(R.layout.dialog);
                txt_nombrar = dialog.findViewById(R.id.txt_nombrar);
                txt_nombreLugar = dialog.findViewById(R.id.txt_nombreLugar);
                btn_agregar = dialog.findViewById(R.id.btnAgregar);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();


                Toast.makeText(MapsActivity.this,""+latLng.latitude +" , " + latLng.longitude, Toast.LENGTH_SHORT).show();
                marcadores.add(mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latLng.latitude, latLng.longitude))
                        .title("Nuevo ")));
                refrescarLugarCercano();

            }
        });

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e(">>>","LAT: "+location.getLatitude()+ " , LONG: "+location.getLongitude());

                if(me != null){
                    me.remove();
                }
                me = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("Me")
                );
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));

                refrescarLugarCercano();

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
    }

    public void refrescarLugarCercano(){

        if(marcadores.size() != 0) {
            Location local = new Location("local");
            local.setLatitude(me.getPosition().latitude);
            local.setLongitude(me.getPosition().longitude);

            Location local1 = new Location("local1");


            float xd = 999999999;
            float x = 0;

            String nombre = "";

            for (int i = 0; marcadores.size() > i; i++) {
                local1.setLatitude(marcadores.get(i).getPosition().latitude);
                local1.setLongitude(marcadores.get(i).getPosition().longitude);
                x = local.distanceTo(local1);
                if (xd > x) {
                    nombre = marcadores.get(i).getId();
                }
            }

            txt_lugarCercano.setText(  "El lugar mas cercano es \n" + nombre);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }



}
