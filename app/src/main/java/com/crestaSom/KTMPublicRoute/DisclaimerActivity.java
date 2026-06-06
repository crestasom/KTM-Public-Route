package com.crestaSom.KTMPublicRoute;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class DisclaimerActivity extends AppCompatActivity {

	Button searchBtn = null;
	Toolbar toolbar;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_disclaimer);
		toolbar = (Toolbar) findViewById(R.id.toolBar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setIcon(R.drawable.iconktmlogo);
		getSupportActionBar().setTitle(" KTM Public Route");
		searchBtn = (Button) findViewById(R.id.button);

		searchBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				finish();

			}
		});
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
		animation.setDuration(1000);
		searchBtn.setVisibility(View.VISIBLE);
		searchBtn.startAnimation(animation);


	}




}