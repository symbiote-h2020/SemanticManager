package eu.h2020.symbiote.utils;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity model for caching location information.
 *
 * Created by Szymon Mueller on 28/06/2018.
 */
@Document
public class LocationInfo {

    @Id
    private String locationUri;

    private String platformId;

    private double latitude;

    private double longitude;

    private double altitude;

    public LocationInfo() {
    }

    public LocationInfo(String locationUri, String platformId, double latitude, double longitude, double altitude) {
        this.locationUri = locationUri;
        this.platformId = platformId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public String getLocationUri() {
        return locationUri;
    }

    public void setLocationUri(String locationUri) {
        this.locationUri = locationUri;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationInfo that = (LocationInfo) o;

        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (Double.compare(that.altitude, altitude) != 0) return false;
        if (locationUri != null ? !locationUri.equals(that.locationUri) : that.locationUri != null) return false;
        return platformId != null ? platformId.equals(that.platformId) : that.platformId == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = locationUri != null ? locationUri.hashCode() : 0;
        result = 31 * result + (platformId != null ? platformId.hashCode() : 0);
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(altitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "LocationInfo{" +
                "locationUri='" + locationUri + '\'' +
                ", platformId='" + platformId + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                '}';
    }
}
