package cat.irec.photonbuster;

import java.io.BufferedInputStream;
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
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import cat.irec.photonbuster.R;
import cat.irec.photonbuster.NNLSSolver.Matrix;

public class FragmentFour extends Fragment {

	private Spinner spinner1;
	private Button btnSubmit, btnRemove;
	
	// List of devices (spectro meter object)
	List<Device> devices = new LinkedList<Device>();
	Device selectedDevice;
	MySQLiteHelper db;
	
	View  view;
	
	private XYPlot mySimpleXYPlot;
	Number[] x_axis = null;
	Number[] y_axis = null;
	double [][] solution; 
	
	
	ClientThread client_thread  = null;
	
	public FragmentFour() {}

	/*
	 * GET FROM SPECTROPHOTOMETER
	 */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_layout_four, container, false);

		// DataBase
		db = new MySQLiteHelper(view.getContext());
		// Spinner linked to XML
		spinner1 = (Spinner) view.findViewById(R.id.spinner1);
		// List of Strings
		List<String> list = new ArrayList<String>();
		// Get data from DataBase
		devices = db.getAllSpectros();
		// Convert data to String 
		for (Device device : devices) list.add(device.toString());
		// Array Adapter for Spinner
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Set Adapter
		spinner1.setAdapter(dataAdapter);
		// Spinner item selection Listener
		spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
		// Button 2 => Remove Action
		btnRemove = (Button) view.findViewById(R.id.button2);
		// Button function
		btnRemove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((ArrayAdapter<String>) spinner1.getAdapter()).remove(selectedDevice.toString());
				((ArrayAdapter<String>) spinner1.getAdapter()).notifyDataSetChanged();
				db.deleteDevice(selectedDevice);
			}
		});
		// Button
		btnSubmit = (Button) view.findViewById(R.id.button1);
		// Button function
		btnSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				double[][] channels = null; 
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
					Toast.makeText(view.getContext(), "ERROR : " + e, Toast.LENGTH_LONG).show();
					Log.d("FILE", "Error Package name not found ", e);
				}
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
		    		    	channels = new double[81][numChannels];
		    		    	for (int i = 0; i < numChannels; i++) {
		    		    		JSONArray jchannel = LED_spectros.getJSONArray(i);
		    		    		for (int j = 0; j < 81; j++) channels[j][i] = jchannel.getDouble(j);
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
				
				if ((channels != null) && (x_axis != null)) {
					Matrix A = new Matrix(channels, 81, 12);
					// Get Formated SpectroPhotoMeter data
					double [][] spectro = new double[81][1];
					int c;
					for (int i = 0; i < 81; i++) {
						c = 0;
						for (int j = 0; j < 234; j++)
							if (Math.abs((380+i*5) - y_axis[j].floatValue()) < Math.abs((380+i*5) - y_axis[c].floatValue())) c = j; 
						spectro[i][0] = x_axis[c].floatValue()/65535;
					}
					Matrix b = new Matrix(spectro,81,1);
					Matrix xNNLS = NNLSSolver.solveNNLS(A,b);
					solution = xNNLS.getArray();
					
					
					for (int i = 0; i < 12; i++) solution[i][0] = solution[i][0] * 4096 / 100;
					
					Toast.makeText(view.getContext(), "SOL : " + Math.ceil(solution[0][0]) +"\r\n"+ Math.ceil(solution[1][0]) +"\r\n"+ Math.ceil(solution[2][0]) +"\r\n"+
																 Math.ceil(solution[3][0]) +"\r\n"+ Math.ceil(solution[4][0]) +"\r\n"+ Math.ceil(solution[5][0]) +"\r\n"+
																 Math.ceil(solution[6][0]) +"\r\n"+ Math.ceil(solution[7][0]) +"\r\n"+ Math.ceil(solution[8][0]) +"\r\n"+
																 Math.ceil(solution[9][0]) +"\r\n"+ Math.ceil(solution[10][0]) +"\r\n"+ Math.ceil(solution[11][0]) +"\r\n"+
																 Math.ceil(solution[12][0]), Toast.LENGTH_LONG).show();
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
				        			   	printStream.print((int)Math.ceil(solution[0][0]) +"\r\n"+ (int)Math.ceil(solution[1][0]) +"\r\n"+ (int)Math.ceil(solution[2][0]) +"\r\n"+
				        			   					  (int)Math.ceil(solution[3][0]) +"\r\n"+ (int)Math.ceil(solution[4][0]) +"\r\n"+ (int)Math.ceil(solution[5][0]) +"\r\n"+
				        			   					  (int)Math.ceil(solution[6][0]) +"\r\n"+ (int)Math.ceil(solution[7][0]) +"\r\n"+ (int)Math.ceil(solution[8][0]) +"\r\n"+
				        			   					  (int)Math.ceil(solution[9][0]) +"\r\n"+ (int)Math.ceil(solution[10][0]) +"\r\n"+ (int)Math.ceil(solution[11][0]) +"\r\n"+
				        			   					  (int)Math.ceil(solution[12][0]));
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
			}
		});
		return view;
	}

	
	public class CustomOnItemSelectedListener implements OnItemSelectedListener 
	{	@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
		{	//Toast.makeText(parent.getContext(), "Select : \n" + parent.getItemAtPosition(pos),Toast.LENGTH_LONG).show();
			Toast.makeText(parent.getContext(), "Select : \n" + devices.get(pos).getName(),Toast.LENGTH_LONG).show();
			selectedDevice = devices.get(pos);
			if (client_thread != null) client_thread.requestStop();
			client_thread = new ClientThread(devices.get(pos));
			client_thread.start();
						
		}
	  
		@Override 
		public void onNothingSelected(AdapterView<?> arg0) 
		{ 
			 
		}
	}
	
	class ClientThread extends Thread {
		private Device spectro;
		private volatile boolean exit = false;
		
		ClientThread (Device device) {
			this.spectro = device;					
		}

		public void requestStop() {
			this.exit = true;
		}		
		
		@Override
		public void run() {	
			
				BufferedOutputStream out;
				BufferedInputStream in;				
				byte[] out_buffer = new byte[100];
				byte[] in_buffer = new byte[512];
				int value;
				float restador;
				try {
			    	InetAddress serverAddr = InetAddress.getByName(spectro.getIp());
			        Log.d("TCP Client", "Connecting...");
			        Socket socket = new Socket(serverAddr, spectro.getPort());
			        socket.setSoTimeout(99999); // Read Time-Out in miliseconds 
			        try {
			        	mySimpleXYPlot = (XYPlot) view.findViewById(R.id.mySimpleXYPlot);
			        	LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, null);
			        	mySimpleXYPlot.setTicksPerRangeLabel(3);
			        	// receive the message which the server sends back
			        	in = new BufferedInputStream(socket.getInputStream());
			            in.read(in_buffer, 0, 512); // Read "HELLO\r"
			        	// send the message to the server			        	
			            out = new BufferedOutputStream(socket.getOutputStream());	
			            while (exit == false) {
			            	out_buffer[0] = (byte)(0xAA);
			            	out.write(out_buffer, 0, 1);
			            	out.flush();
			            	Log.d("TCP Client", "Sent.");
				            /* ---------------------------------------------------------------------------------
				              	Espectro tiene un margen de 340 - 780 y solo nos interesa mostrar de 380 - 780
				            	Descartamos las primeras 22 muestras a razón de: (780-340)/256 = 1,71875nm
				            	23 muestras x  1,7185nm = 40nm
				            	Estos 40nm nos marcan el Offset que hemos de eliminar 
				               --------------------------------------------------------------------------------- */
			            	//in this while the client listens for the messages sent by the server			            	
			            	in.read(in_buffer,0, 512); // Get Data
				            // Plot 
				            restador = 0;
				            y_axis = new Number[234]; // 256 - 22 Samples
				            x_axis = new Number[234]; // 256 - 22 Samples
					    	for (int i = 0; i < 234; i++) y_axis[i] = 380 + (float)(i*1.71875);
					    	for (int i = 0; i < 256; i++) {
					    		value = (in_buffer[i*2] & 0x00ff);
				    			value = (value << 8);
				    			value =  value + ((in_buffer[1+(i*2)]) & 0x00ff);
					            if (i < 22) {
					            	if (i == 0) restador = value; else restador = (restador + value)/2;
					            	//Log.d("TCP Client","value["+i+"] : "+value+"//"+restador+" ValueH: "+(in_buffer[i*2] & 0x00ff)+" ValueL: "+((in_buffer[1+(i*2)]) & 0x00ff));
					            } else {
					            	if (restador > value) x_axis[i-22] = 0; else x_axis[i-22] = value - restador;
					            	//Log.d("TCP Client","data["+i+"] : "+x_axis[i-22]+"//"+restador+" ValueH: "+(in_buffer[i*2] & 0x00ff)+" ValueL: "+((in_buffer[1+(i*2)]) & 0x00ff)+" Value: "+value);
					            }
					    	}
					    	mySimpleXYPlot.clear();
					    	XYSeries serie = new SimpleXYSeries(Arrays.asList(y_axis), Arrays.asList(x_axis), "Spectro");
							mySimpleXYPlot.addSeries(serie, seriesFormat);
							mySimpleXYPlot.redraw();
			            }
			            in.close();
			            out.close();	
			            
			        } catch (Exception e) {
			            Log.d("TCP", "Server Error", e);
			        } finally {
			            socket.close();
			        }
			    } catch (Exception e) {
			    	Log.d("TCP", "Client Error", e);
			    }
			
			Log.d("THREAD","EXIT!!!");
		}
	}
	
	@Override
	public void onDestroy() {
        super.onDestroy();
		// ------------------
        if (client_thread != null) client_thread.requestStop();	
		// ------------------
        Log.d("TCP Client", "onDestroy");
    }
}

