package com.clean.space.ui;

import com.clean.space.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AlertDeleteDialog {
	private Context mContext;
	private AlertDialog mDialog;
	private TextView mLocation;
	private Button mGoonButton;
	private Button mCancelButton;

	public AlertDeleteDialog(Context context) {
		mContext = context;
		
		View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_alert_delete, null);
		mLocation = (TextView) view.findViewById(R.id.location);
		mGoonButton = (Button) view.findViewById(R.id.goon_button);
		mCancelButton = (Button) view.findViewById(R.id.cancel_button);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(view);
		mDialog = builder.create();
	}

	public void show() {
		mDialog.show();
	}
	
	public void setLocation(CharSequence location) {
		mLocation.setText(location);
	}
	
	public void setNegativeButton(OnClickListener listener) {
		mCancelButton.setOnClickListener(listener);
	}
	
	public void setPositiveButton(OnClickListener listener) {
		mGoonButton.setOnClickListener(listener);
	}
	
	public void dismiss() {
		mDialog.dismiss();
	}
}
