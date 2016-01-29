package com.clean.space;

import com.clean.space.ui.listener.BackHandledFragment;
import com.clean.space.ui.listener.BackHandledInterface;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;

public class LookExportPhotoActivity extends Activity implements BackHandledInterface{
	
	private final String TAG = LookExportPhotoActivity.class.getSimpleName();
	
	CleanPhotoFragment cleanPhotoFragment = null;

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

			// Create a new Fragment to be placed in the activity layout
			cleanPhotoFragment = new CleanPhotoFragment(CleanPhotoFragment.TYPE_CURRENT_EXPORTED);
			// plusOneFragment = PlusOneFragment.newInstance("", "");
			FragmentManager fm = this.getFragmentManager();

			// Add the fragment to the 'fragment_container' FrameLayout
			fm.beginTransaction()
					.add(R.id.fragment_container, cleanPhotoFragment)
					.commit();
			// fm.beginTransaction().add(R.id.fragment_container, new
			// CleanPhotoFragment()).commit();
		}
	}

	@Override
	public void onBackPressed() {
		if(mFragment == null || !mFragment.onBackPressed()){
			super.onBackPressed();
        }
	}

	public void onReturnBtnClicked(View view) {
		finish();
	}

	BackHandledFragment mFragment;

	@Override
	public void setSelectedFragment(BackHandledFragment selectedFragment) {
		mFragment = selectedFragment;
	}
}
