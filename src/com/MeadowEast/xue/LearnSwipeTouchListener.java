package com.MeadowEast.xue;

import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class LearnSwipeTouchListener implements OnTouchListener {

	private final GestureDetector gestureDetector = new GestureDetector(
			LearnActivity.context, new GestureListener());

	public boolean onTouch(final View view, final MotionEvent motionEvent) {
		gestureDetector.onTouchEvent(motionEvent);
		return true;
	}

	private final class GestureListener extends SimpleOnGestureListener {

		private static final int DISTANCE_THRESHOLD = 85;
		private static final int VELOCITY_THRESHOLD = 160;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				float deltaX = e2.getX() - e1.getX();
				float deltaY = e2.getY() - e1.getY();

				if (Math.abs(deltaX) > Math.abs(deltaY)) {
					if (Math.abs(deltaX) > DISTANCE_THRESHOLD
							&& Math.abs(velocityX) > VELOCITY_THRESHOLD) {
						if (deltaX > 0) {
							onRightSwipe();
						} else {
							onLeftSwipe();
						}
					}
				} else {
					if (Math.abs(deltaY) > DISTANCE_THRESHOLD
							&& Math.abs(velocityY) > VELOCITY_THRESHOLD) {
						if (deltaY > 0) {
							onDownSwipe();
						} else {
							onUpSwipe();
						}
					}
				}
			} catch (Exception e) {
			}

			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return super.onDown(e);
		}
	}

	public void onRightSwipe() {
	}

	public void onLeftSwipe() {
	}

	public void onUpSwipe() {
	}

	public void onDownSwipe() {
	}
}