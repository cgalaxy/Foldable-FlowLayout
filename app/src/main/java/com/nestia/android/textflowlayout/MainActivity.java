package com.nestia.android.textflowlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements FlowLayout.FlowLayoutListener {
    private FlowLayout flow_layout;
    private LayoutInflater mInflater;
    private LinearLayout main;
    private String[] mStr = new String[]{"xxxxxx", "xx", "xxxxxxxxxxxxxxx", "xxxxxxxxxxxx"
            , "x", "xxx", "xxxxxxxxx", "x", "xxx", "xxxxxxxxx", "x", "xxx", "xxxxxxxxx", "x", "xxx", "xxxxxxxxx", "x", "xxx", "xxxxxxxxx"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flow_layout = (FlowLayout) findViewById(R.id.flow_layout);
        main = (LinearLayout) findViewById(R.id.activity_main);
        flow_layout.setMaxLine(3);
        flow_layout.setFlowLayoutListener(this);
        mInflater = LayoutInflater.from(this);
        for (int i = 0; i < mStr.length; i++) {
            final TextView textView = (TextView) mInflater.inflate(R.layout.tv, flow_layout, false);
            textView.setText(mStr[i]);
            flow_layout.addView(textView);
            final int finalI = i;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "标签ID:" + finalI + "标签内容" + textView.getText(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void lineChangeListener() {
        final TextView m = (TextView) mInflater.inflate(R.layout.more, main, false);
        main.addView(m);
        m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flow_layout.setMaxLine(flow_layout.getLineCount());
                flow_layout.requestLayout();
                main.removeView(m);
            }
        });
    }
}
