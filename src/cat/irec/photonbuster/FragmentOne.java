package cat.irec.photonbuster;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;
import cat.irec.photonbuster.R;

public class FragmentOne extends ListFragment {
/*
	ImageView ivIcon;
	TextView tvItemName;

	public static final String IMAGE_RESOURCE_ID = "iconResourceID";
	public static final String ITEM_NAME = "itemName";
*/
	
	Device deviceSelected;
	MyArrayAdapter adapter;
	MySQLiteHelper db;
	
	public FragmentOne() {

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);


		db = new MySQLiteHelper(getActivity());

		List<Device> devices = new LinkedList<Device>();
		devices = db.getAllLuminaries();

		adapter = new MyArrayAdapter(getActivity(), R.layout.fragment_layout_one, devices);

		setListAdapter(adapter);
		
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
				deviceSelected = adapter.getItem(pos);
            	// REMOVE DIALOG 
            	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            	// Add the buttons
            	builder.setTitle("Remove File");
            	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int id) {
            			adapter.remove(deviceSelected);
    	       	        adapter.notifyDataSetChanged();
    	       	        db.deleteDevice(deviceSelected);
    	       		}
            	});
            	builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int id) {
            			// User cancelled the dialog
            		}
            	});
            	// Create the AlertDialog
            	AlertDialog dialog = builder.create();
            	dialog.show();
            	dialog.setTitle("Remove File");
				return true;
            }       
        });
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// do something with the data
		Toast.makeText(getActivity(), getListView().getItemAtPosition(position).toString(),	Toast.LENGTH_LONG).show();
	}

}
