package cat.irec.photonbuster;

import java.util.LinkedList;
import java.util.List;

import cat.irec.photonbuster.Device;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1; // Database Version
	private static final String DATABASE_NAME = "DeviceDB"; // Database Name

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) { // SQL statement to create table
		String CREATE_TABLE = "CREATE TABLE devices ( "
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "name TEXT, "
				+ "ip TEXT, " + "port INTEGER, " + "type INTEGER, "
				+ "enable INTEGER )";

		Log.d("createDevice", CREATE_TABLE);
		// create table
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS devices"); // Drop older devices table
													// if existed
		this.onCreate(db); // create fresh devices table
	}

	// ---------------------------------------------------------------------

	/**
	 * CRUD operations (create "add", read "get", update, delete) device + get
	 * all + delete all devices
	 */

	// Devices table name
	private static final String TABLE_NAME = "devices";

	// Devices Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	private static final String KEY_IP = "ip";
	private static final String KEY_PORT = "port";
	private static final String KEY_TYPE = "type";
	private static final String KEY_ENABLE = "enable";

	private static final String[] COLUMNS = { KEY_ID, KEY_NAME, KEY_IP,
			KEY_PORT, KEY_TYPE, KEY_ENABLE };

	public void addDevice(Device device) {
		Log.d("addDevice", device.toString());
		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		// get values
		values.put(KEY_NAME, device.getName()); // get name
		values.put(KEY_IP, device.getIp()); // get ip
		values.put(KEY_PORT, device.getPort()); // get port
		values.put(KEY_TYPE, device.getType()); // get type
		values.put(KEY_ENABLE, device.getEnable()); // get Enable
		// 3. insert
		db.insert(TABLE_NAME, /* table */null, /* nullColumnHack */values); // key/value
																			// ->
																			// keys
																			// =
																			// column
																			// names/
																			// values
																			// =
																			// column
																			// values
		// 4. close
		db.close();
	}

	public Device getDevice(int id) { // 1. get reference to readable DB
		SQLiteDatabase db = this.getReadableDatabase();
		// 2. build query
		Cursor cursor = db.query(TABLE_NAME, // a. table
				COLUMNS, // b. column names
				" id = ?", // c. selections
				new String[] { String.valueOf(id) }, // d. selections args
				null, // e. group by
				null, // f. having
				null, // g. order by
				null); // h. limit
		// 3. if we got results get the first one
		if (cursor != null)
			cursor.moveToFirst();
		// 4. build device object
		Device device = new Device();
		device.setId(Integer.parseInt(cursor.getString(0))); // ID
		device.setName(cursor.getString(1)); // Name
		device.setIp(cursor.getString(2)); // IP
		device.setPort(Integer.parseInt(cursor.getString(3))); // PORT
		device.setType(Integer.parseInt(cursor.getString(4))); // TYPE
		device.setEnable(Integer.parseInt(cursor.getString(5)));// ENABLE
		// --
		Log.d("getDevice(" + id + ")", device.toString());
		// 5. return device
		return device;
	}

	// Get All devices
	public List<Device> getAllDevices() {
		List<Device> devices = new LinkedList<Device>();
		// 1. build the query
		String query = "SELECT * FROM " + TABLE_NAME;
		// 2. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		// 3. go over each row, build device and add it to list
		Device device = null;
		if (cursor.moveToFirst())
			do {
				device = new Device();
				device.setId(Integer.parseInt(cursor.getString(0))); // ID
				device.setName(cursor.getString(1)); // Name
				device.setIp(cursor.getString(2)); // IP
				device.setPort(Integer.parseInt(cursor.getString(3))); // PORT
				device.setType(Integer.parseInt(cursor.getString(4))); // TYPE
				device.setEnable(Integer.parseInt(cursor.getString(5)));// ENABLE
				// Add device to devices
				devices.add(device);
			} while (cursor.moveToNext());

		Log.d("getAllSpectros()", devices.toString());
		// return devices
		return devices;
	}

	// Get All devices
	public List<Device> getAllSpectros() {
		List<Device> devices = new LinkedList<Device>();
		// 1. build the query
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_TYPE
				+ " = 1";
		// 2. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		// 3. go over each row, build device and add it to list
		Device device = null;
		if (cursor.moveToFirst())
			do {
				device = new Device();
				device.setId(Integer.parseInt(cursor.getString(0))); // ID
				device.setName(cursor.getString(1)); // Name
				device.setIp(cursor.getString(2)); // IP
				device.setPort(Integer.parseInt(cursor.getString(3))); // PORT
				device.setType(Integer.parseInt(cursor.getString(4))); // TYPE
				device.setEnable(Integer.parseInt(cursor.getString(5)));// ENABLE
				// Add device to devices
				devices.add(device);
			} while (cursor.moveToNext());

		Log.d("getAllDevices()", devices.toString());
		// return devices
		return devices;
	}

	// Get All devices
	public List<Device> getAllLuminaries() {
		List<Device> devices = new LinkedList<Device>();
		// 1. build the query
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_TYPE
				+ " = 0";
		// 2. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		// 3. go over each row, build device and add it to list
		Device device = null;
		if (cursor.moveToFirst())
			do {
				device = new Device();
				device.setId(Integer.parseInt(cursor.getString(0))); // ID
				device.setName(cursor.getString(1)); // Name
				device.setIp(cursor.getString(2)); // IP
				device.setPort(Integer.parseInt(cursor.getString(3))); // PORT
				device.setType(Integer.parseInt(cursor.getString(4))); // TYPE
				device.setEnable(Integer.parseInt(cursor.getString(5)));// ENABLE
				// Add device to devices
				devices.add(device);
			} while (cursor.moveToNext());

		Log.d("getAllLuminaries()", devices.toString());
		// return devices
		return devices;
	}

	// Updating single device
	public int updateDevice(Device device) {
		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, device.getName()); // get name
		values.put(KEY_IP, device.getIp()); // get ip
		values.put(KEY_PORT, device.getPort()); // get port
		values.put(KEY_TYPE, device.getType()); // get type
		values.put(KEY_ENABLE, device.getEnable()); // get enable
		// 3. updating row
		int i = db.update(TABLE_NAME, /* table */values, /* column/value */
				KEY_ID + " = ?", /* selections */
				new String[] { String.valueOf(device.getId()) }); // selection
																	// args
		// 4. close
		db.close();
		return i;

	}

	// Deleting single device
	public void deleteDevice(Device device) {
		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		// 2. delete
		db.delete(TABLE_NAME, KEY_ID + " = ?",
				new String[] { String.valueOf(device.getId()) });
		// 3. close
		db.close();
		Log.d("deleteDevice", device.toString());
	}
}
