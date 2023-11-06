package com.example.raspberryconnection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static final int INTERNET_PERMISSION_CODE = 1001;
    private static final int SERVER_PORT = 5000;
    private String ipAddress = "192.168.0.0"; //type here IP address of your raspberry device

    private String createJsonPacket(String onOff) throws JSONException {
        JSONObject jsonData = new JSONObject();
        jsonData.put("status", "ok");
        jsonData.put("gpio", onOff);
        return jsonData.toString();
    }

    private void sendPackage(final String ipAddress, final String onOff) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int timeoutMillis = 1000;

                try {
                    String jsonPacket = createJsonPacket(onOff);

                    Socket socket = new Socket();
                    InetSocketAddress socketAddress = new InetSocketAddress(ipAddress, SERVER_PORT);

                    socket.connect(socketAddress, timeoutMillis);

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    writer.write(jsonPacket);
                    writer.newLine();
                    writer.flush();

                    writer.close();
                    socket.close();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Send successfully", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    final String errorMessage = e.getMessage();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestInternetPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText inputField = findViewById(R.id.textIP);
        Button btnOn = findViewById(R.id.btn_on);
        Button btnOff = findViewById(R.id.btn_off);

        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ipAddress = inputField.getText().toString();
                Toast.makeText(MainActivity.this, "Turning On", Toast.LENGTH_SHORT).show();
                sendPackage(ipAddress, "on");
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ipAddress = inputField.getText().toString();
                Toast.makeText(MainActivity.this, "Turning Off", Toast.LENGTH_SHORT).show();
                sendPackage(ipAddress, "off");
            }
        });

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Version doesn't need special permission", Toast.LENGTH_LONG).show();
        } else {
            requestInternetPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == INTERNET_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
