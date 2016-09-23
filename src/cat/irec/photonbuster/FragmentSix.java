package cat.irec.photonbuster;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;
import cat.irec.photonbuster.R;
import cat.irec.photonbuster.FragmentTwo.SocketThread;

public class FragmentSix extends ListFragment {
	
    private File currentDir;
    private FileArrayAdapter adapter;
    private Stack<File> dirStack = new Stack<File>();

    Option fd;
    
    public String SOURCE;

	// -- List of devices connected
	//private List<Device> devices = new LinkedList<Device>();
	
	// -- Data to send through Sockets
	int length; 
	byte[] buffer = new byte[1024];
	
	// --- List of active Sockets
	//List<Socket> sockets = new LinkedList<Socket>();
	
    
	public FragmentSix() {

	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		/*
        // ------------------
		// load DataBase
		// ------------------
		MySQLiteHelper db = new MySQLiteHelper(getActivity());
		devices = db.getAllLuminaries();
		// ------------------
		// starting sockets
		// ------------------
		Thread start_thread = new Thread(new OpenSocketThread());
        start_thread.start();*/		
		// ------------------
		// Preparamos la trama a enviar
        // ------------------
		buffer[0] = 0x02; // STX
		buffer[1] = 0x69; // IND
		for (int c = 2; c < 34; c++) buffer[c] = 0x00; // DATA
		buffer[34] = 0x03; // ETX
		length = 35;		
		// -----------------
        // Find APP path
        //-----------------
		PackageManager m = getActivity().getPackageManager();
		SOURCE = getActivity().getPackageName();
		try {
			PackageInfo p = m.getPackageInfo(SOURCE, 0);
			SOURCE = p.applicationInfo.dataDir;
	        currentDir = new File(SOURCE);
	        fill(currentDir);
		} catch (NameNotFoundException e) {
			Log.d("FILE", "Error Package name not found ", e);
		}
    }
	
    private void fill(File f) {
    	File[]dirs = f.listFiles();
		List<Option>dir = new ArrayList<Option>();
		List<Option>fls = new ArrayList<Option>();
		
		try{
			for(File ff: dirs) {
				if ( (!ff.isDirectory()) && (ff.getName().endsWith(".spm"))) {
					fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
				}
			}
		} catch(Exception e) {
			Log.d("FileChooser", "Error: " + e);
		}
		//Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		//if (!f.getName().equalsIgnoreCase(SOURCE)) dir.add(0,new Option("..","Parent Directory",f.getParent()));
		adapter = new FileArrayAdapter(getActivity(),R.layout.fragment_layout_five, dir);
		this.setListAdapter(adapter);
    }
    
	@Override    
    public void onActivityCreated(Bundle savedInstanceState) {    
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
            	fd = adapter.getItem(pos);
            	// REMOVE DIALOG 
            	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            	// Add the buttons
            	builder.setTitle("Remove File");
            	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int id) {
            			// Find APP path
    	       			PackageManager m = getActivity().getPackageManager();
    	       			String s = getActivity().getPackageName();
    	       			try {
    	       				PackageInfo p = m.getPackageInfo(s, 0);
    	       				s = p.applicationInfo.dataDir;
    	       				File file = new File(s+"/"+fd.getName());
    	       				file.delete();
    	                	adapter.remove(fd);
    	       	            adapter.notifyDataSetChanged();
    	       			} catch (NameNotFoundException e) {
    	       				Log.d("FILE", "Error Package name not found ", e);
    	       			}	
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
    	if (o.getName().endsWith(".spm")) {
    		Toast.makeText(getActivity(), "File " + o.getName() + " loaded", Toast.LENGTH_SHORT).show();
    		// Find APP path
    		PackageManager m = getActivity().getPackageManager();
    		String s = getActivity().getPackageName();
    		int value, c = 2;
    		try {
    			PackageInfo p = m.getPackageInfo(s, 0);
    			s = p.applicationInfo.dataDir;
	    		// Read Data
	    		try {
	    			BufferedReader br = new BufferedReader(new FileReader(s+"/"+o.getName()));
	    		    String line = br.readLine();
	   		        while (line != null) {
   		            	Log.d("Read File", "Reading: "+ Integer.valueOf(line));
   		            	value = Integer.valueOf(line);
   				    	buffer[c] = (byte) ((value>>8)&0xFF);
   				    	c++;
   				    	buffer[c] = (byte) (value&0xFF);
   				    	c++;
   				    	// -- Read New Line
	   		        	line = br.readLine();
	   		        }
	   		        br.close();
	   		        // ------------------
	   		        // -- Send data!!!!
	   		        // ------------------
					Thread send_thread = new Thread(new SocketThread());
		            send_thread.start();		
		    		// ------------------	
	    		} catch (Exception e) {
	    			e.printStackTrace();
				    Log.d("Read File", "Error: " + e);
				}
    		} catch (NameNotFoundException e) {
    			Log.d("FILE", "Error Package name not found ", e);
    		}
    	}
    }
    
