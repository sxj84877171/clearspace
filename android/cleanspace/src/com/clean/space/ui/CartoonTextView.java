/**
 * 
 */
package com.clean.space.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.clean.space.log.FLog;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author Elvis
 * 
 */
public class CartoonTextView extends TextView {

	private Handler handler;

	private String TAG = CartoonTextView.class.getSimpleName();

	/**
	 * @param context
	 */
	public CartoonTextView(Context context) {
		this(context, null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public CartoonTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyleAttr
	 */
	public CartoonTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		handler = new Handler(context.getMainLooper());
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyleAttr
	 * @param defStyleRes
	 */
	public CartoonTextView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		this(context, attrs, defStyleAttr);
	}

	public void setCartoonText(CharSequence text) {
		if (impl != null) {
			handler.removeCallbacks(impl);
		}
		float max = getNumText(text);
		if (max > 0) {
			impl = new RunnableImpl(text, null);
			handler.postDelayed(impl, 500);
		} else {
			setText(text);
		}
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
	}

	private void reflesh(CharSequence text, BufferType type) {
		super.setText(text, type);
	}

	/*
	 * public void setCartoonText(String text){ setCartoonText(text); }
	 */

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	private float getNumText(CharSequence text) {
		Pattern p = Pattern.compile("[0-9\\.]+");
		Matcher m = p.matcher(text);
		String result = "-1";
		if (m.find()) {
			result = m.group();
		}
		float retFloat = -1;
		try {
			retFloat = Float.parseFloat(result);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return retFloat;
	}

	private RunnableImpl impl;

	class RunnableImpl implements Runnable {

		CharSequence text;
		BufferType type;
		float cur = 1;
		float max;
		float step = 0;

		public RunnableImpl(CharSequence text, BufferType type) {
			this.text = text;
			this.type = type;
			max = getNumText(text);
			String maxStr = "" + max;
			if (maxStr.contains(".")) {
				step = cur = 0.1f;
			} else {
				step = cur = 1f;
			}
			step = max/50;
		}

		@Override
		public void run() {
			String textStr = "" + text;
			DecimalFormat df = new DecimalFormat("#.#");
			textStr = textStr.replace("" + max, df.format(cur));
			reflesh(textStr, type);
			cur += step;
			if (cur < max) {
				handler.postDelayed(this, 20);
			} else {
				reflesh(text, type);
			}
		}
	};

}
