package com.example.controllerzmq;

import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.slider.Slider;


public class CONTROLLER extends AppCompatActivity {

//    private ImageView LAMPU;
//    private Button ON, OFF;
private ImageView tengah;
private ImageButton atas,bawah,kanan,kiri,kiriatas,kiribawah,kananatas,kananbawah,Zatas,Zbawah;

private Slider slider;
//private TextView sliderValue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_controller);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.controller), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;});


        slider=findViewById(R.id.gripper);
//        sliderValue=findViewById(R.id.sliderValue)
////        LAMPU = findViewById(R.id.indikator);
////        ON = findViewById(R.id.buttonON);
////        OFF = findViewById(R.id.buttonOFF);
        atas=findViewById(R.id.atas);
        bawah=findViewById(R.id.bawah);
        kanan=findViewById(R.id.kanan);
        kiri=findViewById(R.id.kiri);
        kiriatas=findViewById(R.id.kiriatas);
        kiribawah=findViewById(R.id.kiribawah);
        kananatas =findViewById(R.id.kananatas);
        kananbawah=findViewById(R.id.kananbawah);
        Zatas=findViewById(R.id.Zatas);
        Zbawah=findViewById(R.id.Zbawah);



//        OFF.setOnClickListener(v1 -> {
//            LAMPU.setImageResource(R.drawable.off_image);
//        });
//        ON.setOnClickListener(v1 -> {
//            LAMPU.setImageResource(R.drawable.on_image);
//        });
// 🔹 Inisialisasi Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 🔹 Set default item (halaman Main)
        bottomNavigationView.setSelectedItemId(R.id.main);

        // 🔹 Logika berpindah halaman seperti tombol (versi barumu)
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.controller1) {
                // Sudah di Main, tidak pindah Activity
                return true;

            } else if (itemId == R.id.connection1) {
                // Pindah ke halaman Connection
                Intent intent = new Intent(CONTROLLER.this, CONNECTION.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;

            } else if (itemId == R.id.main1) {
                // Pindah ke halaman Controller
                Intent intent = new Intent(CONTROLLER.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });

    }
}