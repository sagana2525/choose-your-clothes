package jp.ac.meijou.android.cyc;

import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jp.ac.meijou.android.cyc.databinding.ActivityMainWeatherBinding;

public class MainActivityWeather extends AppCompatActivity {

    private ActivityMainWeatherBinding binding;

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

            spinnerPrefectures.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        });
    }

}