	// Open Send and close sockets to all selected devices 
	public class SocketThread implements Runnable {
		@Override
		public void run() {
			List<Socket> sockets = new ArrayList<Socket>();
			List<Device> devices = new ArrayList<Device>();
			MySQLiteHelper db = new MySQLiteHelper(getActivity());
			devices = db.getAllLuminaries();
			// -- Open
			for (Device device : devices) {
				if (device.getEnable() == 1) {
					try {
						InetAddress serverAddr = InetAddress.getByName(device.getIp());
					    Log.d("TCP Client", "Connecting...");
					    Socket socket = new Socket(serverAddr, device.getPort());
					    sockets.add(socket);
					    Log.d("TCP Client", "Connected");
					} catch (Exception e) {
					    Log.d("TCP", "Error: " + e);
					}
				}
			}
			// -- Send
			BufferedOutputStream out;
			for (Socket socket : sockets) {
				try {
			        out = new BufferedOutputStream(socket.getOutputStream());	
			        out.write(buffer, 0, length);
			        out.flush(); 
			        Log.d("TCP Client", "C: Sent.");
			    } catch (Exception e) {
			        Log.d("TCP", "S: Error", e);
			    }
			}
			// -- Close
			for (Socket socket : sockets) {
				try {
				    Log.d("TCP Client", "Closing...");
				    socket.close();
				    Log.d("TCP Client", "Closed");
				} catch (Exception e) {
				    Log.d("TCP", "Error: " + e);
				}
			}
	    }
	}
	
    /*
	// --------------------------------------------------------------------------------------------
	public class OpenSocketThread implements Runnable {
		@Override
		public void run() {
			for (Device device : devices) {
				if (device.getEnable() == 1) {
					try {
						InetAddress serverAddr = InetAddress.getByName(device.getIp());
					    Log.d("TCP Client", "Connecting...");
					    Socket socket = new Socket(serverAddr, device.getPort());
					    sockets.add(socket);
					    Log.d("TCP Client", "Connected");
					} catch (Exception e) {
					    Log.d("TCP", "Error: " + e);
					}
				}
			}
	    }
	}
	// --------------------------------------------------------------------------------------------
	public class SendSocketThread implements Runnable {
		@Override
		public void run() {
			BufferedOutputStream out;
			for (Socket socket : sockets) {
				try {
			        out = new BufferedOutputStream(socket.getOutputStream());	
			        out.write(buffer, 0, length);
			        out.flush(); 
			        //out.close();
			        Log.d("TCP Client", "C: Sent.");
			    } catch (Exception e) {
			        Log.d("TCP", "S: Error", e);
			    }
			}
	    }
	}	
	// --------------------------------------------------------------------------------------------
	public class CloseSocketThread implements Runnable {
		@Override
		public void run() {
			for (Socket socket : sockets) {
				try {
				    Log.d("TCP Client", "Closing...");
				    socket.close();
				    Log.d("TCP Client", "Closed");
				} catch (Exception e) {
				    Log.d("TCP", "Error: " + e);
				}
			}
	    }
	}*/
	// --------------------------------------------------------------------------------------------
	@Override
	public void onDestroy() {
        super.onDestroy();
		/*
        // ------------------
		Thread stop_thread = new Thread(new CloseSocketThread());
        stop_thread.start();		
		// ------------------
		*/
        Log.d("TCP Client", "onDestroy");
    }
	// --------------------------------------------------------------------------------------------
}