package jp.ac.meijou.android.cyc;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jp.ac.meijou.android.cyc.databinding.ActivityPhotoListBinding;

public class PhotoListActivity extends AppCompatActivity {

    private ActivityPhotoListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPhotoListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.groupTops.setOnClickListener(view -> {
            Log.d("test", "TopsButton was clicked.");
        });

        binding.groupBottoms.setOnClickListener(view ->{
            Log.d("test", "BottomsButton was clicked.");
        });

        binding.groupOuter.setOnClickListener(view ->{
            Log.d("test", "OuterButton was clicked.");
        });
    }
}