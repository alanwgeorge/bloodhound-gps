package com.alangeorge.android.bloodhound;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.alangeorge.android.bloodhound.model.LocationDiff;

import org.jetbrains.annotations.Nullable;

/**
 * For our location diff layout we need to calculate the distance between the start and end points.  This is done by
 * overriding the {@link com.alangeorge.android.bloodhound.LocationDiffCursorAdaptor#getView(int, android.view.View, android.view.ViewGroup)} method.
 */
public class LocationDiffCursorAdaptor extends SimpleCursorAdapter {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = "LocationDiffCursorAdaptor";

    public LocationDiffCursorAdaptor(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    /**
     * For our location diff layout we need to calculate the distance between the start and end points.  The distance
     * is calculated by first getting the view from super and then using the points on the view to calculate the distance
     * in meters.  Then we set the distance value on the layout.
     * <p/>
     * We also set a {@link com.alangeorge.android.bloodhound.model.LocationDiff} as a {@link android.view.View#setTag(int, Object)}
     * on the View we return.  This allows us access to the model object form the {
     * @link com.alangeorge.android.bloodhound.LocationDiffFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)}
     * in order to have access to the required data to start the {@link com.alangeorge.android.bloodhound.MapDetailActivity} for this LocationDiff.
     *
     * @param position current ListView position
     * @param convertView preserved previous View for reuse if possible
     * @param parent parent View
     * @return the full constructed View
     */
    @Nullable
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result;

        result = super.getView(position, convertView, parent);

        // here we set a convenience data object on the view for easy access to data about this LocationDiff item
        result.setTag(R.id.location_diff_view_tag, LocationDiff.cursorToLocationDiff(getCursor()));

        // after getting the populated view from super, we calculate as set the distance TextView
        if (result.getId() == R.id.diff_list_item) {
            TextView distanceTextView;
            float distance;
            double latitude1, longitude1, latitude2, longitude2;

            distanceTextView = (TextView) result.findViewById(R.id.distanceTextView);
            latitude1 = Double.parseDouble(((TextView) result.findViewById(R.id.latitude1TextView)).getText().toString());
            longitude1 =  Double.parseDouble(((TextView) result.findViewById(R.id.longitude1TextView)).getText().toString());
            latitude2 =  Double.parseDouble(((TextView) result.findViewById(R.id.latitude2TextView)).getText().toString());
            longitude2 =  Double.parseDouble(((TextView) result.findViewById(R.id.longitude2TextView)).getText().toString());

            // calculate the distance in meters
            Location loc1 = new Location("internal");
            Location loc2 = new Location("internal");
            loc1.setLatitude(latitude1);
            loc1.setLongitude(longitude1);
            loc2.setLatitude(latitude2);
            loc2.setLongitude(longitude2);

            distance = loc1.distanceTo(loc2);

            distanceTextView.setText(Float.toString(distance));
        }

        return result;
    }
}
