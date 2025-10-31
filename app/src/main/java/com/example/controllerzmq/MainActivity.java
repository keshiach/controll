package com.example.controllerzmq;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.slider.Slider;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private ImageButton atas, bawah, kanan, kiri, kiriatas, kiribawah, kananatas, kananbawah, Zatas, Zbawah;
    private Button btnConnect;
    private  ImageButton btnSetting;
    private Slider slider;
    private TextView koor;
    private String msg;

    private float valX = 0f, valY = 0f, valZ = 0f, valGripper = 0f;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private ZContext context;
    private ZMQ.Socket socket;
    private boolean isConnected = false;
    private int kondisiLed = 1;

    private String serverIp = "192.168.1.100";
    private int serverPort = 5555;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_controller);

        setupSystemInsets();

        prefs = getSharedPreferences("ZMQ_PREFS", MODE_PRIVATE);
        serverIp = prefs.getString("IP", serverIp);
        serverPort = prefs.getInt("PORT", serverPort);

        // init views
        button = findViewById(R.id.button);
        slider = findViewById(R.id.gripper);
        btnConnect = findViewById(R.id.btnConnect);
        btnSetting = findViewById(R.id.btnSetting);
        atas = findViewById(R.id.atas);
        bawah = findViewById(R.id.bawah);
        kanan = findViewById(R.id.kanan);
        kiri = findViewById(R.id.kiri);
        kiriatas = findViewById(R.id.kiriatas);
        kiribawah = findViewById(R.id.kiribawah);
        kananatas = findViewById(R.id.kananatas);
        kananbawah = findViewById(R.id.kananbawah);
        Zatas = findViewById(R.id.Zatas);
        Zbawah = findViewById(R.id.Zbawah);
        koor = findViewById(R.id.koord);

        // setup button loops
        setButtonPress(atas);
        setButtonPress(kananatas);
        setButtonPress(kiriatas);
        setButtonPress(bawah);
        setButtonPress(kananbawah);
        setButtonPress(kiribawah);
        setButtonPress(kanan);
        setButtonPress(kiri);
        setButtonPress(Zatas);
        setButtonPress(Zbawah);

        // slider listener
        slider.addOnChangeListener((s, value, fromUser) -> {
            valGripper = value;
            updateCoordinateDisplay();
            if (isConnected) sendCoordinate(valX, valY, valZ, valGripper);
        });

        // connect/disconnect button
        btnConnect.setOnClickListener(v -> {
            if (!isConnected) connectToServer();
            else disconnectFromServer();
        });

        // settings button
        btnSetting.setOnClickListener(v -> showIpPortDialog());

        // ===== automatic connect =====
        handler.postDelayed(() -> {
            if (!isConnected) connectToServer();
        }, 500);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected){
                   nyalakanLed();

                }
            }
        });
    }

    private void setupSystemInsets() {
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setButtonPress(ImageButton btn) {
        btn.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startButtonLoop(v);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopButtonLoop();
                    return true;
            }
            return false;
        });
    }

    private void startButtonLoop(View v) {
        if (runnable != null) handler.removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                int id = v.getId();
                if (id == R.id.bawah || id == R.id.kananbawah || id == R.id.kiribawah) valY -= 15;
                if (id == R.id.atas || id == R.id.kananatas || id == R.id.kiriatas) valY += 15;
                if (id == R.id.kiri || id == R.id.kiribawah || id == R.id.kiriatas) valX -= 15;
                if (id == R.id.kanan || id == R.id.kananbawah || id == R.id.kananatas) valX += 15;
                if (id == R.id.Zatas) valZ += 15;
                if (id == R.id.Zbawah) valZ -= 15;
                valX = Math.max(0, Math.min(180, valX));
                valY = Math.max(0, Math.min(180, valY));
                valZ = Math.max(0, Math.min(180, valZ));

                updateCoordinateDisplay();
                if (isConnected) sendCoordinate(valX, valY, valZ, valGripper);
                handler.postDelayed(this, 100);
            }
        };
        handler.post(runnable);
    }

    private void stopButtonLoop() {
        if (runnable != null) handler.removeCallbacks(runnable);
    }

    private void updateCoordinateDisplay() {
        koor.setText(String.format("(%.2f, %.2f, %.2f, %.2f)", valX, valY, valZ, valGripper));
    }
    private void resetCoordinates() {
        valX = 0f;
        valY = 0f;
        valZ = 0f;
        valGripper = 0f;
        updateCoordinateDisplay();

        // Kirim reset ke server jika masih terhubung
        if (isConnected) sendCoordinate(valX, valY, valZ, valGripper);
    }

    private void showIpPortDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_ip_port, null);
        EditText etIp = dialogView.findViewById(R.id.etIpAddress);
        EditText etPort = dialogView.findViewById(R.id.etPort);
        etIp.setText(serverIp);
        etPort.setText(String.valueOf(serverPort));

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Server Settings")
                .setView(dialogView)
                .setPositiveButton("Connect", (dialog, which) -> {
                    String ip = etIp.getText().toString().trim();
                    String portStr = etPort.getText().toString().trim();
                    if (ip.isEmpty() || portStr.isEmpty()) {
                        Toast.makeText(this, "IP and Port cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int port;
                    try { port = Integer.parseInt(portStr); }
                    catch (NumberFormatException e) {
                        Toast.makeText(this, "Port must be a number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    serverIp = ip;
                    serverPort = port;

                    // Save for next auto-connect
                    prefs.edit().putString("IP", serverIp).putInt("PORT", serverPort).apply();

                    disconnectFromServer(); // disconnect if already connected
                    connectToServer();      // connect to new server
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                context = new ZContext();
                socket = context.createSocket(SocketType.PUSH);
                socket.setSendTimeOut(1000);
                socket.setLinger(500);
                socket.connect("tcp://" + serverIp + ":" + serverPort);
                isConnected = true;
                runOnUiThread(() -> {
                    btnConnect.setText("DISCONNECT");
                    Toast.makeText(this, "Connected to server", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e("ZMQ", "Connection failed", e);
                runOnUiThread(() -> Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void sendCoordinate(float x, float y, float z, float gripper) {
        if (socket != null && isConnected) {
            String msg = x + "," + y + "," + z + "," + gripper;
            socket.send(msg.getBytes(ZMQ.CHARSET));
            Log.d("ZMQ", "Sent: " + msg);
        }
    }
    //Testing
    private void nyalakanLed() {
        if (socket != null && isConnected) {

            if (kondisiLed == 0){
                msg = "0";
                kondisiLed = 1;
            } else if (kondisiLed == 1){
                msg = "1";
                kondisiLed = 0;
            }
            socket.send(msg.getBytes(ZMQ.CHARSET));
            Log.d("ZMQ", "Sent: " + msg);
            runOnUiThread(() -> {
                Toast.makeText(this, "Data terkirim", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void disconnectFromServer() {
        new Thread(() -> {
            try {
                resetCoordinates();
                if (socket != null) socket.close();
                if (context != null) context.close();
                isConnected = false;
                runOnUiThread(() -> {
                    btnConnect.setText("CONNECT");
                    Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e("ZMQ", "Disconnect failed", e);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopButtonLoop();
        disconnectFromServer();
        resetCoordinates();

    }
}
