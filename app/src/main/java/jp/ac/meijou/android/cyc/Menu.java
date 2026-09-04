package jp.ac.meijou.android.cyc;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jp.ac.meijou.android.cyc.databinding.ActivityMenuBinding;

public class Menu extends AppCompatActivity {

    private ActivityMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //画面遷移(weather)
        binding.b1.setOnClickListener(view->{
            var intent = new Intent(this,MainActivityWeather.class);
            startActivity(intent);
        });

        //画面遷移(picture)
        binding.b1.setOnClickListener(view->{
            var intent = new Intent(this,PhotoListActivity.class);
            startActivity(intent);
        });

        //画面遷移(camera)
        binding.b1.setOnClickListener(view->{
            var intent = new Intent(this,camera.class);
            startActivity(intent);
        });
    }
}