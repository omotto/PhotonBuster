package cat.irec.photonbuster;

import android.app.ListFragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import cat.irec.photonbuster.R;

public class FragmentFive extends ListFragment {
	
    private File currentDir;
    private FileArrayAdapter adapter;
    private Stack<File> dirStack = new Stack<File>();

    public static final String SOURCE = "/sdcard/";
//    private View view;
    
	public FragmentFive() {

	}

	//
	// GET CALIBRATION FILE
	//


	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDir = new File(SOURCE); //Environment.getExternalStorageDirectory().getPath()
        fill(currentDir);
	        
    }
        
    private void fill(File f) {
    	File[]dirs = f.listFiles();
		getActivity().setTitle("Current Dir: "+f.getName());
		List<Option>dir = new ArrayList<Option>();
		List<Option>fls = new ArrayList<Option>();
		try{
			for(File ff: dirs) {
				if(ff.isDirectory()) {
					dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
				} else {
					fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
				}
			}
		} catch(Exception e) {
			Log.d("FileChooser", "Error: " + e);
		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		if (!f.getName().equalsIgnoreCase("sdcard")) dir.add(0,new Option("..","Parent Directory",f.getParent()));
		adapter = new FileArrayAdapter(getActivity(),R.layout.fragment_layout_five, dir);
		this.setListAdapter(adapter);
    }
    
    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
		Option o = adapter.getItem(position);
		if (o.getData().equalsIgnoreCase("folder")) {
			dirStack.push(currentDir);
			currentDir = new File(o.getPath());
			fill(currentDir);
		} else {
			if (o.getData().equalsIgnoreCase("parent directory")) {
				currentDir = dirStack.pop();
				fill(currentDir);
			} else {
				onFileClick(o);
			}
		}
	}

    private void onFileClick(Option o) {
    	Log.d("FILE","Carpeta actual: "+currentDir);
    	if (o.getName().endsWith(".json")) {
    		Toast.makeText(getActivity(), "File " + o.getName() + " loaded", Toast.LENGTH_SHORT).show();
    		// Find APP path
    		PackageManager m = getActivity().getPackageManager();
    		String s = getActivity().getPackageName();
    		try {
    			PackageInfo p = m.getPackageInfo(s, 0);
    			s = p.applicationInfo.dataDir;
    		} catch (NameNotFoundException e) {
    			Log.d("FILE", "Error Package name not found ", e);
    		}
    		Log.d("FILE", "File "+s+o.getName());
    		// Copy file
            File source = new File(currentDir, o.getName());
        	File destination = new File(s,"calibration.clb");
        	InputStream in = null;
       		OutputStream out = null;
         
       		try {
       			in = new FileInputStream(source);
       			out = new FileOutputStream(destination);
       			byte[] buffer = new byte[1024];
       			int length;
       			while ((length = in.read(buffer)) > 0) out.write(buffer, 0, length);
           	    in.close();
           	    out.close();
       		} catch (IOException e) {
				Log.d("FILE","ERROR: "+e);
				e.printStackTrace();
       		}
    	} else {
    		Toast.makeText(getActivity(), "File Clicked: "+o.getName(), Toast.LENGTH_SHORT).show();
    	}
    }
}