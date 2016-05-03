package com.coolweather.app.acitivity;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {

	private CoolWeatherDB coolWeatherDB;

	private ProgressDialog progressDialog;

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		coolWeatherDB = CoolWeatherDB.getInstance(ChooseAreaActivity.this);

		queryFromServer("", "province");

	}

	private void queryFromServer(final String code, final String type) {
		String address = "";
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";

		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}

		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {

				boolean result = false;
				if ("province".equals(type)) {

					result = HttpUtil.handleProvincesResponse(coolWeatherDB, response);

				} else if ("city".equals(type)) {

					result = HttpUtil.handleCitiesResponse(coolWeatherDB, response, 1);

				} else if ("county".equals(type)) {

					result = HttpUtil.handleCountiesResponse(coolWeatherDB, response, 1);

				}

				if (result) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							
			
						}
					});

				}

			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败...", Toast.LENGTH_SHORT).show();

					}
				});

			}
		});

	}

	private void showProgressDialog() {

		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载.......");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();

	}

	private void closeProgressDialog() {

		if (progressDialog != null) {

			progressDialog.dismiss();
		}

	}

	
	@Override
	public void onBackPressed() {
	
	
		
		
		
	}
}
