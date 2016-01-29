package com.clean.space;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.clean.space.ui.listener.BackHandledFragment;
import com.clean.space.ui.listener.BackHandledInterface;

@SuppressLint("NewApi")
public class CleanPhotoActivity extends Activity implements
		BackHandledInterface {

	CleanPhotoMainFragment mMainFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clean_photo);

		if (findViewById(R.id.fragment_container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			Intent intent = getIntent();
			String page = intent.getStringExtra("page");
			if (Constants.PAGE_EXPORTPHOTOFRAGMENT.equals(page)) {
				getFragmentManager()
						.beginTransaction()
						.add(R.id.fragment_container, new ExportPhotoFragment())
						.commit();
			} else if (Constants.PAGE_CLEANPHOTOFRAGMENT.equals(page)) {
				getFragmentManager()
				.beginTransaction()
				.add(R.id.fragment_container, new CleanPhotoFragment(CleanPhotoFragment.TYPE_ALL_EXPORTED))
				.commit();
			} else {

				// Create a new Fragment to be placed in the activity layout
				mMainFragment = new CleanPhotoMainFragment();
				// plusOneFragment = PlusOneFragment.newInstance("", "");
				FragmentManager fm = this.getFragmentManager();

				// Add the fragment to the 'fragment_container' FrameLayout
				fm.beginTransaction()
						.add(R.id.fragment_container, mMainFragment).commit();
				// fm.beginTransaction().add(R.id.fragment_container, new
				// ExportPhotoFragment()).commit();
			}
		}
	}

	BackHandledFragment mFragment;

	@Override
	public void setSelectedFragment(BackHandledFragment selectedFragment) {
		mFragment = selectedFragment;
	}

	@Override
	public void onBackPressed() {
		
		if (mFragment == null || !mFragment.onBackPressed()) {
			String tag = UserSetting.getString(CleanPhotoActivity.this, Constants.COMEFROMSCAN, "");
			if("scan".equals(tag)){
				Intent main = new Intent(CleanPhotoActivity.this,MainActivity.class);
				startActivity(main);
				UserSetting.setString(CleanPhotoActivity.this, Constants.COMEFROMSCAN, "");
				finish();
			}else{
				super.onBackPressed();
			}
		}
	}
}
