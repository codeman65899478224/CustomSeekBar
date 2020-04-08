package com.cyy.customtextseekbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.cyy.seekbar.CustomTextSeekBar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CustomTextSeekBar seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new CustomTextSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(int index) {
                Log.i("MainActivity", "当前刻度: " + index);
            }
        });
    }
}
