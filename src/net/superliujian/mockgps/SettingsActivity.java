package net.superliujian.mockgps;

import java.util.ArrayList;
import java.util.HashMap;

import net.superliujian.mockgps.constants.Constants;
import net.superliujian.mockgps.model.Preferences;
import net.superliujian.mockgps.utils.ActivityUtils;
import net.superliujian.mockgps.utils.Utils;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



public final class SettingsActivity extends ListActivity {
	private static final String LOG_TAG = "SettingsView";

	private Context mContext;

	private ArrayList<Entry> list = new ArrayList<Entry>();
	private SettingsAdapter settingsList;
	private static ProgressDialog searchLoading = null;

	private String accuracy = "";
	private String speed = "0";
	private String bearing = "0";
	private String interval="5000";
	
	private GPSBaseMenuObj intervalMenu;
	private GPSBaseMenuObj speedMenu;
	private GPSBaseMenuObj accuracyMenu;
	private GPSBaseMenuObj bearingMenu;

	private HashMap<String, MenuObj> hm = new HashMap<String, MenuObj>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.settings_list_view);
		
		setTitle(getString(R.string.pageTitleSttings));
		
		ActivityUtils.addActivity(this);
		
		mContext = this;

		initGPSValue();

		setDataAdapter();

		setListAdapter(settingsList);

		searchLoading = ProgressDialog.show(mContext, "",
				getString(R.string.importLoadingTitle), true, true);
		searchLoading.dismiss();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ActivityUtils.removeActivity(this);
	}

	private void setDataAdapter() {
		hm = new HashMap<String, MenuObj>();
		int intLoop = 0;
		MenuObj menuObj;

		list.clear();
		if (settingsList != null) {
			settingsList.notifyDataSetChanged();
		}

		menuObj = new ImportMenuObj(this,
				getString(R.string.settingsImportTitle),
				getString(R.string.settingsImportDesc), null, "Import");
		hm.put(Integer.toString(intLoop), menuObj);
		list.add(menuObj.getMenuObj());
		intLoop++;

		
		intervalMenu = new GPSBaseMenuObj(
				this,
				getString(R.string.dialogIntervalTitle),
				getString(R.string.dialogIntervalLabel) + ": " + interval,
				null, "Interval", Constants.EXTRA_INTERVAL);
		hm.put(Integer.toString(intLoop), intervalMenu);
		list.add(intervalMenu.getMenuObj());
		intLoop++;
		
		
		
		speedMenu = new GPSBaseMenuObj(
				this,
				getString(R.string.dialogGPSSpeedTitle),
				getString(R.string.dialogGPSSpeedLabel) + ", current: " + speed,
				null, "Speed", Constants.EXTRA_SPEED);
		hm.put(Integer.toString(intLoop), speedMenu);
		list.add(speedMenu.getMenuObj());
		intLoop++;

		accuracyMenu = new GPSBaseMenuObj(this,
				getString(R.string.dialogGPSAccuracyTitle),
				getString(R.string.dialogGPSAccuracyLabel) + ", current: "
						+ accuracy, null, "Altitude", Constants.EXTRA_ALTITUDE);
		hm.put(Integer.toString(intLoop), accuracyMenu);
		list.add(accuracyMenu.getMenuObj());
		intLoop++;

		bearingMenu = new GPSBaseMenuObj(this,
				getString(R.string.dialogGPSBearingTitle),
				getString(R.string.dialogGPSBearingLabel) + ", current: "
						+ bearing, null, "Bearing", Constants.EXTRA_BEARING);
		hm.put(Integer.toString(intLoop), bearingMenu);
		list.add(bearingMenu.getMenuObj());
		intLoop++;

		

		
		
		
		menuObj = new TipMenuObj(this, getString(R.string.settingsTipTitle),
				getString(R.string.settingsTipDesc), null, "Tip");
		hm.put(Integer.toString(intLoop), menuObj);
		list.add(menuObj.getMenuObj());
		intLoop++;
		
		menuObj = new AboutMenuObj(this,
				getString(R.string.settingsAboutTitle),
				getString(R.string.settingsAboutDesc)
						+ Utils.getVersionName(mContext), null, "About");
		hm.put(Integer.toString(intLoop), menuObj);
		list.add(menuObj.getMenuObj());
		intLoop++;
		
		settingsList = new SettingsAdapter(this, list);

	}

	class MenuObj {
		public String title;
		public String desc;
		public Context mContext;
		public Class<?> mClass;
		public String logKey;
		public Bundle bd;

		public MenuObj(Context ctx, String title, String desc, Class<?> cls,
				String logKey) {
			this.title = title;
			this.desc = desc;
			mContext = ctx;
			mClass = cls;
			this.logKey = logKey;
		}

		public MenuObj(Context ctx, String title, String desc, Class<?> cls,
				String logKey, Bundle bd) {
			this.title = title;
			this.desc = desc;
			mContext = ctx;
			mClass = cls;
			this.logKey = logKey;
			this.bd = bd;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public Entry getMenuObj() {
			Entry settings = new Entry();
			settings.text1 = title;
			settings.text2 = desc;
			return settings;
		}

		public void execute() {
			Intent it = new Intent(mContext, mClass);
			if (bd != null) {
				it.putExtras(bd);
			}
			startActivity(it);
			finish();
		}
	}

	class ImportMenuObj extends MenuObj {
		public ImportMenuObj(Context ctx, String title, String desc,
				Class<?> cls, String logKey) {
			super(ctx, title, desc, cls, logKey);
		}

		public void execute() {
			LayoutInflater factory = LayoutInflater.from(mContext);
			final View textEntryView = factory.inflate(
					R.layout.alert_dialog_import, null);

			final AlertDialog dialog = new AlertDialog.Builder(mContext)
					.setTitle(R.string.dialogImportTitle)
					.setView(textEntryView).create();

			final EditText editName = (EditText) textEntryView
					.findViewById(R.id.editName);
			final TextView txtError = (TextView) textEntryView
					.findViewById(R.id.txtError);
			final Button btnChoose =(Button)textEntryView
					.findViewById(R.id.btnChoose);
			final Button btnOk = (Button) textEntryView
					.findViewById(R.id.btnOk);
			final Button btnCancel = (Button) textEntryView
					.findViewById(R.id.btnCancle);

			editName.setText(Constants.IMPORT_DIR + Constants.IMPORT_FILENAME);

			btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if ("".equals(editName.getText().toString())) {
						txtError.setText(getString(R.string.errorName));
						txtError.setVisibility(View.VISIBLE);
						return;
					} else {
						dialog.dismiss();
						searchLoading.show();
						try
						{
							Utils.doImport(mContext, editName.getText().toString(),mHandler);
						}catch(Exception e){
							Toast.makeText(SettingsActivity.this, "导入出错", 0).show();
						}
					}
				}
			});

			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			
			btnChoose.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					OpenDialog dialog=new OpenDialog(SettingsActivity.this);
					dialog.setOnFileSelected(new OnFileSelectedListener(){

						@Override
						public void onSelected(String path, String fileName) {
							// TODO Auto-generated method stub
							editName.setText(path+fileName);
						}
						
					});
					dialog.Show();
				}
			});

			String error = Utils.checkSdcard(Constants.IMPORT_DIR);
			if ("".equals(error)) {
				dialog.show();
			} else {
				Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
			}

		}
	}

	class AboutMenuObj extends MenuObj {
		public AboutMenuObj(Context ctx, String title, String desc,
				Class<?> cls, String logKey) {
			super(ctx, title, desc, cls, logKey);
		}

		public void execute() {
			LayoutInflater factory = LayoutInflater.from(mContext);
			final View textEntryView = factory.inflate(
					R.layout.alert_dialog_about, null);

			final AlertDialog dialog = new AlertDialog.Builder(mContext)
					.setTitle(R.string.dialogAbout).setView(textEntryView)
					.create();

			final TextView txtAbout = (TextView) textEntryView
					.findViewById(R.id.txtAbout);
			final Button btnOk = (Button) textEntryView
					.findViewById(R.id.btnOk);

			String strAbout = mContext.getString(R.string.app_name) + ": v"
					+ Utils.getVersionName(mContext)+"\n"+getString(R.string.right);
			strAbout += "\n" + getString(R.string.about_copyright) + "\n"
					+ getString(R.string.about_contact);

			txtAbout.setText(strAbout);

			btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					dialog.dismiss();
				}
			});

			dialog.show();


		}
	}

	class TipMenuObj extends MenuObj {
		public TipMenuObj(Context ctx, String title, String desc, Class<?> cls,
				String logKey) {
			super(ctx, title, desc, cls, logKey);
		}

		public void execute() {
			LayoutInflater factory = LayoutInflater.from(mContext);
			final View textEntryView = factory.inflate(
					R.layout.alert_dialog_tip, null);

			final AlertDialog dialog = new AlertDialog.Builder(mContext)
					.setTitle(R.string.dialogTip).setView(textEntryView)
					.create();

			final Button btnOk = (Button) textEntryView
					.findViewById(R.id.btnOk);

			btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					dialog.dismiss();
				}
			});

			dialog.show();



		}
	}

	class GPSBaseMenuObj extends MenuObj {
		String type = "";

		public GPSBaseMenuObj(Context ctx, String title, String desc,
				Class<?> cls, String logKey, String type) {
			super(ctx, title, desc, cls, logKey);
			this.type = type;
		}

		public void execute() {
			String title = "";
			String label = "";
			String defValue = "";
			if (Constants.EXTRA_SPEED.equals(type)) {
				defValue = speed;
				title = getString(R.string.dialogGPSSpeedTitle);
				label = getString(R.string.dialogGPSSpeedLabel);
			} else if (Constants.EXTRA_ALTITUDE.equals(type)) {
				defValue = accuracy;
				title = getString(R.string.dialogGPSAccuracyTitle);
				label = getString(R.string.dialogGPSAccuracyLabel);

			} else if (Constants.EXTRA_BEARING.equals(type)) {
				defValue = bearing;
				title = getString(R.string.dialogGPSBearingTitle);
				label = getString(R.string.dialogGPSBearingLabel);
			} else if (Constants.EXTRA_INTERVAL.equals(type)) {
				defValue = interval;
				title = getString(R.string.dialogIntervalTitle);
				label = getString(R.string.dialogIntervalLabel);
			}
			LayoutInflater factory = LayoutInflater.from(mContext);
			final View textEntryView = factory.inflate(
					R.layout.alert_dialog_gps, null);

			final AlertDialog dialog = new AlertDialog.Builder(mContext)
					.setTitle(title).setView(textEntryView).create();

			final TextView txtName = (TextView) textEntryView
					.findViewById(R.id.txtName);
			final EditText editName = (EditText) textEntryView
					.findViewById(R.id.editName);
			final TextView txtError = (TextView) textEntryView
					.findViewById(R.id.txtError);
			final Button btnOk = (Button) textEntryView
					.findViewById(R.id.btnOk);
			final Button btnCancel = (Button) textEntryView
					.findViewById(R.id.btnCancle);

			txtName.setText(label);
			editName.setText(defValue);

			btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if ("".equals(editName.getText().toString())) {
						txtError
								.setText(getString(R.string.errorInputWrongValue));
						txtError.setVisibility(View.VISIBLE);
						return;
					} else {
						try {
							if (Constants.EXTRA_SPEED.equals(type)) {
								Preferences.putSpeed(Float.parseFloat(editName
										.getText().toString()));
								speed = editName.getText().toString();
								setDesc(getString(R.string.dialogGPSSpeedLabel)
										+ ", current: " + speed);
							} else if (Constants.EXTRA_ALTITUDE.equals(type)) {
								Preferences.putAccuracy(Float.parseFloat(editName
										.getText().toString()));
								accuracy = editName.getText().toString();
								setDesc(getString(R.string.dialogGPSAutitudeLabel)
										+ ", current: " + accuracy);
							} else if (Constants.EXTRA_BEARING.equals(type)) {
								Preferences.putBearing(Float
										.parseFloat(editName.getText()
												.toString()));
								bearing = editName.getText().toString();
								setDesc(getString(R.string.dialogGPSBearingLabel)
										+ ", current: " + bearing);
							}else if (Constants.EXTRA_INTERVAL.equals(type)) {
								Preferences.putInterval(Long.parseLong(editName.getText().toString()));
								interval = editName.getText().toString();
								//setDesc(getString(R.string.dialogIntervalLabel)+ ", current(ms): " + interval);
							}

							setDataAdapter();

							dialog.dismiss();
						} catch (Exception e) {
							txtError
									.setText(getString(R.string.errorInputWrongValue));
							txtError.setVisibility(View.VISIBLE);
						}
					}
				}
			});

			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			dialog.show();
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			searchLoading.dismiss();

			Integer length = (Integer) msg.obj;
			Toast.makeText(mContext, getString(R.string.importNums, length),
					Toast.LENGTH_LONG).show();
			Intent it = new Intent(Constants.REFRESH_BROADCAST);
			mContext.sendBroadcast(it);
		}
	};

	class Entry {
		public String text1;
		public String text2;
	}

	class SettingsAdapter extends BaseAdapter {

		private final ArrayList<Entry> mActions;

		private final LayoutInflater mInflater;

		public SettingsAdapter(Context context, ArrayList<Entry> actions) {
			mActions = actions;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return mActions.size();
		}

		public Object getItem(int position) {
			return mActions.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// Make sure we have a valid convertView to start with
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.setting_item_two_line_row, parent, false);
			}

			// Fill action with icon and text.
			final Entry entry = mActions.get(position);
			convertView.setTag(entry);
			TextView text1 = (TextView) convertView.findViewById(R.id.text1);
			TextView text2 = (TextView) convertView.findViewById(R.id.text2);

			text1.setText(entry.text1);
			if (entry.text2 != null && !"".equals(entry.text2)) {
				text2.setText(entry.text2);
				text2.setVisibility(View.VISIBLE);
			} else {
				text2.setVisibility(View.GONE);
			}

			return convertView;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		initGPSValue();

		try {
			if (hm.containsKey(Integer.toString(position))) {
				hm.get(Integer.toString(position)).execute();
			}
		} catch (Exception e) {

		}
	}

	private void initGPSValue() {
		speed = Float.toString(Preferences.getSpeed());
		accuracy = Float.toString(Preferences.getAccuracy());
		bearing = Float.toString(Preferences.getBearing());
		interval = Long.toString(Preferences.getInterval());
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (searchLoading != null && searchLoading.isShowing()) {
			searchLoading.dismiss();
		}
	}

}
