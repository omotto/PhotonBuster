package cat.irec.photonbuster;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import cat.irec.photonbuster.R;

public class MainActivity extends Activity {
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	CustomDrawerAdapter adapter;

	List<DrawerItem> dataList;

	int current_fragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
			
		// Initializing
		dataList = new ArrayList<DrawerItem>();
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,	GravityCompat.START);

		// Add Drawer Item to dataList
		// dataList.add(new DrawerItem("Messages"));
		dataList.add(new DrawerItem(R.drawable.images));
		dataList.add(new DrawerItem("Play List", R.drawable.ic_action_good));
		dataList.add(new DrawerItem("SPECTRO"));
		//dataList.add(new DrawerItem("Read Spectro", R.drawable.ic_action_video));
		dataList.add(new DrawerItem("Set LED Channels",	R.drawable.ic_action_settings));
		//dataList.add(new DrawerItem("Set from RGB",	R.drawable.ic_action_gamepad));
		dataList.add(new DrawerItem("CONFIGURATION"));
		dataList.add(new DrawerItem("Select Luminaries",R.drawable.ic_action_labels));
		dataList.add(new DrawerItem("Set Devices", R.drawable.ic_action_group));
		dataList.add(new DrawerItem("Import Calibration", R.drawable.ic_action_import_export));
		// dataList.add(new DrawerItem("HELP "));
		// dataList.add(new DrawerItem("Help ", R.drawable.ic_action_help));
		// dataList.add(new DrawerItem("About", R.drawable.ic_action_about));
		// dataList.add(new DrawerItem("Exit ", R.drawable.ic_action_cloud));

		adapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item, dataList);

		mDrawerList.setAdapter(adapter);

		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,	R.drawable.ic_drawer, R.string.drawer_open,	R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			if (dataList.get(0).getItemName() == null) {
				SelectItem(1);
			} else {
				SelectItem(0);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void SelectItem(int possition) {

		current_fragment = possition; 
		
		Log.d("MainActivity", "SelectItem(" + possition + ")");
		
		Fragment fragment = null;
		
		Bundle args = new Bundle();
		switch (possition) {
		case 0:
			// Nothing Image
			break;
		case 1: // PlayList
			fragment = new FragmentSix();
			break;
		case 2:
			// Nothing Text
			break;
		case 3: // Read SpectroPhotoMeter
/*			fragment = new FragmentFour();
			break;
		case 4: // Sliders*/
			fragment = new FragmentTwo();
			break;
/*		case 5: // Add device
			fragment = new FragmentSeven();
//			args.putString(FragmentThree.ITEM_NAME, dataList.get(possition).getItemName());
//			args.putInt(FragmentThree.IMAGE_RESOURCE_ID, dataList.get(possition).getImgResID());
			break;*/
		case 4:
			// Nothing Text
			break;
		case 5: // Select Luminaries
			fragment = new FragmentOne();
/*			args.putString(FragmentOne.ITEM_NAME, dataList.get(possition).getItemName());
			args.putInt(FragmentOne.IMAGE_RESOURCE_ID, dataList.get(possition).getImgResID());*/
			break;
		case 6: // Add Device
			fragment = new FragmentThree();
			break;
		case 7: // Get Calibration file
			fragment = new FragmentFive();
			break;
		default:
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
			break;
		}

		fragment.setArguments(args);
		FragmentManager frgManager = getFragmentManager();
		frgManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

		mDrawerList.setItemChecked(possition, true);
		setTitle(dataList.get(possition).getItemName());
		mDrawerLayout.closeDrawer(mDrawerList);

	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		/*
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return false;
		*/
		// Cuando pulsas el boton del navigator drawing
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Cuandoe stás en las opciones 
	    switch (item.getItemId()) {
	    	case R.id.action_remove:
//	    		if (current_fragment == 7)
	    			
	    		//Toast.makeText(this, "REMOVING " + current_fragment, Toast.LENGTH_SHORT).show();
	    		return true;
	    	case R.id.action_help:
	    		
	    		//Toast.makeText(this, "HELPING" + current_fragment, Toast.LENGTH_SHORT).show();
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
	    }

	}
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
			if (dataList.get(position).getItemName() != null) {
				SelectItem(position);
			} // If there is a text do not do anything
		}
	}

}
