package cat.irec.photonbuster;

import cat.irec.photonbuster.ColorPickerDialog;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

public class FragmentSeven extends Fragment implements ColorPickerDialog.OnColorChangedListener {

	protected int _color = Color.GREEN;

	private Paint mPaint;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPaint.setColor(Color.BLUE);
		new ColorPickerDialog(getActivity(), this, mPaint.getColor()).show();
	}

	@Override
	public void colorChanged(int color) {
		mPaint.setColor(color);
		_color = color;
 	}

}