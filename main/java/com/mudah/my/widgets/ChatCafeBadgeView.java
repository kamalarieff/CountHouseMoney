package com.mudah.my.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mudah.my.R;


public class ChatCafeBadgeView extends RelativeLayout{

    private TextView badgeNumber;

    public ChatCafeBadgeView(Context context) {
        super(context);
        init();
    }

    public ChatCafeBadgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatCafeBadgeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.badge_view, this);
        initInstance();
    }

    private void initInstance() {
        badgeNumber = (TextView) findViewById(R.id.badgeNumber);
    }

    public ChatCafeBadgeView setNumber(String number) {
        badgeNumber.setText(number);
        return this;
    }

    public ChatCafeBadgeView setTextSize(float textSize){
        badgeNumber.setTextSize(textSize);
        return this;
    }

}
