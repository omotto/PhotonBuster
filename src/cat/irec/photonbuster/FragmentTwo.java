package cat.irec.photonbuster;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYPlot;

import cat.irec.photonbuster.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class FragmentTwo extends Fragment {

	// -- Graph
	private XYPlot mySimpleXYPlot;
	XYSeries serie;
	Number[] y_axis;
	
	// -- Checkbox and Sliders 
	SeekBar sk1, sk2, sk3, sk4, sk5, sk6, sk7, sk8, sk9, sk10, sk11, sk12;
	CheckBox checkbox;
	
	// -- Device Calibration Values
	Number[][] channels = null;
	
	// -- List of devices connected
	//private List<Device> devices = new LinkedList<Device>();
	
	// -- Data to send through Sockets
	int length; 
	byte[] buffer = new byte[1024];
	
	// --- List of active Sockets
	//List<Socket> sockets = new LinkedList<Socket>();
	
	
	public FragmentTwo() {

	}

	/*
	 * @Override public void onCreate(Bundle savedInstanceState) {
	 * super.onCreate(savedInstanceState); }
	 */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_layout_two, container, false);
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
		buffer[0] = 0x02; // STX
		buffer[1] = 0x69; // IND
		for (int c = 2; c < 34; c++) buffer[c] = 0x00; // DATA
		buffer[34] = 0x03; // ETX
		length = 35;		
		// ------------------
		// Reading Calibration File
		// ------------------
		// Find APP path
		PackageManager m = getActivity().getPackageManager();
		String s = getActivity().getPackageName();
		try {
			PackageInfo p = m.getPackageInfo(s, 0);
			s = p.applicationInfo.dataDir;
		} catch (NameNotFoundException e) {
			Log.d("FILE", "Error Package name not found ", e);
		}
		// Read Data
		// Read Data
		String fileContent = null;
		int numChannels;
		File file = new File(s,"calibration.clb");
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			try {
    			StringBuilder sb = new StringBuilder();
    			String line = null;
    			while ((line = br.readLine()) != null) sb.append(line).append("\n");
    			fileContent = sb.toString();
    			br.close();
    			JSONObject reader = null;
    			try { 
    				reader = new JSONObject(fileContent); 
    	    		JSONArray LED_spectros = reader.getJSONArray("spectra");
    		    	numChannels = reader.getInt("nChannels");
    		    	channels = new Number[numChannels][81];
    		    	for (int i = 0; i < numChannels; i++) {
    		    		JSONArray jchannel = LED_spectros.getJSONArray(i);
    		    		for (int j = 0; j < 81; j++) channels[i][j] = jchannel.getDouble(j);
    		    	}
    			} catch (JSONException e1) {	
    				Log.d("FILE","ERROR: "+e1);
    				Toast.makeText(view.getContext(), "ERROR : " + e1, Toast.LENGTH_LONG).show();
    				e1.printStackTrace(); 
    			}
    		} catch (IOException e2) {
				Log.d("FILE","ERROR: "+e2);
				Toast.makeText(view.getContext(), "ERROR : " + e2, Toast.LENGTH_LONG).show();
    			e2.printStackTrace();
    		}
    	} catch (FileNotFoundException e) {
    		Toast.makeText(view.getContext(), "ERROR: "+e, Toast.LENGTH_SHORT).show();
    		Log.d("FILE","ERROR: "+e);
			e.printStackTrace();
		} 
		// --------------------
		// Prepare Button
		// --------------------
		Button btnSubmit = (Button) view.findViewById(R.id.button1);
		btnSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// ---------------------------
				// Create dialog to save file
				// ---------------------------					
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				// Get the layout inflater
				LayoutInflater dialog_inflater = getActivity().getLayoutInflater();
				// Inflate and set the layout for the dialog
			    // Pass null as the parent view because its going in the dialog layout
				builder.setTitle("Set Spectro Name");
				builder.setView(dialog_inflater.inflate(R.layout.dialog_layout, null))
			    // Add action buttons
		           .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		        	   @Override
		        	   public void onClick(DialogInterface dialog, int id) {
		        		    Dialog f = (Dialog) dialog;
		                    //This is the input I can't get text from
		                    EditText filename = (EditText) f.findViewById(R.id.filename);
		                    // Get APP directory
		        			PackageManager m = getActivity().getPackageManager();
		        			String s = getActivity().getPackageName();
		        			try {
		        				PackageInfo p = m.getPackageInfo(s, 0);
		        				s = p.applicationInfo.dataDir;
		        				Log.d("Save File",filename.getText().toString()+".spm"); 
				        		   	// Save file
		        				//FileOutputStream outputStream;
		        				File file = null;
		        				try {
		        					//outputStream = getActivity().openFileOutput(filename.getText().toString()+".spm", Context.MODE_PRIVATE);
			        				file = new File(s,filename.getText().toString()+".spm"); 
			        			   	//PrintStream printStream = new PrintStream(outputStream);
			        			   	PrintStream printStream = new PrintStream(file);
			        			   	printStream.print(sk1.getProgress()+"\r\n"+sk2.getProgress() +"\r\n"+ sk3.getProgress() +"\r\n"+
			        			   					  sk4.getProgress() +"\r\n"+ sk5.getProgress() +"\r\n"+ sk6.getProgress() +"\r\n"+
			        			   					  sk7.getProgress() +"\r\n"+ sk8.getProgress() +"\r\n"+ sk9.getProgress() +"\r\n"+
			        			   					  sk10.getProgress() +"\r\n"+ sk11.getProgress() +"\r\n"+ sk12.getProgress());
			        			   	printStream.close();
			        			   	//outputStream.close();
			        			 } catch (Exception e) {
			        				 Log.d("Save File","ERROR: "+e);
			        				 e.printStackTrace();
			        			 }
		        			} catch (NameNotFoundException e) {
		        				Log.d("FILE", "Error Package name not found ", e);
		        			}
		        	   }
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// Exit
						}
					});      
				 builder.create().show();
			}
		});
		// --------------
		// Prepare View
		// --------------
		// CheckBox
		checkbox = (CheckBox) view.findViewById(R.id.checkBox1);
		// Sliders
		sk1 = (SeekBar) view.findViewById(R.id.seekBar1);
		sk2 = (SeekBar) view.findViewById(R.id.seekBar2);
		sk3 = (SeekBar) view.findViewById(R.id.seekBar3);
		sk4 = (SeekBar) view.findViewById(R.id.seekBar4);
		sk5 = (SeekBar) view.findViewById(R.id.seekBar5);
		sk6 = (SeekBar) view.findViewById(R.id.seekBar6);
		sk7 = (SeekBar) view.findViewById(R.id.seekBar7);
		sk8 = (SeekBar) view.findViewById(R.id.seekBar8);
		sk9 = (SeekBar) view.findViewById(R.id.seekBar9);
		sk10 = (SeekBar) view.findViewById(R.id.seekBar10);
		sk11 = (SeekBar) view.findViewById(R.id.seekBar11);
		sk12 = (SeekBar) view.findViewById(R.id.seekBar12);
		
		if (channels != null) {
			sk1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
			    @Override       
			    public void onStopTrackingTouch(SeekBar seekBar) {      
			    	int progress = seekBar.getProgress();
			    	// -- Debug
			    	Log.d("sk1", String.valueOf(progress));
			    	Toast.makeText(getActivity(), "sk1: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();
			    	// -- Change Values
			    	buffer[2] = (byte) ((progress >> 8)&0xFF);
			    	buffer[3] = (byte) (progress&0xFF);
			    	// -- Send Data 
			    	if (checkbox.isChecked()) { 
			    		// ------------------
    					Thread send_thread = new Thread(new SocketThread());
    		            send_thread.start();
			    		// ------------------	    		
			    	}
			    	// -- Calculate new spectro
			    	int value = 0;
			    	Number[] x_axis = new Number[81];
			    	for (int c = 0; c < 81; c++) x_axis[c] = 0;
			    	for (int i = 0; i < 12; i++) {
		    			value = (buffer[2+i*2] & 0x0ff);
		    			value = (value << 8);
		    			value =  value + ((buffer[3+(i*2)]) & 0xff);
		    			for (int j = 0; j < 81; j++) {
			    			x_axis[j] = x_axis[j].floatValue() + (channels[i][j].floatValue() * value);
			    		}
			    	}
			    	// -- Plot
					mySimpleXYPlot.clear();
					serie = new SimpleXYSeries(Arrays.asList(y_axis), Arrays.asList(x_axis), "Spectro"); 
					LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, null);
					mySimpleXYPlot.addSeries(serie, seriesFormat);
					mySimpleXYPlot.redraw();
			    }       
			    @Override       
			    public void onStartTrackingTouch(SeekBar seekBar) {}       
			    @Override       
			    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}       
			});
	
			sk2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
			    @Override       
			    public void onStopTrackingTouch(SeekBar seekBar) {      
			    	int progress = seekBar.getProgress();
			    	
			    	Log.d("sk2", String.valueOf(progress));
			    	Toast.makeText(getActivity(), "sk2: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();
	
			    	buffer[4] = (byte) ((progress >> 8)&0xFF);
			    	buffer[5] = (byte) (progress&0xFF);
			    	
			    	if (checkbox.isChecked()) { 
			    		// ------------------
    					Thread send_thread = new Thread(new SocketThread());
    		            send_thread.start();
			    		// ------------------	    		
			    	}
			    	// -- Calculate new spectro
			    	int value = 0;
			    	Number[] x_axis = new Number[81];
			    	for (int c = 0; c < 81; c++) x_axis[c] = 0;
			    	for (int i = 0; i < 12; i++) {
		    			value = (buffer[2+i*2] & 0x0ff);
		    			value = (value << 8);
		    			value =  value + ((buffer[3+(i*2)]) & 0xff);
			    		for (int j = 0; j < 81; j++) {
			    			x_axis[j] = x_axis[j].floatValue() + (channels[i][j].floatValue() * value);
			    		}
			    	}
			    	// -- Plot
					mySimpleXYPlot.clear();
					serie = new SimpleXYSeries(Arrays.asList(y_axis), Arrays.asList(x_axis), "Spectro"); 
					LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, null);
					mySimpleXYPlot.addSeries(serie, seriesFormat);
					mySimpleXYPlot.redraw();
			    }       
			    @Override       
			    public void onStartTrackingTouch(SeekBar seekBar) {}       
			    @Override       
			    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}       
			});		
		
			sk3.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
			    @Override       
			    public void onStopTrackingTouch(SeekBar seekBar) {      
			    	int progress = seekBar.getProgress();
			    	
			    	Log.d("sk3", String.valueOf(progress));
			    	Toast.makeText(getActivity(), "sk3: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();
			    	
			    	buffer[6] = (byte) ((progress >> 8)&0xFF);
			    	buffer[7] = (byte) (progress&0xFF);
	
			    	if (checkbox.isChecked()) { 
			    		// ------------------
    					Thread send_thread = new Thread(new SocketThread());
    		            send_thread.start();
			    		// ------------------	    		
			    	}
			    	// -- Calculate new spectro
			    	int value = 0;
			    	Number[] x_axis = new Number[81];
			    	for (int c = 0; c < 81; c++) x_axis[c] = 0;
			    	for (int i = 0; i < 12; i++) {
		    			value = (buffer[2+i*2] & 0x0ff);
		    			value = (value << 8);
		    			value =  value + ((buffer[3+(i*2)]) & 0xff);
			    		for (int j = 0; j < 81; j++) {
			    			x_axis[j] = x_axis[j].floatValue() + (channels[i][j].floatValue() * value);
			    		}
			    	}
			    	// -- Plot
					mySimpleXYPlot.clear();
					serie = new SimpleXYSeries(Arrays.asList(y_axis), Arrays.asList(x_axis), "Spectro"); 
					LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, null);
					mySimpleXYPlot.addSeries(serie, seriesFormat);
					mySimpleXYPlot.redraw();
			    }       
			    @Override       
			    public void onStartTrackingTouch(SeekBar seekBar) {}       
			    @Override       
			    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}       
			});	
			
			sk4.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
			    @Override       
			    public void onStopTrackingTouch(SeekBar seekBar) {      
			    	int progress = seekBar.getProgress();
			    	
			    	Log.d("sk4", String.valueOf(progress));
			    	Toast.makeText(getActivity(), "sk4: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();
			    	
			    	buffer[8] = (byte) ((progress >> 8)&0xFF);
			    	buffer[9] = (byte) (progress&0xFF);
	
			    	if (checkbox.isChecked()) { 
			    		// ------------------
    					Thread send_thread = new Thread(new SocketThread());
    		            send_thread.start();
			    		// ------------------	    		
			    	}
			    	// -- Calculate new spectro
			    	int value = 0;
			    	Number[] x_axis = new Number[81];
			    	for (int c = 0; c < 81; c++) x_axis[c] = 0;
			    	for (int i = 0; i < 12; i++) {
		    			value = (buffer[2+i*2] & 0x0ff);
		    			value = (value << 8);
		    			value =  value + ((buffer[3+(i*2)]) & 0xff);
			    		for (int j = 0; j < 81; j++) {
			    			x_axis[j] = x_axis[j].floatValue() + (channels[i][j].floatValue() * value);
			    		}
			    	}
			    	// -- Plot
					mySimpleXYPlot.clear();
					serie = new SimpleXYSeries(Arrays.asList(y_axis), Arrays.asList(x_axis), "Spectro"); 
					LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, null);
					mySimpleXYPlot.addSeries(serie, seriesFormat);
					mySimpleXYPlot.redraw();
			    }       
			    @Override       
			    public void onStartTrackingTouch(SeekBar seekBar) {}       
			    @Override       
			    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}       
			});			
			
			sk5.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
			    @Override       
			    public void onStopTrackingTouch(SeekBar seekBar) {      
			    	int progress = seekBar.getProgress();
			    	
			    	Log.d("sk5", String.valueOf(progress));
			    	Toast.makeText(getActivity(), "sk5: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();
			    	
			    	buffer[10] = (byte) ((progress >> 8)&0xFF);
			    	buffer[11] = (byte) (progress&0xFF);
	
			    	if (checkbox.isChecked()) { 
			    		// ------------------
    					Thread send_thread = new Thread(new SocketThread());
    		            send_thread.start();
			    		// ------------------	    		
			    	}
			    	// -- Calculate new spectro
			    	int value = 0;
			    	Number[] x_axis = new Number[81];
			    	for (int c = 0; c < 81; c++) x_axis[c] = 0;
			    	for (int i = 0; i < 12; i++) {
		    			value = (buffer[2+i*2] & 0x0ff);
		    			value = (value << 8);
		    			value =  value + ((buffer[3+(i*2)]) & 0xff);
			    		for (int j = 0; j < 81; j++) {
			    			x_axis[j] = x_axis[j].floatValue() + (channels[i][j].floatValue() * value);
			    		}
			    	}
			    	// -- Plot
					mySimpleXYPlot.clear();
					serie = new SimpleXYSeries(Arrays.asList(y_axis), Arrays.asList(x_axis), "Spectro"); 
					LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, null);
					mySimpleXYPlot.addSeries(serie, seriesFormat);
					mySimpleXYPlot.redraw();
			    }       
			    @Override       
			    public void onStartTrackingTouch(SeekBar seekBar) {}       
			    @Override       
			    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}       
			});			
			
			sk6.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
			    @Override       
			    public void onStopTrackingTouch(SeekBar seekBar) {      
			    	int progress = seekBar.getProgress();
			    	
			    	Log.d("sk6", String.valueOf(progress));
			    	Toast.makeText(getActivity(), "sk6: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();
			    	
			    	buffer[12] = (byte) ((progress >> 8)&0xFF);
			    	buffer[13] = (byte) (progress&0xFF);
	
			    	if (checkbox.isChecked()) { 
			    		// ------------------
    					Thread send_thread = new Thread(new SocketThread());
    		            send_thread.start();
			    		// ------------------	    		
			    	}
			    	// -- Calculate new spectro
			    	int value = 0;
			    	Number[] x_axis = new Number[81];
			    	for (int c = 0; c < 81; c++) x_axis[c] = 0;
			    	for (int i = 0; i < 12; i++) {
		    			value = (buffer[2+i*2] & 0x0ff);
		    			value = (value << 8);
		    			value =  value + ((buffer[3+(i*2)]) & 0xff);
			    		for (int j = 0; j < 81; j++) {
			    			x_axis[j] = x_axis[j].floatValue() + (channels[i][j].floatValue() * value);
			    		}
			    	}
			    	// -- Plot
					mySimpleXYPlot.clear();
					serie = new SimpleXYSeries(Arrays.asList(y_axis), Arrays.asList(x_axis), "Spectro"); 
					LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, null);
					mySimpleXYPlot.addSeries(serie, seriesFormat);
					mySimpleXYPlot.redraw();
			    }       
			    @Override       
			    public void onStartTrackingTouch(SeekBar seekBar) {}       
			    @Override       
			    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}       
			});			
			
			sk7.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
			    @Override       
			    public void onStopTrackingTouch(SeekBar seekBar) {      
			    	int progress = seekBar.getProgress();
			    	
			    	Log.d("sk7", String.valueOf(progress));
			    	Toast.makeText(getActivity(), "sk7: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();
			    	
			    	buffer[14] = (byte) ((progress >> 8)&0xFF);
			    	buffer[15] = (byte) (progress&0xFF);
	
			    	if (checkbox.isChecked()) { 
			    		// ------------------
    					Thread send_thread = new Thread(new SocketThread());
    		            send_thread.start();
			    		// ------------------	    		
			    	}
			    	// -- Calculate new spectro
			    	int value = 0;
			    	Number[] x_axis = new Number[81];
			    	for (int c = 0; c < 81; c++) x_axis[c] = 0;
			    	for (int i = 0; i < 12; i++) {
		    			value = (buffer[2+i*2] & 0x0ff);
		    			value = (value << 8);
		    			value =  value + ((buffer[3+(i*2)]) & 0xff);
			    		for (int j = 0; j < 81; j++) {
			    			x_axis[j] = x_axis[j].floatValue() + (channels[i][j].floatValue() * value);
			    		}
			    	}
			    	// -- Plot
					mySimpleXYPlot.clear();
					serie = new SimpleXYSeries(Arrays.asList(y_axis), Arrays.asList(x_axis), "Spectro"); 
					LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, null);
					mySimpleXYPlot.addSeries(serie, seriesFormat);
					mySimpleXYPlot.redraw();
			    }       
			    @Override       
			    public void onStartTrackingTouch(SeekBar seekBar) {}       
			    @Override       
			    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}       
			});			
			
			sk8.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
			    @Override       
			    public void onStopTrackingTouch(SeekBar seekBar) {      
			    	int progress = seekBar.getProgress();
			    	
			    	Log.d("sk8", String.valueOf(progress));
			    	Toast.makeText(getActivity(), "sk8: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();
			    	
			    	buffer[16] = (byte) ((progress >> 8)&0xFF);
			    	buffer[17] = (byte) (progress&0xFF);
	
			    	if (checkbox.isChecked()) { 
			    		// ------------------
    					Thread send_thread = new Thread(new SocketThread());
    		            send_thread.start();
			    		// ------------------	    		
			    	}
			    	// -- Calculate new spectro
			    	int value = 0;
			    	Number[] x_axis = new Number[81];
			    	for (int c = 0; c < 81; c++) x_axis[c] = 0;
			    	for (int i = 0; i < 12; i++) {
		    			value = (buffer[2+i*2] & 0x0ff);
		    			value = (value << 8);
		    			value =  value + ((buffer[3+(i*2)]) & 0xff);
			    		for (int j = 0; j < 81; j++) {
			    			x_axis[j] = x_axis[j].floatValue() + (channels[i][j].floatValue() * value);
			    		}
			    	}
			    	// -- Plot
					mySimpleXYPlot.clear();
					serie = new SimpleXYSeries(Arrays.asList(y_axis), Arrays.asList(x_axis), "Spectro"); 
					LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, null);
					mySimpleXYPlot.addSeries(serie, seriesFormat);
					mySimpleXYPlot.redraw();
			    }       
			    @Override       
			    public void onStartTrackingTouch(SeekBar seekBar) {}       
			    @Override       
			    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}       
			});
			
			
			sk9.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
			    @Override       
			    public void onStopTrackingTouch(SeekBar seekBar) {      
			    	int progress = seekBar.getProgress();
			    	
			    	Log.d("sk9", String.valueOf(progress));
			    	Toast.makeText(getActivity(), "sk9: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();
			    	
			    	buffer[18] = (byte) ((progress >> 8)&0xFF);
			    	buffer[19] = (byte) (progress&0xFF);
	
			    	if (checkbox.isChecked()) { 
			    		// ------------------
    					Thread send_thread = new Thread(new SocketThread());
    		            send_thread.start();
			    		// ------------------	    		
			    	}
			    	// -- Calculate new spectro
			    	int value = 0;
			    	Number[] x_axis = new Number[81];
			    	for (int c = 0; c < 81; c++) x_axis[c] = 0;
			    	for (int i = 0; i < 12; i++) {
		    			value = (buffer[2+i*2] & 0x0ff);
		    			value = (value << 8);
		    			value =  value + ((buffer[3+(i*2)]) & 0xff);
			    		for (int j = 0; j < 81; j++) {
			    			x_axis[j] = x_axis[j].floatValue() + (channels[i][j].floatValue() * value);
			    		}
			    	}
			    	// -- Plot
					mySimpleXYPlot.clear();
					serie = new SimpleXYSeries(Arrays.asList(y_axis), Arrays.asList(x_axis), "Spectro"); 
					LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, null);
					mySimpleXYPlot.addSeries(serie, seriesFormat);
					mySimpleXYPlot.redraw();
			    }       
			    @Override       
			    public void onStartTrackingTouch(SeekBar seekBar) {}       
			    @Override       
			    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}       
			});
			
			sk10.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
			    @Override       
			    public void onStopTrackingTouch(SeekBar seekBar) {      
			    	int progress = seekBar.getProgress();
			    	
			    	Log.d("sk10", String.valueOf(progress));
		    		Toast.makeText(getActivity(), "sk10: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();
	
		    		buffer[20] = (byte) ((progress >> 8)&0xFF);
			    	buffer[21] = (byte) (progress&0xFF);
	
			    	if (checkbox.isChecked()) { 
			    		// ------------------
    					Thread send_thread = new Thread(new SocketThread());
    		            send_thread.start();
			    		// ------------------	    		
			    	}
			    	// -- Calculate new spectro
			    	int value = 0;
			    	Number[] x_axis = new Number[81];
			    	for (int c = 0; c < 81; c++) x_axis[c] = 0;
			    	for (int i = 0; i < 12; i++) {
		    			value = (buffer[2+i*2] & 0x0ff);
		    			value = (value << 8);
		    			value =  value + ((buffer[3+(i*2)]) & 0xff);
			    		for (int j = 0; j < 81; j++) {
			    			x_axis[j] = x_axis[j].floatValue() + (channels[i][j].floatValue() * value);
			    		}
			    	}
			    	// -- Plot
					mySimpleXYPlot.clear();
					serie = new SimpleXYSeries(Arrays.asList(y_axis), Arrays.asList(x_axis), "Spectro"); 
					LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, null);
					mySimpleXYPlot.addSeries(serie, seriesFormat);
					mySimpleXYPlot.redraw();
			    }       
			    @Override       
			    public void onStartTrackingTouch(SeekBar seekBar) {}       
			    @Override       
			    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}       
			});	
			
			sk11.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
			    @Override       
			    public void onStopTrackingTouch(SeekBar seekBar) {      
			    	int progress = seekBar.getProgress();
			    	
			    	Log.d("sk11", String.valueOf(progress));
			    	Toast.makeText(getActivity(), "sk11: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();
			    	
			    	buffer[22] = (byte) ((progress >> 8)&0xFF);
			    	buffer[23] = (byte) (progress&0xFF);
	
			    	if (checkbox.isChecked()) { 
			    		// ------------------
    					Thread send_thread = new Thread(new SocketThread());
    		            send_thread.start();
			    		// ------------------	    		
			    	}
			    	// -- Calculate new spectro
			    	int value = 0;
			    	Number[] x_axis = new Number[81];
			    	for (int c = 0; c < 81; c++) x_axis[c] = 0;
			    	for (int i = 0; i < 12; i++) {
		    			value = (buffer[2+i*2] & 0x0ff);
		    			value = (value << 8);
		    			value =  value + ((buffer[3+(i*2)]) & 0xff);
			    		for (int j = 0; j < 81; j++) {
			    			x_axis[j] = x_axis[j].floatValue() + (channels[i][j].floatValue() * value);
			    		}
			    	}
			    	// -- Plot
					mySimpleXYPlot.clear();
					serie = new SimpleXYSeries(Arrays.asList(y_axis), Arrays.asList(x_axis), "Spectro"); 
					LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, null);
					mySimpleXYPlot.addSeries(serie, seriesFormat);
					mySimpleXYPlot.redraw();
			    }       
			    @Override       
			    public void onStartTrackingTouch(SeekBar seekBar) {}       
			    @Override       
			    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}       
			});			
			
			sk12.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
			    @Override       
			    public void onStopTrackingTouch(SeekBar seekBar) {      
			    	int progress = seekBar.getProgress();
			    	
			    	Log.d("sk12", String.valueOf(progress));
			    	Toast.makeText(getActivity(), "sk12: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();
			    	
			    	buffer[24] = (byte) ((progress >> 8)&0xFF);
			    	buffer[25] = (byte) (progress&0xFF);
	
			    	if (checkbox.isChecked()) { 
			    		// ------------------
    					Thread send_thread = new Thread(new SocketThread());
    		            send_thread.start();
			    		// ------------------	    		
			    	}
			    	// -- Calculate new spectro
			    	int value = 0;
			    	Number[] x_axis = new Number[81];
			    	for (int c = 0; c < 81; c++) x_axis[c] = 0;
			    	for (int i = 0; i < 12; i++) {
		    			value = (buffer[2+i*2] & 0x0ff);
		    			value = (value << 8);
		    			value =  value + ((buffer[3+(i*2)]) & 0xff);
			    		for (int j = 0; j < 81; j++) {
			    			x_axis[j] = x_axis[j].floatValue() + (channels[i][j].floatValue() * value);
			    		}
			    	}
			    	// -- Plot
					mySimpleXYPlot.clear();
					serie = new SimpleXYSeries(Arrays.asList(y_axis), Arrays.asList(x_axis), "Spectro"); 
					LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, null);
					mySimpleXYPlot.addSeries(serie, seriesFormat);
					mySimpleXYPlot.redraw();
			    }       
			    @Override       
			    public void onStartTrackingTouch(SeekBar seekBar) {}       
			    @Override       
			    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}       
			});			
		}
		// -----------------
		// GraphWindow
		// -----------------		
		// initialize our XYPlot reference:
		mySimpleXYPlot = (XYPlot) view.findViewById(R.id.mySimpleXYPlot);
		// Setting Y axis
		y_axis = new Number[81];
		// Setting X axis
		Number[] x_axis = new Number[81];
		for (int c = 0; c < 81; c++) {
			y_axis[c] = 380+c*5;
			x_axis[c] = 0;
		}
		// seriesNumbers = NumberFormat.parse();		
		// Turn the above arrays into XYSeries':
		serie = new SimpleXYSeries(Arrays.asList(y_axis), // SimpleXYSeries takes a List so turn our array into a List
								   Arrays.asList(x_axis),/*SimpleXYSeries.ArrayFormat.Y_VALS_ONLY*/ // Y_VALS_ONLY means use the element index as the x value
							       "Spectro"); // Set the display title of the series
		
		// Create a formatter to use for drawing a series using LineAndPointRenderer:
		LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), // line color
		    														   Color.rgb(0, 100, 0), // point color
																	   null, // fill color (none)
																	   null/*new PointLabelFormatter(Color.WHITE)*/); // text color
		// add a new series' to the xyplot:
		mySimpleXYPlot.addSeries(serie, seriesFormat);
		// same as above:
		//mySimpleXYPlot.addSeries(series2, new LineAndPointFormatter(Color.rgb(0, 0, 200), Color.rgb(0, 0,100), null, new PointLabelFormatter(Color.WHITE)));
		// reduce the number of range labels
		mySimpleXYPlot.setTicksPerRangeLabel(3);

		return view;
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
			        Log.d("TCP Client", "C: Sent.");
			    } catch (Exception e) {
			        Log.d("TCP", "S: Error", e);
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
