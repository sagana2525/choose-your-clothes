package jp.ac.meijou.android.cyc;

import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.ac.meijou.android.cyc.databinding.ActivityMainWeatherBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivityWeather extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ActivityMainWeatherBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final Moshi moshi = new Moshi.Builder().build();
    private final JsonAdapter<HeartRailsResponse> heartRailsAdapter = moshi.adapter(HeartRailsResponse.class);
    private final JsonAdapter<List<JmaResponse>> jmaAdapter;

    public MainActivityWeather() {
        Type type = Types.newParameterizedType(List.class, JmaResponse.class);
        jmaAdapter = moshi.adapter(type);
    }

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    // 精密な位置情報の権限がある場合
                    updateCurrentLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    // おおよその位置情報の権限がある場合
                    updateCurrentLocation();
                } else {
                    // 権限が拒否された場合
                    Toast.makeText(this, "位置情報の権限が必要です", Toast.LENGTH_SHORT).show();
                }
            });

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

        binding.buttonCurrentLocation.setOnClickListener(view -> checkLocationPermission());

        binding.buttonSelectLocation.setOnClickListener(view ->
        {
            binding.buttonSelect.setVisibility(VISIBLE);
            binding.spinnerCity.setVisibility(VISIBLE);
            binding.spinnerPrefectures.setVisibility(VISIBLE);
            Spinner spinnerPrefectures = (Spinner) findViewById(R.id.spinnerPrefectures);
            ArrayAdapter<CharSequence> adapterPrefectures = ArrayAdapter.createFromResource(
                    this,
                    R.array.prefectures_array,
                    android.R.layout.simple_spinner_item

            );

            adapterPrefectures.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPrefectures.setAdapter(adapterPrefectures);

            spinnerPrefectures.setOnItemSelectedListener(this);
        });

        binding.buttonSelect.setOnClickListener(view -> {
            String selectedPref = binding.spinnerPrefectures.getSelectedItem().toString();
            String selectedCity = binding.spinnerCity.getSelectedItem() != null ? 
                    binding.spinnerCity.getSelectedItem().toString() : "";
            binding.textNameLocation.setText(selectedPref + selectedCity);
            
            String areaCode = getJmaAreaCode(selectedPref);
            if (!areaCode.isEmpty()) {
                fetchWeather(areaCode);
            }
        });
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 権限がない場合はリクエストする
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            // すでに権限がある場合
            updateCurrentLocation();
        }
    }

    private void updateCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // 最新の現在地を取得するリクエスト (PRIORITY_HIGH_ACCURACY を使用)
        fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null // キャンセルトークンは今回は不要
        ).addOnSuccessListener(this, location -> {
            if (location != null) {
                // 座標が取得できたら住所を取得
                fetchAddress(location.getLatitude(), location.getLongitude());
                Toast.makeText(this, "現在地を取得しました", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "位置情報を取得できませんでした。エミュレータのLocation設定で'SET LOCATION'を押したか確認してください。", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "エラー: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchAddress(double lat, double lon) {
        executor.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(MainActivityWeather.this, Locale.JAPAN);
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);

                    // 都道府県 + 市区町村 + 町名を連結
                    StringBuilder sb = new StringBuilder();
                    if (address.getAdminArea() != null) sb.append(address.getAdminArea());
                    if (address.getLocality() != null) sb.append(address.getLocality());
                    if (address.getSubLocality() != null) sb.append(address.getSubLocality());
                    if (address.getThoroughfare() != null) sb.append(address.getThoroughfare());

                    String addressText = sb.toString();
                    runOnUiThread(() -> {
                        binding.textNameLocation.setText(addressText);
                        String prefName = address.getAdminArea();
                        String areaCode = getJmaAreaCode(prefName);
                        if (!areaCode.isEmpty()) {
                            fetchWeather(areaCode);
                        }
                    });
                }
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivityWeather.this, "住所取得に失敗しました", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown(); // スレッドプールの破棄
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spinnerPrefectures) {
            String selectedPref = parent.getItemAtPosition(position).toString();
            fetchCities(selectedPref);
        }
    }

    private void fetchCities(String prefecture) {
        String url = "https://geoapi.heartrails.com/api/json?method=getCities&prefecture=" + prefecture;

        Request request = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivityWeather.this, "市町村の取得に失敗しました", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    HeartRailsResponse hrResponse = heartRailsAdapter.fromJson(response.body().source());
                    if (hrResponse != null && hrResponse.response != null && hrResponse.response.location != null) {
                        List<String> cityList = new ArrayList<>();
                        for (HeartRailsResponse.Location loc : hrResponse.response.location) {
                            cityList.add(loc.city);
                        }
                        runOnUiThread(() -> updateCitySpinner(cityList));
                    }
                }
            }
        });
    }

    private void updateCitySpinner(List<String> cities) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCity.setAdapter(adapter);
        binding.spinnerCity.setVisibility(VISIBLE);
    }

    private void fetchWeather(String areaCode) {
        String url = "https://www.jma.go.jp/bosai/forecast/data/forecast/" + areaCode + ".json";

        Request request = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivityWeather.this, "天気情報の取得に失敗しました", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    List<JmaResponse> jmaResponses = jmaAdapter.fromJson(response.body().source());
                    if (jmaResponses != null && !jmaResponses.isEmpty()) {
                        JmaResponse data = jmaResponses.get(0);
                        runOnUiThread(() -> updateWeatherUI(data));
                    }
                }
            }
        });
    }

    private void updateWeatherUI(JmaResponse data) {
        try {
            // 気象庁のデータから今日の情報を抽出
            // timeSeries[0]: 天気
            // timeSeries[1]: 降水確率
            // timeSeries[2]: 気温 (一部の地域のみ)
            
            JmaResponse.Area weatherArea = data.timeSeries.get(0).areas.get(0);
            String weather = weatherArea.weathers.get(0);
            
            JmaResponse.Area popArea = data.timeSeries.get(1).areas.get(0);
            String pop = popArea.pops.get(0) + "%";
            
            binding.textViewWeather.setText(weather);
            binding.textRainfall.setText(pop);
            
            // 気温の取得 (データがある場合)
            if (data.timeSeries.size() > 2) {
                JmaResponse.Area tempArea = data.timeSeries.get(2).areas.get(0);
                if (tempArea.temps != null && !tempArea.temps.isEmpty()) {
                    binding.textTemparature.setText(tempArea.temps.get(0) + "℃");
                }
            }
            
            // 天気に合わせて画像を変更（簡易実装）
            if (weather.contains("晴")) {
                binding.imageWeather.setImageResource(android.R.drawable.ic_menu_day);
            } else if (weather.contains("雨")) {
                binding.imageWeather.setImageResource(android.R.drawable.ic_menu_send);
            } else if (weather.contains("曇")) {
                binding.imageWeather.setImageResource(android.R.drawable.ic_menu_recent_history);
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "天気データの解析に失敗しました", Toast.LENGTH_SHORT).show();
        }
    }

    private String getJmaAreaCode(String prefName) {
        if (prefName == null) return "";
        Map<String, String> map = new HashMap<>();
        map.put("北海道", "016000"); map.put("青森県", "020000"); map.put("岩手県", "030000");
        map.put("宮城県", "040000"); map.put("秋田県", "050000"); map.put("山形県", "060000");
        map.put("福島県", "070000"); map.put("茨城県", "080000"); map.put("栃木県", "090000");
        map.put("群馬県", "100000"); map.put("埼玉県", "110000"); map.put("千葉県", "120000");
        map.put("東京都", "130000"); map.put("神奈川県", "140000"); map.put("新潟県", "150000");
        map.put("富山県", "160000"); map.put("石川県", "170000"); map.put("福井県", "180000");
        map.put("山梨県", "190000"); map.put("長野県", "200000"); map.put("岐阜県", "210000");
        map.put("静岡県", "220000"); map.put("愛知県", "230000"); map.put("三重県", "240000");
        map.put("滋賀県", "250000"); map.put("京都府", "260000"); map.put("大阪府", "270000");
        map.put("兵庫県", "280000"); map.put("奈良県", "290000"); map.put("和歌山県", "300000");
        map.put("鳥取県", "310000"); map.put("島根県", "320000"); map.put("岡山県", "330000");
        map.put("広島県", "340000"); map.put("山口県", "350000"); map.put("徳島県", "360000");
        map.put("香川県", "370000"); map.put("愛媛県", "380000"); map.put("高知県", "390000");
        map.put("福岡県", "400000"); map.put("佐賀県", "410000"); map.put("長崎県", "420000");
        map.put("熊本県", "430000"); map.put("大分県", "440000"); map.put("宮崎県", "450000");
        map.put("鹿児島県", "460100"); map.put("沖縄県", "471000");

        for (String key : map.keySet()) {
            if (prefName.contains(key)) {
                return map.get(key);
            }
        }
        return "";
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

}