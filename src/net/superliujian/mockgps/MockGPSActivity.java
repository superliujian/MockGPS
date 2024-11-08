package net.superliujian.mockgps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.superliujian.mockgps.constants.Constants;
import net.superliujian.mockgps.model.DataAdapter;
import net.superliujian.mockgps.model.DataModel;
import net.superliujian.mockgps.model.Preferences;
import net.superliujian.mockgps.service.MockLocationService;
import net.superliujian.mockgps.service.UpdateService;
import net.superliujian.mockgps.utils.ActivityTools;
import net.superliujian.mockgps.utils.ActivityUtils;
import net.superliujian.mockgps.utils.DialogClickLisener;
import net.superliujian.mockgps.utils.Utils;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.OverlayItem;


public class MockGPSActivity extends MapActivity implements OnClickListener,View.OnCreateContextMenuListener {
	private static final String LOG_TAG = "MockGPSActivity";
	private static final int DELETE_ID = 1;
	private static final int SETLOCATION_ID = 2;
	
	private boolean hasAd=true;
	
	private Context mContext;
	private MapView mMapView;
	private MyLocationOverlay mMyLocationOverlay;
	private static MapController mMapController;

	private Button mBtnSet;
	private Button mBtnSetAndFav;
	private Button mBtnStop;
	private Button mBtnLoop;

	private PopupWindow popMenu;
	private ProgressDialog searchLoading = null;

	private List<Overlay> mapOverlays;
	private MapItemizedOverlay favoritedOverlay;
	private MapItemizedOverlay searchOverlay;
	private OverlayItem centerOverlay;

	
	private static LocationManager mLocationManager;
	private static MyNotification mNotification;

	private String[] favNames = new String[1];
	private HashMap<String, OverlayItem> tempOverlays = new HashMap<String, OverlayItem>();
	private static String currentMockLoction = "";

	private DataModel selectedFavItem;
	private ArrayList<DataModel> listFavs;
	private int selectedFavItemId = -1;
	private ViewAdapter viewAdapterFav;
	private LinearLayout layout;
	private ArrayList<DataModel> allFav = new ArrayList<DataModel>();
	
	
	
	private int curentCursor = 0;
	private boolean isLoop=false;
	private boolean shouldRun=false;
	
	private boolean isForTest=false;//测试用
	
