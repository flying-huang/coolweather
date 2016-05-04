package com.coolweather.app.acitivity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private CoolWeatherDB coolWeatherDB;

	private ProgressDialog progressDialog;

	private ListView mListView;
	private TextView mTextView;
	private List<String> datalist = new ArrayList<String>();

	private ArrayAdapter<String> adapter;

	/**
	 * ʡ�б�
	 */
	private List<Province> provinceList;
	/**
	 * ���б�
	 */
	private List<City> cityList;
	/**
	 * ���б�
	 */
	private List<County> countyList;
	/**
	 * ѡ�е�ʡ��
	 */
	private Province selectedProvince;
	/**
	 * ѡ�еĳ���
	 */
	private City selectedCity;
	/**
	 * ��ǰѡ�еļ���
	 */
	private int currentLevel;

	/**
	 * �Ƿ��WeatherActivity����ת������
	 */
	private boolean isFromWeatherActivity;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_ activity", false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// �Ѿ�ѡ���˳����Ҳ��Ǵ�WeatherActivity��ת�������Ż�ֱ����ת��WeatherActivity
		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		coolWeatherDB = CoolWeatherDB.getInstance(ChooseAreaActivity.this);
		mTextView = (TextView) findViewById(R.id.title_text);
		mListView = (ListView) findViewById(R.id.listview);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datalist);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int index, long id) {

				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(index);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countyList.get(index).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}

			}

		});

		queryProvinces(); // ����ʡ������

	}

	// ����ʡ������
	private void queryProvinces() {
		provinceList = coolWeatherDB.getProvince();
		if (provinceList.size() > 0) {
			datalist.clear();
			for (Province province : provinceList) {
				datalist.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			mListView.setSelection(0);
			mTextView.setText("�й�");
			currentLevel = LEVEL_PROVINCE;

		} else {
			queryFromServer(null, "province");
		}

	}

	// �����ؼ�����
	protected void queryCounties() {

		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) {
			datalist.clear();
			for (County county : countyList) {
				datalist.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			mListView.setSelection(0);
			mTextView.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}

	}

	// �����м�����
	protected void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			datalist.clear();
			for (City city : cityList) {
				datalist.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			mListView.setSelection(0);
			mTextView.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}

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

					result = HttpUtil.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());

				} else if ("county".equals(type)) {

					result = HttpUtil.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());

				}

				if (result) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							closeProgressDialog();

							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}

						}
					});

				}

			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��...", Toast.LENGTH_SHORT).show();

					}
				});

			}
		});

	}

	private void showProgressDialog() {

		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���.......");
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
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}

			finish();
		}

	}
}
