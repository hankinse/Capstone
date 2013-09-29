package com.MeadowEast.xue;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class LearnLinearLayout extends LinearLayout {
    public LearnLinearLayout(Context context) {
        super(context);
    }
    public LearnLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public LearnLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            return false;
        } 
        if(event.getAction() == MotionEvent.ACTION_UP){
            return false;
        }
        
        return true;
    }
}