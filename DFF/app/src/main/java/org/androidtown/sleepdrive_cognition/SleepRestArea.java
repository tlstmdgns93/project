package org.androidtown.sleepdrive_cognition;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class SleepRestArea extends AppCompatActivity
        implements OnMapReadyCallback{

    private static final String TAG = SleepRestArea.class.getSimpleName();
    private GoogleMap mMap; //화면이동, 마커달기 등등으로 쓰이는 구글맵 변수
    private CameraPosition mCameraPosition; //좌표로 이동 변수


    private GeoDataClient mGeoDataClient; //구글 API에 접근해서 지역정보를 얻는 변수
    private PlaceDetectionClient mPlaceDetectionClient; //구글 API에 접근해서 <현재 위치> 를 얻는 변수


    private FusedLocationProviderClient mFusedLocationProviderClient;//구글 API에 접근해서 <주위정보>를 얻는 변수


    private final LatLng mDefaultLocation = new LatLng(35.05148245, 126.72306776);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // 연결이 끊겼을 때 마지막 위치를 기억
    private Location mLastKnownLocation;


    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // 저장한 위치 변수들
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);


        }

        // 지도를 렌더해서 보여줌.
        setContentView(R.layout.activity_maps);


        mGeoDataClient = Places.getGeoDataClient(this, null);


        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // 지도를 빌드
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);




    }








    //액티비티가 중지되었을때, 맵만 살아있으면, 맵변수에서 좌표정보/위치 주변정보를 번들객체에 담아둔다.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            showCurrentPlace();
        }
        return true;
    }


    //졸음쉼터 좌표
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        LatLng Korea = new LatLng(36.701788,129.727663);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Korea,7));


        LatLng MunGyeung = new LatLng(36.746571, 128.061148);
        mMap.addMarker(new MarkerOptions().position(MunGyeung).title("문경 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(MunGyeung));

        LatLng Wanju = new LatLng(35.992632, 127.095515);
        mMap.addMarker(new MarkerOptions().position(Wanju).title("완주 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Wanju));

        LatLng apo = new LatLng(36.224207, 128.266222);
        mMap.addMarker(new MarkerOptions().position(apo).title("아포 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(apo));

        LatLng gaejin = new LatLng(35.708876, 128.418763);
        mMap.addMarker(new MarkerOptions().position(gaejin).title("개진 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(gaejin));

        LatLng Changnyung= new LatLng(35.601473, 128.465836);
        mMap.addMarker(new MarkerOptions().position(Changnyung).title("창녕 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Changnyung));

        LatLng Guri= new LatLng(37.589756, 127.155895);
        mMap.addMarker(new MarkerOptions().position(Guri).title("구리 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Guri));

        LatLng Gimpo= new LatLng(37.592540, 126.768250);
        mMap.addMarker(new MarkerOptions().position(Gimpo).title("김포 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Gimpo));

        LatLng Siheng= new LatLng(37.451457, 126.804152);
        mMap.addMarker(new MarkerOptions().position(Siheng).title("시흥 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Siheng));

        LatLng Cungae= new LatLng(37.392720, 127.028435);
        mMap.addMarker(new MarkerOptions().position(Cungae).title("청계 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Cungae));

        LatLng Sungnam= new LatLng(37.436036, 127.123834);
        mMap.addMarker(new MarkerOptions().position(Sungnam).title("성남 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Sungnam));

        LatLng buncheon= new LatLng(37.453498, 127.256250);
        mMap.addMarker(new MarkerOptions().position(buncheon).title("번천 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(buncheon));

        LatLng sohuel= new LatLng(37.8287742, 127.159198);
        mMap.addMarker(new MarkerOptions().position(sohuel).title("소흘 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sohuel));

        LatLng Emok= new LatLng(37.3202623, 126.9828534);
        mMap.addMarker(new MarkerOptions().position(Emok).title("이목 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Emok));

        LatLng seoseoul= new LatLng(37.3547081, 126.8630876);
        mMap.addMarker(new MarkerOptions().position(seoseoul).title("서서울 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(seoseoul));

        LatLng jinju = new LatLng(35.1970188, 128.0292988);
        mMap.addMarker(new MarkerOptions().position(jinju).title("진주 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(jinju));

        LatLng bukchangwon = new LatLng(35.2834707, 128.6160546);
        mMap.addMarker(new MarkerOptions().position(bukchangwon).title("북창원 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bukchangwon));

        LatLng jisu= new LatLng(37.1842437,127.082306 );
        mMap.addMarker(new MarkerOptions().position(jisu).title("지수 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(jisu));

        LatLng gumsung= new LatLng(37.0646128,126.812136 );
        mMap.addMarker(new MarkerOptions().position(gumsung).title("금성 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(gumsung));

        LatLng jungangtop= new LatLng(37.0414207,127.8323553 );
        mMap.addMarker(new MarkerOptions().position(jungangtop).title("중앙탑 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(jungangtop));

        LatLng dangjin = new LatLng(36.9007504,126.7184688 );
        mMap.addMarker(new MarkerOptions().position(dangjin).title("당진 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(dangjin));

        LatLng jangan= new LatLng(37.3202623, 126.9828534);
        mMap.addMarker(new MarkerOptions().position(jangan).title("장안 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(jangan));

        LatLng ilghang= new LatLng(35.2816719,129.2064488 );
        mMap.addMarker(new MarkerOptions().position(ilghang).title("일광 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ilghang));

        LatLng gosung= new LatLng(37.1137101,126.9866505 );
        mMap.addMarker(new MarkerOptions().position(gosung).title("고성 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(gosung));

        LatLng gangjin= new LatLng(36.084437, 128.6322083 );
        mMap.addMarker(new MarkerOptions().position(gangjin).title("강진 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(gangjin));

        LatLng bulgyo = new LatLng(34.82368,  127.3811754);
        mMap.addMarker(new MarkerOptions().position(bulgyo).title("벌교 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bulgyo));

        LatLng gimhae= new LatLng(35.2208334,128.8702902 );
        mMap.addMarker(new MarkerOptions().position(gimhae).title("김해 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(gimhae));

        LatLng gumgok = new LatLng(37.1631371,127.0760963 );
        mMap.addMarker(new MarkerOptions().position(gumgok).title("금곡 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(gumgok));

        LatLng ulsan= new LatLng(35.5509757,129.169589 );
        mMap.addMarker(new MarkerOptions().position(ulsan).title("울산 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ulsan));

        LatLng samnam= new LatLng(35.5462988,129.119326 );
        mMap.addMarker(new MarkerOptions().position(samnam).title("삼남 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(samnam));

        LatLng mongtan= new LatLng(34.9458936, 126.5046266);
        mMap.addMarker(new MarkerOptions().position(mongtan).title("몽탄 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mongtan));

        LatLng sunchun = new LatLng(35.3147488,126.7933754 );
        mMap.addMarker(new MarkerOptions().position(sunchun).title("순천 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sunchun));

        LatLng jungchon= new LatLng(35.1418802,128.1107763 );
        mMap.addMarker(new MarkerOptions().position(jungchon).title("정촌 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(jungchon));

        LatLng juam= new LatLng(35.0557318, 127.2775388 );
        mMap.addMarker(new MarkerOptions().position(juam).title("주암 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(juam));

        LatLng taeryung = new LatLng(35.2489478,126.9276994 );
        mMap.addMarker(new MarkerOptions().position(taeryung).title("태령 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(taeryung));

        LatLng youngghang= new LatLng(35.2494519, 126.5189737);
        mMap.addMarker(new MarkerOptions().position(youngghang).title("영광 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(youngghang));

        LatLng damyang= new LatLng(35.260993, 126.971147);
        mMap.addMarker(new MarkerOptions().position(damyang).title("담양 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(damyang));

        LatLng daeduk= new LatLng(35.2448096, 127.0260954);
        mMap.addMarker(new MarkerOptions().position(daeduk).title("대덕 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(daeduk));

        LatLng goryung= new LatLng(35.7220935,128.2972976 );
        mMap.addMarker(new MarkerOptions().position(goryung).title("고령 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(goryung));

        LatLng ssangrim= new LatLng(35.7024289, 128.2311992);
        mMap.addMarker(new MarkerOptions().position(ssangrim).title("쌍림 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ssangrim));

        LatLng yangsan = new LatLng(35.3425061, 129.0423895);
        mMap.addMarker(new MarkerOptions().position(yangsan).title("양산 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(yangsan));

        LatLng namsang= new LatLng(35.6351238, 127.9048877);
        mMap.addMarker(new MarkerOptions().position(namsang).title("남상 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(namsang));

        LatLng mukgae= new LatLng(36.4374647,128.9247596 );
        mMap.addMarker(new MarkerOptions().position(mukgae).title("묵계 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mukgae));

        LatLng seosang = new LatLng(35.6334413,127.7140162 );
        mMap.addMarker(new MarkerOptions().position(seosang).title("서상 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(seosang));

        LatLng naejangsan= new LatLng(35.5374635,126.8137618 );
        mMap.addMarker(new MarkerOptions().position(naejangsan).title("내장산 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(naejangsan));

        LatLng myunggok = new LatLng(36.1256816,128.5419092 );
        mMap.addMarker(new MarkerOptions().position(myunggok).title("명곡 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myunggok));

        LatLng anpyung= new LatLng(36.3790414,128.5774932 );
        mMap.addMarker(new MarkerOptions().position(anpyung).title("안평 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(anpyung));

        LatLng sungi= new LatLng(36.13623, 128.3171685);
        mMap.addMarker(new MarkerOptions().position(sungi).title("선기 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sungi));

        LatLng daeghanryung= new LatLng(37.67551, 128.7196951);
        mMap.addMarker(new MarkerOptions().position(daeghanryung).title("대관령 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(daeghanryung));

        LatLng nonsan= new LatLng(36.1593982,127.1756229 );
        mMap.addMarker(new MarkerOptions().position(nonsan).title("논산 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(nonsan));

        LatLng seodaejeon= new LatLng(36.3153582,  127.3067961);
        mMap.addMarker(new MarkerOptions().position(seodaejeon).title("서대전 졸음쉼터"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(seodaejeon));


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override

            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        // 유저에게 위치정보 요청
        getLocationPermission();


        updateLocationUI();


        getDeviceLocation();
    }

    //GPS 사용유무
    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(SleepRestArea.this);
        alertDialog.setTitle("GPS 사용유무셋팅");
        alertDialog.setMessage("GPS 셋팅이 되지 않았을수도 있습니다.\n 설정창으로 가시겠습니까?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        (SleepRestArea.this).startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }




    private void getDeviceLocation() {
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Log.d("S2","이퓨");
                    showSettingsAlert();

                    return false;
                } else {
                    return false;
                }
            }
        });

        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {

                            mLastKnownLocation = task.getResult();
                            if(mLastKnownLocation!= null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }

    }


    //디바이스의 위치를 요청
    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    //근처의 주요 장소나 건물들을 목록으로 보여줌
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        getDeviceLocation();
        if (mLocationPermissionGranted) {

            @SuppressWarnings("MissingPermission") final
            Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();


                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {

                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }


                                likelyPlaces.release();


                                openPlacesDialog();

                            } else {
                                Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {

            Log.i(TAG, "The user did not grant location permission.");


            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));


            getLocationPermission();
        }
    }


    private void openPlacesDialog() {


        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }


                mMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));


                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };


        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }


    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


}