	private Handler handlerLoop=new Handler();
	private Runnable runnableLoop=new Runnable() {
		@Override
	    public void run() {
	        // TODO Auto-generated method stub
			if(!shouldRun)
				return;
			curentCursor=++curentCursor%allFav.size();
			//if(curentCursor>=allFav.size())
			//	curentCursor=allFav.size()-1;
	    	DataModel m=allFav.get(curentCursor);
			Preferences.putLastPosition(curentCursor);

			setLocation(m);
			handlerLoop.removeCallbacks(this);
			shouldRun=false;
			handlerLoop.postDelayed(this, Preferences.getInterval()*1000);
			shouldRun=true;
		
	    }
	};
	

	
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.main);
		ActivityUtils.addActivity(this);
		mContext = this;
		
		Preferences.putLaunchNum(Preferences.getLaunchNum() + 1);

		
		initView();
		initData();

		curentCursor=Preferences.getLastPosition()-1;
		if(curentCursor<-1)
			curentCursor=-1;
		
		gotoCurrentLoction();
		 
		File dir=new File(Constants.IMPORT_DIR);
		if(!dir.exists())
			dir.mkdirs();
		
		if (Preferences.getLaunchNum() == 1) {
			Utils.dialogCreate(mContext, getString(R.string.dialogTip),
					getString(R.string.dialogTipContent), null, false);
		}
		
		
	}

	
	
	
	private void gotoCurrentLoction(){

		 mLocationManager = ActivityTools.getTestLocationManager(this);
		 
		 Location currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         if(currentLocation==null)
         {
                 Log.e("SuperMap","get the location from network provider!!! error");
         }else
         {
	         GeoPoint geoGPSLocation = new GeoPoint((int)(currentLocation.getLatitude()*1000000), (int)(currentLocation.getLongitude()*1000000));
	         mMapController.setCenter(geoGPSLocation);
         }
         /*
		 mLocationManager.requestLocationUpdates(
		          LocationManager.GPS_PROVIDER, 
		          1000, 
		          0, 
		         new LocationListener(){

					@Override
					public void onLocationChanged(Location location) {
						if(location!=null)
						{
						// TODO Auto-generated method stub
							DataModel dm=new DataModel();
							dm.lat=String.valueOf(location.getLatitude());
							dm.lng=String.valueOf(location.getLongitude());
							Toast.makeText(MockGPSActivity.this, dm.toString(), 0).show();
							Message msg = new Message();
							msg.obj = dm;
							mHandler.sendMessage(msg);
							
						}
						//mLocationManager.removeUpdates(this);
					}

					@Override
					public void onProviderDisabled(String arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onProviderEnabled(String arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onStatusChanged(String arg0, int arg1,
							Bundle arg2) {
						// TODO Auto-generated method stub
						
					}
		        	  
		          });
		          
		          */
		 
	}
	private void checkUpdate() {
		Intent intent = new Intent(this, UpdateService.class);
		intent.putExtra(UpdateService.APPLICATION_NAME,
				getString(R.string.app_name));
		intent.putExtra(UpdateService.APPLICATION_VERSION, Utils
				.getVersionCode(mContext));
		startService(intent);
	}

	private void initView() {
		MockGPSApp app = (MockGPSApp)this.getApplication();
		if (app.mBMapMan == null) {
			app.mBMapMan = new BMapManager(getApplication());
			app.mBMapMan.init(app.mStrKey, new MockGPSApp.MyGeneralListener());
		}
		app.mBMapMan.start();
        super.initMapActivity(app.mBMapMan);

        mMapView = (MapView)findViewById(R.id.mapView);
        mMapView.setBuiltInZoomControls(true);
        
		mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
		mMapView.getOverlays().add(mMyLocationOverlay);
		mMapView.setSatellite(false);
		
		
		mBtnSet = (Button) findViewById(R.id.btnSet);
		mBtnSet.setOnClickListener(this);
		mBtnSetAndFav = (Button) findViewById(R.id.btnSetAndFav);
		mBtnSetAndFav.setOnClickListener(this);
		mBtnStop = (Button) findViewById(R.id.btnStop);
		mBtnStop.setOnClickListener(this);
		mBtnLoop= (Button) findViewById(R.id.btnLoop);
		mBtnLoop.setOnClickListener(this);
		
		View view = this.getLayoutInflater().inflate(R.layout.about, null);
		popMenu = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		popMenu.setOutsideTouchable(false);
		popMenu.setBackgroundDrawable(new BitmapDrawable());
		popMenu.setFocusable(true);

		final TextView txtAboutVersion = (TextView) view
				.findViewById(R.id.txtAbout);
		final TextView txtAboutContact = (TextView) view
				.findViewById(R.id.txtAboutContact);

		PackageManager manager = this.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			txtAboutVersion.setText(txtAboutVersion.getText() + " v"
					+ info.versionName);
		} catch (NameNotFoundException e) {

		}

		txtAboutContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent it = new Intent(Intent.ACTION_VIEW, Uri
						.parse(txtAboutContact.getText().toString().replace(
								"Home: ", "")));
				startActivity(it);
			}
		});

		searchLoading = ProgressDialog.show(mContext, "",
				getString(R.string.dialogSearchTitle), true, true);
		searchLoading.dismiss();

	}

	private void initData() {
		mMapController = mMapView.getController();
		//mMapController.setZoom(Preferences.getLastZoomLevel());

		mapOverlays = mMapView.getOverlays();

		favoritedOverlay = new MapItemizedOverlay(getResources().getDrawable(
				R.drawable.place), this, 12);
		searchOverlay = new MapItemizedOverlay(getResources().getDrawable(
				R.drawable.search_place), this, 12);

		initAllOverlayItems();

		// add overlay items to master list of overlays
		mapOverlays.add(favoritedOverlay);
		mapOverlays.add(searchOverlay);
		// it's must init,
		searchOverlay.clear();

		mMapView.invalidate();

		mMapView.setBuiltInZoomControls(true);
		mMapController.setZoom(Preferences.getLastZoomLevel());
		mMapController.setZoom(13);
		if (favoritedOverlay.size() > 0) {
			OverlayItem oi = favoritedOverlay.getItem(0);
			mMapController.setCenter(oi.getPoint());
		}

		mNotification = new MyNotification(this, Constants.NOTIFICATION_ID);

		IntentFilter deleteOverlayFilter = new IntentFilter(
				Constants.DELETE_BROADCAST);
		registerReceiver(deleteOverlayReceiver, deleteOverlayFilter);

		IntentFilter addOverlayFilter = new IntentFilter(
				Constants.ADD_BROADCAST);
		registerReceiver(addOverlayReceiver, addOverlayFilter);

		IntentFilter setLocationFilter = new IntentFilter(
				Constants.SHOW_ON_MAP_BROADCAST);
		registerReceiver(setLocationReceiver, setLocationFilter);

		IntentFilter refreshFilter = new IntentFilter(
				Constants.REFRESH_BROADCAST);
		registerReceiver(refreshReceiver, refreshFilter);

		IntentFilter finishFilter = new IntentFilter(Constants.FINISH_BROADCAST);
		registerReceiver(finishReceiver, finishFilter);

		currentMockLoction = "";

	}

	private void initAllOverlayItems() {
		favoritedOverlay.clear();
		DataAdapter db = new DataAdapter(mContext);
		ArrayList<DataModel> dms = db.getAllFavs();

		if (dms != null) {
			favNames = new String[dms.size() + 1];
			favNames[0] = getString(R.string.choose);

			int intLoop = 1;
			for (DataModel dm : dms) {
				addOverlay(favoritedOverlay, dm, Constants.TYPE_FAVORITE);
				if ("".equals(dm.name)) {
					favNames[intLoop] = dm.getLatLng();
				} else {
					favNames[intLoop] = dm.name;
				}

				intLoop++;
			}
		}
		mMapView.invalidate();
	}


	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnSet) {
			setLocation();
		} else if (v == mBtnSetAndFav) {
			showAddFavoriteDialog();
		} else if (v == mBtnStop) {
			stopLoopGPS(mContext);
			endTestGPS(mContext);
		}else if (v == mBtnLoop) {
			startLoopGPS(mContext);
		}
	}


	private void setLocation() {
		centerOverlay = getCenterOverlayItem("", Constants.TYPE_COMMON);
		// start the test gps
		GeoPoint gp = centerOverlay.getPoint();
		final double lat = gp.getLatitudeE6() / 1E6;
		final double lng = gp.getLongitudeE6() / 1E6;
		startTestGPS(mContext, getString(R.string.notifyLocationLabel), lat,
				lng);

		DataAdapter db = new DataAdapter(mContext);
		DataModel dm = new DataModel("", gp);
		dm.type = Constants.TYPE_COMMON;
		db.insertFav(dm);

		addOverlay(searchOverlay, dm, Constants.TYPE_COMMON);

	}

	private void setLocation(DataModel dm) {
		centerOverlay = getCenterOverlayItem("", Constants.TYPE_COMMON);
		endTestGPS(mContext);
		startTestGPS(mContext, dm.name, Double.valueOf(dm.lat).doubleValue(),
				 Double.valueOf(dm.lng).doubleValue());
		searchOverlay.clear();
		mMapView.invalidate();
		addOverlay(searchOverlay, dm, Constants.TYPE_COMMON);

	}
	
	public static void startTestGPS(Context ctx, String name, double lat,
			double lng) {
		startTestGPS(ctx, name, lat, lng, Preferences.getSpeed(), Preferences
				.getAccuracy(), Preferences.getBearing(), true);
	}

	public static void startTestGPS(Context ctx, String name, double lat,
			double lng, float speed, float accuracy, float bearing,
			boolean local) {
		if (mNotification == null) {
			mNotification = ActivityTools.getMyNotification(ctx);
		}

		mNotification.cancelNotify();

		
		GeoPoint gp=new GeoPoint((int)lat,(int)lng);
		mMapController.setCenter(gp);
		
		mMapController.setZoom(13);
		
		mLocationManager = ActivityTools.getTestLocationManager(ctx);

		if (mLocationManager == null) {
			// if triggle by other app
			if (!local) {
				Intent it = new Intent(ctx, MockGPSActivity.class);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ctx.startActivity(it);
		
			} else {
				Utils.dialogCreate(ctx, ctx
						.getString(R.string.errorMockSettingDisabled),
						new DialogClick(ctx), false);
			}

			endTestGPS(ctx);
			
			return;
		}

		mLocationManager.addTestProvider(Constants.GPS_LABEL, false, false,
				false, false, true, true, true, 0, 5);

		MockLocationService.forFlag = false;
		Intent it = new Intent(ctx, MockLocationService.class);
		it.putExtra(Constants.EXTRA_LAT, lat);
		it.putExtra(Constants.EXTRA_LNG, lng);
		it.putExtra(Constants.EXTRA_ACCURACY, accuracy);
		it.putExtra(Constants.EXTRA_BEARING, bearing);
		it.putExtra(Constants.EXTRA_SPEED, speed);
		ctx.startService(it);
		
		
		
		currentMockLoction = name + ": " + lat + ", " + lng ;// " accuracy:"+ accuracy + " speed:" + speed;

		mNotification.startNotify(currentMockLoction);
	}

	public static void endTestGPS(Context ctx) {
		if (mLocationManager == null) {
			mLocationManager = ActivityTools.getTestLocationManager(ctx);
		}
		if (mNotification == null) {
			mNotification = ActivityTools.getMyNotification(ctx);
		}

		mNotification.cancelNotify();

		try {
			mLocationManager.removeTestProvider(Constants.GPS_LABEL);
		} catch (Exception e) {
			Log.e(LOG_TAG, "" + e.getMessage());
		}
		MockLocationService.forFlag = false;
		Intent it = new Intent(ctx, MockLocationService.class);
		it.putExtra("finishFlag", true);
		ctx.startService(it);
	}

	public  void startLoopGPS(Context ctx){
		if(isLoop){
			//pause
			handlerLoop.removeCallbacks(runnableLoop);
			isLoop=false;
			shouldRun=false;
			mBtnLoop.setText(R.string.btnLoop);
		}else{
			//start
			DataAdapter db = new DataAdapter(ctx);
			allFav=db.getAllFavs();
			if(allFav.size()==0){
				Toast.makeText(this, "当前收藏夹没有任何地点，请先添加地点到收藏夹", 0).show();
				return;
			}
			
			if(isForTest){
				
				Toast.makeText(this, "测试版本，最多只能定位三个点(收藏夹前三个点)", Toast.LENGTH_LONG).show();
				
				while(allFav.size()>3){
					allFav.remove(allFav.size()-1);
				}
				
				
			}
			//curentCursor=0;
			isLoop=true;
			handlerLoop.postDelayed(runnableLoop, 0);
			shouldRun=true;
			mBtnLoop.setText(R.string.btnPause);
		}
	}
	
	public void stopLoopGPS(Context ctx){
		handlerLoop.removeCallbacks(runnableLoop);
		isLoop=false;
		shouldRun=false;
		mBtnLoop.setText(R.string.btnLoop);
		curentCursor=0;
	}
	
	private OverlayItem getCenterOverlayItem(String title, String snippet) {
		GeoPoint gp = mMapView.getMapCenter();
		return new OverlayItem(gp, title, snippet);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.menuFavs:
			showListDialog(true);
			return true;
		case R.id.menuHistory:
			showListDialog(false);
			return true;
			// case R.id.menuAbout:
			// popMenu
			// .showAtLocation(findViewById(R.id.top), Gravity.CENTER, 0,
			// 0);
			// break;
		case R.id.menuSearch:
			showSearchDialog();
			break;
		case R.id.menuSettings:
			startActivity(new Intent(mContext, SettingsActivity.class));
			break;
		case R.id.menuExit:
			endTestGPS(mContext);
			stopLoopGPS(mContext);
			ActivityUtils.exit();
			break;
		}
		return false;
	}

	private void showAddFavoriteDialog() {
		centerOverlay = getCenterOverlayItem("", Constants.TYPE_FAVORITE);
		final GeoPoint gp = centerOverlay.getPoint();
		final double lat = gp.getLatitudeE6() / 1E6;
		final double lng = gp.getLongitudeE6() / 1E6;

		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.alert_dialog_fav,
				null);

		final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(
				R.string.dialogFavTitle).setView(textEntryView).create();

		final EditText editName = (EditText) textEntryView
				.findViewById(R.id.editName);
		final EditText editLat = (EditText) textEntryView
				.findViewById(R.id.editLat);
		final EditText editLng = (EditText) textEntryView
				.findViewById(R.id.editLng);
		final Spinner spName = (Spinner) textEntryView
				.findViewById(R.id.spName);
		final TextView txtError = (TextView) textEntryView
				.findViewById(R.id.txtError);
		final Button btnOk = (Button) textEntryView.findViewById(R.id.btnOk);
		final Button btnCancel = (Button) textEntryView
				.findViewById(R.id.btnCancle);

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, favNames);


		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spName.setAdapter(adapter);

		spName.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				txtError.setText("");
				txtError.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		editName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				txtError.setText("");
				txtError.setVisibility(View.INVISIBLE);
			}
		});

		editLat.setText(Double.toString(lat));
		editLng.setText(Double.toString(lng));

		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String select = editName.getText().toString();
				if ("".equals(select)) {
					select = adapter.getItem(spName.getSelectedItemPosition());
				}

				if (getString(R.string.choose).equals(select)) {
					txtError.setText(getString(R.string.errorName));
					txtError.setVisibility(View.VISIBLE);
					return;
				}
				
				if ("".equals(editLat.getText().toString())) {
					txtError.setText(getString(R.string.errorLat));
					txtError.setVisibility(View.VISIBLE);
					return;
				}
				
				if ("".equals(editLng.getText().toString())) {
					txtError.setText(getString(R.string.errorLng));
					txtError.setVisibility(View.VISIBLE);
					return;
				}

				favoritedOverlay.addOverlay(centerOverlay);

				// update the Spinner
				if (!"".endsWith(editName.getText().toString())) {
					ArrayList<String> list = new ArrayList<String>(Arrays
							.asList(favNames));
					list.add(select);
					favNames = (String[]) list.toArray(new String[list.size()]);
					adapter.notifyDataSetChanged();
				}

				mMapView.invalidate();

				// start the test gps
				//startTestGPS(mContext, select, lat, lng);

				DataAdapter db = new DataAdapter(mContext);
				DataModel dm = new DataModel();
				dm.name = select;
				dm.lat = editLat.getText().toString();
				dm.lng = editLng.getText().toString();
				dm.type = Constants.TYPE_FAVORITE;
				db.insertFav(dm);
				// also add as history records
				dm.type = Constants.TYPE_COMMON;
				db.insertFav(dm);
				dialog.dismiss();
				int count=db.getAllFavs().size();
				Toast.makeText(MockGPSActivity.this, "当前收藏夹共有"+count+"项", 0).show();
			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				txtError.setText("");
				txtError.setVisibility(View.INVISIBLE);
				dialog.dismiss();
			}
		});

		dialog.show();

	}

	private void showListDialog(final boolean isFav) {
		LayoutInflater factory = LayoutInflater.from(mContext);
		View viewFav = factory.inflate(R.layout.alert_dialog_fav_list, null);
		final ListView listViewFav = (ListView) viewFav
				.findViewById(R.id.listViewFavs);
		listViewFav.setOnCreateContextMenuListener(this);

		final Button btnClearAllFavs = (Button) viewFav
				.findViewById(R.id.btnClearAllFavs);
		final Button btnExportAllFavs = (Button) viewFav
				.findViewById(R.id.btnExportAllFavs);
		
		final DataAdapter db = new DataAdapter(mContext);

		String dialogTitle = "";
		if (isFav) {
			listFavs = db.getAllFavs();
			btnClearAllFavs.setText(getString(R.string.btnClearAllFavs));
			dialogTitle = getString(R.string.dialogFavListTitle);
			btnExportAllFavs.setVisibility(View.VISIBLE);
			
		} else {
			listFavs = db.getAllNONFavs();
			btnClearAllFavs.setText(getString(R.string.btnClearAllHistory));
			dialogTitle = getString(R.string.dialogHistoryListTitle);
			btnExportAllFavs.setVisibility(View.GONE);
		}

		final AlertDialog dialog = new AlertDialog.Builder(mContext).setTitle(
				dialogTitle).setView(viewFav).create();

		viewAdapterFav = new ViewAdapter(mContext, listFavs);

		listViewFav.setAdapter(viewAdapterFav);

		btnExportAllFavs.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (isFav) {
				 
					OpenDialog dialog=new OpenDialog(MockGPSActivity.this);
					dialog.setOnFileSelected(new OnFileSelectedListener(){

						@Override
						public void onSelected(String path, String fileName) {
							// TODO Auto-generated method stub
							try
							{
							DataAdapter db = new DataAdapter(mContext);
							ArrayList<DataModel> favs=	db.getAllFavs();
							String file=path+"/mockgps.csv";
							BufferedWriter bf = new BufferedWriter(new FileWriter(file),10240);
							 for(int i=0;i<favs.size();i++){
								 DataModel m=favs.get(i);
								 bf.write(m.toString()+"\n");
							 }
							 bf.close();
							 Toast.makeText(MockGPSActivity.this, "导出到:"+file+"成功:共"+favs.size()+"条记录", 0).show();
							}catch(Exception e){
								 Toast.makeText(MockGPSActivity.this, "发生错误", 0).show();
							}
						
						
						}
						
					});
					dialog.Show();
				 
				 
				}
			}
		});
		
		btnClearAllFavs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DataAdapter db = new DataAdapter(mContext);
				if (isFav) {
					favoritedOverlay.clear();

					ArrayList<String> dropList = new ArrayList<String>();
					dropList.add(getString(R.string.choose));
					favNames = (String[]) dropList.toArray(new String[dropList
							.size()]);

					db.deleteAllFavs();

					clearTempOverlays(Constants.TYPE_FAVORITE);

				} else {
					searchOverlay.clear();

					db.deleteAllNONFavs();

					clearTempOverlays(Constants.TYPE_COMMON);
				}

				mMapView.invalidate();

				listFavs.clear();
				viewAdapterFav.notifyDataSetChanged();
				listViewFav.setAdapter(viewAdapterFav);
			}
		});

		listViewFav.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				selectedFavItem = listFavs.get(arg2);
				selectedFavItemId = arg2;
				return false;
			}
		});

		dialog.show();

	}

	private void showSearchDialog() {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(
				R.layout.alert_dialog_search, null);

		final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(
				R.string.dialogSearchTitle).setView(textEntryView).create();

		final EditText editName = (EditText) textEntryView
				.findViewById(R.id.editName);
		final TextView txtError = (TextView) textEntryView
				.findViewById(R.id.txtError);
		final Button btnOk = (Button) textEntryView.findViewById(R.id.btnOk);
		final Button btnCancel = (Button) textEntryView
				.findViewById(R.id.btnCancle);

		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if ("".equals(editName.getText().toString())) {
					txtError.setText(getString(R.string.errorName));
					txtError.setVisibility(View.VISIBLE);
					return;
				} 
				//else if (!Utils.isNetWorkExist(mContext)) {
				//	txtError.setText(getString(R.string.errorNoDataConnection));
				//	txtError.setVisibility(View.VISIBLE);
				//	return;
				//}
				else {
					dialog.dismiss();
					searchLoading.show();
					Utils.doMapSearch(mContext, editName.getText().toString(),
							mHandler);
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

	private BroadcastReceiver deleteOverlayReceiver = new BroadcastReceiver() {
		public void onReceive(final Context context, final Intent intent) {
			// initAllOverlayItems();
			DataModel dm = getIntentDataModel(intent);

			if (Constants.TYPE_FAVORITE.equals(dm.type)) {
				deleteOverlay(favoritedOverlay, dm.getKey());
			} else {
				deleteOverlay(searchOverlay, dm.getKey());
			}
		}
	};

	private BroadcastReceiver addOverlayReceiver = new BroadcastReceiver() {
		public void onReceive(final Context context, final Intent intent) {

			DataModel dm = getIntentDataModel(intent);

			dm.type = Constants.TYPE_COMMON;

			deleteOverlay(searchOverlay, dm.getKey());

			dm.type = Constants.TYPE_FAVORITE;
			addOverlay(favoritedOverlay, dm, Constants.TYPE_FAVORITE);

		}
	};

	private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
		public void onReceive(final Context context, final Intent intent) {
			initAllOverlayItems();
		}
	};

	private BroadcastReceiver setLocationReceiver = new BroadcastReceiver() {
		public void onReceive(final Context context, final Intent intent) {
			DataModel dm = getIntentDataModel(intent);

			addOverlay(searchOverlay, dm, Constants.TYPE_COMMON);
		}
	};

	private BroadcastReceiver finishReceiver = new BroadcastReceiver() {
		public void onReceive(final Context context, final Intent intent) {
			finish();
		}
	};

	private DataModel getIntentDataModel(Intent it) {
		DataModel dm = new DataModel();
		dm.name = it.getStringExtra(Constants.EXTRA_NAME);
		dm.lat = it.getStringExtra(Constants.EXTRA_LAT);
		dm.lng = it.getStringExtra(Constants.EXTRA_LNG);
		dm.type = it.getStringExtra(Constants.EXTRA_TYPE);

		return dm;
	}

	private void deleteOverlay(MapItemizedOverlay overlay, String key) {
		if (tempOverlays.containsKey(key)) {
			overlay.removeOverlay(tempOverlays.get(key));
			mMapView.invalidate();
			tempOverlays.remove(key);
		}

		if (currentMockLoction.contains(key)) {
			endTestGPS(mContext);
		}
	}

	@Override
	protected void onDestroy() {

		try {
			Preferences.putLastZoomLevel(mMapView.getZoomLevel());
			this.unregisterReceiver(deleteOverlayReceiver);
			this.unregisterReceiver(addOverlayReceiver);
			this.unregisterReceiver(setLocationReceiver);
			this.unregisterReceiver(refreshReceiver);
			this.unregisterReceiver(finishReceiver);
		
		} catch (Exception e) {
			Log.e(LOG_TAG, "" + e.getMessage());
		}

		super.onDestroy();
		ActivityUtils.removeActivity(this);
	}

	
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				searchLoading.dismiss();
				DataModel dm = (DataModel) msg.obj;
				if ("".equals(dm.lat) || "".equals(dm.lng)) {
					Toast.makeText(mContext, getString(R.string.errorNotFound),Toast.LENGTH_LONG);
					return;
				}

				
				 double lat = Double.parseDouble(dm.lat);
				 double lng = Double.parseDouble(dm.lng);

				GeoPoint gp = new GeoPoint((int) (lat * 1000000), (int) (lng * 1000000));
				
				//int lat= (int)Double.parseDouble(dm.lat);
				//int lng= (int)Double.parseDouble(dm.lng);
				

				addOverlay(searchOverlay, dm, "");

				startTestGPS(mContext, dm.name, Double.parseDouble(dm.lat),
						Double.parseDouble(dm.lng));

				mMapController.setCenter(gp);
				mMapController.setCenter(gp);
				if (mMapView.getZoomLevel() == Preferences.getLastZoomLevel()) {
					mMapController.setZoom(13);
				}
				
				DataAdapter db = new DataAdapter(mContext);
				dm.type = Constants.TYPE_COMMON;
				db.insertFav(dm);

			} catch (Exception e) {
				Log.e(LOG_TAG, "mHandler--" + e.getMessage());
			}

		}
	};

	private void addOverlay(MapItemizedOverlay overlay, DataModel dm,
			String which) {

		if (tempOverlays.containsKey(dm.getKey())) {
			mMapController.setCenter(tempOverlays.get(dm.getKey()).getPoint());
			return;
		}

		Log.d(LOG_TAG, "addOverlay--name:" + dm.name + "--lat/lng:" + dm.lat
				+ "/" + dm.lng);
		if ("".equals(dm.lat) || "".equals(dm.lng)) {
			return;
		}
		final double lat = Double.parseDouble(dm.lat);
		final double lng = Double.parseDouble(dm.lng);

		GeoPoint gp = new GeoPoint((int) (lat * 1000000), (int) (lng * 1000000));
		OverlayItem overlayItem = new OverlayItem(gp, dm.name, which);
		overlay.addOverlay(overlayItem);
		tempOverlays.put(dm.getKey(), overlayItem);
		mMapView.invalidate();

		mMapController.setCenter(overlayItem.getPoint());
	}

	@Override

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuItem delMenu = menu.add(0, DELETE_ID, 0, R.string.btnDelete); 
		MenuItem setLocationMenu = menu.add(0, SETLOCATION_ID, 0,
				R.string.btnSet);

		delMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if (selectedFavItem != null) {
					ActivityTools.deleteFav(mContext, selectedFavItem);
					if (selectedFavItemId != -1) {
						listFavs.remove(selectedFavItemId);
					}
					if (viewAdapterFav != null) {
						viewAdapterFav.notifyDataSetChanged();
					}
				}

				return false;
			}
		});

		setLocationMenu
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						if (selectedFavItem != null) {
							startTestGPS(mContext, selectedFavItem.name,
									selectedFavItem.getDoubleLat(),
									selectedFavItem.getDoubleLng());

							ActivityTools
									.setLocation(mContext, selectedFavItem);
						}
						return false;
					}
				});
	}

	private void clearTempOverlays(String type) {
		try {
			Set<String> keys = tempOverlays.keySet();

			if (keys.size() > 0) {
				for (String key : keys) {
					if (key.contains(type)) {
						tempOverlays.remove(key);
					}
				}
			}
		} catch (Exception e) {

		}

	}

	



	static class DialogClick extends DialogClickLisener {
		private Context ctx;

		public DialogClick(Context ctx) {
			this.ctx = ctx;
		}

		@Override
		public void onClick() {
			try {
				ctx.sendBroadcast(new Intent(Constants.FINISH_BROADCAST));
			} catch (Exception e) {
				Log.e("DialogClick", "" + e.getMessage());
			}
		}

	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		if (searchLoading != null && searchLoading.isShowing()) {
			searchLoading.dismiss();
		}
	}



}