package com.alangeorge.android.bloodhound.model;

public class LocationDiff {
    private long id;


    private Location fromLocation;
    private Location toLocation;
    private float latitudeDiff;
    private float longitudeDiff;

    public Location getFromLocation() {
        return fromLocation;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setFromLocation(Location fromLocation) {
        this.fromLocation = fromLocation;
    }

    public Location getToLocation() {
        return toLocation;
    }

    public void setToLocation(Location toLocation) {
        this.toLocation = toLocation;
    }

    @SuppressWarnings("UnusedDeclaration")
    public float getLatitudeDiff() {
        return latitudeDiff;
    }

    public void setLatitudeDiff(float latitudeDiff) {
        this.latitudeDiff = latitudeDiff;
    }

    @SuppressWarnings("UnusedDeclaration")
    public float getLongitudeDiff() {
        return longitudeDiff;
    }

    public void setLongitudeDiff(float longitudeDiff) {
        this.longitudeDiff = longitudeDiff;
    }

    @Override
    public String toString() {
        return "LocationDiff{" +
                "id=" + id +
                ", fromLocation=" + fromLocation +
                ", toLocation=" + toLocation +
                ", latitudeDiff=" + latitudeDiff +
                ", longitudeDiff=" + longitudeDiff +
                '}';
    }
}
