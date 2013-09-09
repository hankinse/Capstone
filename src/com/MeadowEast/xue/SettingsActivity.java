package com.MeadowEast.xue;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class SettingsActivity extends Activity implements OnClickListener {

	public void onClick(View arg0) {
	   	Intent i;
	   	switch (arg0.getId()){
	   	case R.id.update_button:
    		i = new Intent(this, LearnActivity.class);
	    	startActivity(i);
			break;
	    	}
	    }

}
