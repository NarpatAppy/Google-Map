package com.example.appy_sales.googlemap;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.appy_sales.googlemap.model.Example;
import com.example.appy_sales.googlemap.model.LocationModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    SupportMapFragment mapFragment;
    GoogleMap mMap;
    LatLng origin;
    LatLng dest;
    Polyline line;
    ArrayList<LatLng> locationlArrayList;
    TextView ShowDistanceDuration;
    MarkerOptions options;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private  String locationId;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        ShowDistanceDuration = findViewById(R.id.show_distance_time);

        locationlArrayList = new ArrayList<>();

        //firebase instance
        mFirebaseInstance = FirebaseDatabase.getInstance();
        //get reference to location node
        mFirebaseDatabase = mFirebaseInstance.getReference("Location");

        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        options =new MarkerOptions();
        mMap = googleMap;
        mFirebaseDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                locationId = mFirebaseDatabase.push().getKey();
                LocationModel location = dataSnapshot.getValue(LocationModel.class);

                Double lat = Double.parseDouble(location.latitude);
                Double lng = Double.parseDouble(location.longitude);

                Log.e("Latitude value==", "==========>" + lat + "  ========longitude value=======>" + lng);
                LatLng newLocation = new LatLng(lat, lng);

                locationlArrayList.add(newLocation);

                origin = locationlArrayList.get(0);
                dest = locationlArrayList.get(locationlArrayList.size()-1);

                Log.e("Array List", "value: " + locationlArrayList);
                Log.e("origin","========================================================>"+origin);
                Log.e("dest","========================================================>"+dest);

             //   addPoly(mMap);
                //Marker for coordinate
//                mMap.addMarker(options
//                        .position(newLocation)
//                        .title(String.valueOf(lat)+","+String.valueOf(lng)));


                mMap.addMarker(options
                                .position(new LatLng(origin.latitude, origin.longitude))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                .title(String.valueOf(lat)+","+String.valueOf(lng))
                );



                mMap.addMarker(options
                                .position(new LatLng(dest.latitude,dest.longitude))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                .title(String.valueOf(lat)+","+String.valueOf(lng))
                );

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));



            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });





        Button btnDriving = findViewById(R.id.btnDriving);

        btnDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                build_retrofit_and_get_response("driving");
            }
        });
        Button btnWalk = findViewById(R.id.btnWalk);
        btnWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                build_retrofit_and_get_response("walking");
            }
        });

    }



    //polyline  create polyline from 1st point to next point and continue to end point
 /*   private void addPoly(GoogleMap googleMap){
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int z = 0; z < locationlArrayList.size(); z++) {
            Log.e(" arrayOfLocation",""+locationlArrayList.get(z));
            LatLng point = locationlArrayList.get(z);
            options.add(point);
        }
        line = googleMap.addPolyline(options);
    }*/


    private void build_retrofit_and_get_response(String type){
        String url="https://maps.googleapis.com/maps/";
        Log.e("call Function","=============build_retrofit_and_get_response");

        Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(url)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

        RetrofitMaps service = retrofit.create(RetrofitMaps.class);

       Call<Example> call = service.getDistanceDuration("metric", origin.latitude + ","+origin.longitude,dest.latitude+","+dest.longitude,type);

       Log.e("Metric","==========>");
       Log.e("origin.latitude","==========>"+origin.latitude);
       Log.e("origin.longitude","==========>"+origin.longitude);
       Log.e("dest.latitude","==========>"+dest.latitude);
       Log.e("dest.longitude","==========>"+dest.longitude);
       Log.e("type","==========>"+type);


        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Response<Example> response, Retrofit retrofit) {

                try {
                    //remove previous line from map
                    if (line != null){
                        line.remove();
                    }
                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i<response.body().getRoutes().size();i++) {

                        String distance = response.body().getRoutes().get(i).getLegs().get(i).getDistance().getText();

                        String time = response.body().getRoutes().get(i).getLegs().get(i).getDuration().getText();

                        ShowDistanceDuration.setText("Distance:" + distance + "\n"+"Duration:" + time);

                        String encodedString = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();

                        List<LatLng> list = decodePoly(encodedString);
                        line = mMap.addPolyline(new PolylineOptions()
                                .addAll(list)
                                .width(20)
                                .color(Color.GREEN)
                                .geodesic(true)
                        );
                    }
                }catch (Exception e){
                    Log.e("onResponse  ","There is a Error============================>");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {

                Log.e("onFailure",t.toString());
            }
        });




    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }


}


