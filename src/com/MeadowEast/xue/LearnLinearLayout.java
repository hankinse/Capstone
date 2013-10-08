package com.MeadowEast.xue;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class LearnLinearLayout extends LinearLayout {

	float xDelta = 0;

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
		if (Math.abs(xDelta - event.getX()) < 50) {
			return false;
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE
				|| event.getAction() == MotionEvent.ACTION_DOWN
				|| event.getAction() == MotionEvent.ACTION_UP) {
			xDelta = event.getX();
		}
		return true;
	}
}