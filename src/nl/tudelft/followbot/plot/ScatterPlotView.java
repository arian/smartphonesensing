package nl.tudelft.followbot.plot;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;

public class ScatterPlotView extends View {

	private final ArrayList<ShapeDrawable> mDrawables = new ArrayList<ShapeDrawable>();

	public ScatterPlotView(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		for (ShapeDrawable md : mDrawables) {
			md.draw(canvas);
		}
	}

	private void createShapes(int length) {
		if (length > mDrawables.size()) {
			for (int i = mDrawables.size(); i < length; i++) {
				ShapeDrawable mDrawable = new ShapeDrawable(new OvalShape());
				mDrawable.getPaint().setColor(0xff74AC23);
				mDrawables.add(mDrawable);
			}
		} else if (mDrawables.size() > length) {
			for (int i = length; i < mDrawables.size(); i++) {
				mDrawables.remove(i);
			}
		}
	}

	public void plot(double[][] data) {
		assert data.length == 3 : "Data should have 3 dimensions";

		double[] x = data[0];
		double[] y = data[1];
		double[] w = data[2];

		double minX = Float.MAX_VALUE;
		double maxX = Float.MIN_VALUE;
		double minY = Float.MAX_VALUE;
		double maxY = Float.MIN_VALUE;

		for (int i = 0; i < x.length; i++) {
			minX = Math.min(minX, x[i]);
			maxX = Math.max(maxX, x[i]);
			minY = Math.min(minY, y[i]);
			maxY = Math.max(maxY, y[i]);
		}

		createShapes(x.length);

		int width = getWidth();
		int height = getHeight();

		int i = 0;
		for (ShapeDrawable md : mDrawables) {
			int _x = (int) ((x[i] - minX) / (maxX - minX) * width);
			int _y = (int) ((y[i] - minY) / (maxY - minY) * height);
			md.setBounds(_x, _y, _x + 10, _y + 10);
			i++;
		}

		invalidate();
	}
}
