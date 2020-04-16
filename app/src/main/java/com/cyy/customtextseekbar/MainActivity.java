package com.cyy.customtextseekbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.cyy.seekbar.CustomTextSeekBar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CustomTextSeekBar seekBar = findViewById(R.id.seekbar);
        List<String> list = new ArrayList<>();
        list.add("2");
        list.add("2");
        list.add("1");
        list.add("1");
        seekBar.setTextArray(list);
        seekBar.setOnSeekBarChangeListener(new CustomTextSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(int index) {
                Log.i("MainActivity", "当前刻度: " + index);
            }
        });
    }
}
