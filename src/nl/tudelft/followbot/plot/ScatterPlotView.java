package nl.tudelft.followbot.plot;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;

public class ScatterPlotView extends View {

	private final ArrayList<ShapeDrawable> mDrawables = new ArrayList<ShapeDrawable>();
	private final ShapeDrawable origin = new ShapeDrawable();
	private String minMaxText = "(0,0)";

	private final Paint textPaint;

	public ScatterPlotView(Context context) {
		super(context);
		textPaint = new Paint();
		textPaint.setColor(Color.BLUE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		origin.draw(canvas);
		for (ShapeDrawable md : mDrawables) {
			md.draw(canvas);
		}
		canvas.drawText(minMaxText, canvas.getWidth() - 60,
				canvas.getHeight() - 40, textPaint);
	}

	private void createShapes(int length) {
		if (length > mDrawables.size()) {
			for (int i = mDrawables.size(); i < length; i++) {
				ShapeDrawable mDrawable = new ShapeDrawable(new OvalShape());
				mDrawable.getPaint().setColor(0xff74AC23);
				mDrawables.add(mDrawable);
			}
		} else if (mDrawables.size() > length) {
			while (mDrawables.size() > length && mDrawables.size() > 0) {
				mDrawables.remove(mDrawables.size() - 1);
			}
		}
	}

	public void plot(double[][] data) {
		assert data.length == 3 : "Data should have 3 dimensions";

		double[] x = data[0];
		double[] y = data[1];
		double[] w = data[2];

		double max = Float.MIN_VALUE;

		for (int i = 0; i < x.length; i++) {
			max = Math.max(max, Math.abs(x[i]));
			max = Math.max(max, Math.abs(y[i]));
		}

		double min = 0 - max;

		createShapes(x.length);

		int width = getWidth();
		int height = getHeight();

		int i = 0;
		for (ShapeDrawable md : mDrawables) {
			int _x = (int) ((x[i] - min) / (max - min) * width);
			int _y = (int) ((y[i] - min) / (max - min) * height);
			_y = height - _y; // minimum starts at the bottom of the screen
			md.setBounds(_x - 5, _y - 5, _x + 5, _y + 5);
			md.getPaint().setColor(blend(0xff74AC23, Color.MAGENTA, w[i]));
			i++;
		}

		minMaxText = String.format("(%2.2f, %2.2f)", min, max);

		origin.getPaint().setColor(Color.BLACK);
		origin.setBounds(width / 2 - 10, height / 2 - 10, width / 2 + 10,
				height / 2 + 10);

		invalidate();
	}

	static int blend(int color1, int color2, double frac) {
		int r1 = Color.red(color1);
		int g1 = Color.green(color1);
		int b1 = Color.blue(color1);
		int r2 = Color.red(color2);
		int g2 = Color.green(color2);
		int b2 = Color.blue(color2);
		int r3 = (int) (r1 * (1.0 - frac) + r2 * frac);
		int g3 = (int) (g1 * (1.0 - frac) + g2 * frac);
		int b3 = (int) (b1 * (1.0 - frac) + b2 * frac);
		return Color.rgb(r3, g3, b3);
	}

}
