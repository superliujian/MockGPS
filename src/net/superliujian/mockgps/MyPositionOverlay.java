package net.superliujian.mockgps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.Projection;



public class MyPositionOverlay extends Overlay {
	private GeoPoint geoPoint;
	private Context context;
	private int drawable;

	public MyPositionOverlay(GeoPoint geoPoint, Context context, int drawable) {
		super();
		this.geoPoint = geoPoint;
		this.context = context;
		this.drawable = drawable;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {

		Projection projection = mapView.getProjection();
		Point point = new Point();
		projection.toPixels(geoPoint, point);

		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
				drawable);
		canvas.drawBitmap(bitmap, point.x - bitmap.getWidth(), point.y
				- bitmap.getHeight(), null);
		super.draw(canvas, mapView, shadow);
	}
}
