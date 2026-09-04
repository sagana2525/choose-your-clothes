package jp.ac.meijou.android.cyc;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.Optional;

import jp.ac.meijou.android.cyc.databinding.ActivityMainWeatherBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivityWeather extends AppCompatActivity {

    private ActivityMainWeatherBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final Moshi moshi = new Moshi.Builder().build();
    private final JsonAdapter<Gist> gistJsonAdapter = moshi.adapter(Gist.class);


    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainWeatherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        binding.buttonSelectLocation.setOnClickListener(view ->
        {
            binding.buttonSelect.setVisibility(VISIBLE);
            binding.spinnerCity.setVisibility(VISIBLE);
            binding.spinnerPrefectures.setVisibility(VISIBLE);
            binding.buttonCurrentLocation.setVisibility(INVISIBLE);
            Spinner spinnerPrefectures = (Spinner) findViewById(R.id.spinnerPrefectures);
            ArrayAdapter<CharSequence> adapterPrefectures = ArrayAdapter.createFromResource(
                    this,
                    R.array.prefectures_array,
                    android.R.layout.simple_spinner_item

            );

            adapterPrefectures.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPrefectures.setAdapter(adapterPrefectures);

//            spinnerPrefectures.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        });

        binding.buttonCurrentLocation.setOnClickListener(view -> {
            binding.buttonSelectLocation.setVisibility(INVISIBLE);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(location != null){
                                var lat = location.getLatitude();
                                var lon = location.getLongitude();

                                var text = getNameLocation(lat, lon);
                            }
                            Log.d("test", "onSuccess:");
                        }
                    });
        });
    }

    protected String getNameLocation(double lat, double lon){
        String name = null;
        var request = new Request.Builder()
                .url("https://mreversegeocoder.gsi.go.jp/reverse-geocoder/LonLatToAddress?lat=" + lat + "&lon=" + lon)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                var gist = gistJsonAdapter.fromJson(response.body().source());
                Optional.ofNullable(gist)
                        .map(g -> g.files.get("lv01Nm"))
                        .ifPresent(gistFile -> {
                            // UIスレッド以外䛷更新する䛸クラッシュする䛾䛷、UIスレッド上䛷実行させる
                            runOnUiThread(() -> binding.textNameLocation.setText(gistFile.content));
                        });
            }
        });

        return name;
    }

}