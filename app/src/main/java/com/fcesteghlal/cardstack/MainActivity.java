package com.fcesteghlal.cardstack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.fcesteghlal.cardstackview.CardStackView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreen();
        setContentView(R.layout.activity_main);
        initStack();
    }

    private void fullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void initStack() {

        List<Object> objects = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            objects.add(new Object());
        }
        StackViewAdapter adapter;
        CardStackView cardStackView = findViewById(R.id.stackView);
        adapter = new StackViewAdapter(this, objects);
        cardStackView.setAdapter(adapter);
    }
}
