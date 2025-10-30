package com.example.controllerzmq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Inisialisasi Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 🔹 Set default item (halaman Main)
        bottomNavigationView.setSelectedItemId(R.id.main);

        // 🔹 Logika berpindah halaman seperti tombol (versi barumu)
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.main1) {
                // Sudah di Main, tidak pindah Activity
                return true;

            } else if (itemId == R.id.connection1) {
                // Pindah ke halaman Connection
                Intent intent = new Intent(MainActivity.this, CONNECTION.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;

            } else if (itemId == R.id.controller1) {
                // Pindah ke halaman Controller
                Intent intent = new Intent(MainActivity.this, CONTROLLER.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
}





