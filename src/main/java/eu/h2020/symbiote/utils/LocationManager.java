package eu.h2020.symbiote.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.parsing.Location;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Szymon Mueller on 29/06/2018.
 */
@Component
public class LocationManager {

    private static final Log log = LogFactory.getLog(LocationManager.class);

    private final boolean insertWholeLocation;
    private final LocationRepository locationRepo;

    @Autowired
//    public LocationManager(@Value("semantic.insert.whole.location.for.existing") boolean insertWholeLocation,
    public LocationManager(
                           LocationRepository locationRepo) {
//        this.insertWholeLocation = insertWholeLocation;
        this.insertWholeLocation = true;
        this.locationRepo = locationRepo;
    }


    public Optional<LocationInfo> findExistingLocation(String name, String platformId, double latitude, double longitude, double altitude) {
        List<LocationInfo> platformLocationsList = locationRepo.findByPlatformId(platformId);
        List<LocationInfo> equalLocations = platformLocationsList.stream().filter(locationInfo -> locationDetailsAreEqual(locationInfo, new LocationInfo(null, platformId, latitude, longitude, altitude))).collect(Collectors.toList());
        switch (equalLocations.size()) {
            case 1: return Optional.of(equalLocations.get(0));
            default: if(equalLocations.size() > 1 ) {
                log.debug("Size of location entries fulfilling the equality is more than 1: " + equalLocations.size() + ". Returning first one found.");
                return Optional.of(equalLocations.get(0));
            } else {
                return Optional.empty();
            }
        }
    }

    public boolean locationDetailsAreEqual( LocationInfo location1, LocationInfo location2 ) {
        if( location1 == null || location2 == null ) return false;
        if (location1 == location2) return true;

        if (Double.compare(location1.getLatitude(), location2.getLatitude()) != 0) return false;
        if (Double.compare(location1.getLongitude(), location2.getLongitude()) != 0) return false;
        if (Double.compare(location1.getAltitude(), location2.getAltitude()) != 0) return false;
        return location1.getPlatformId() != null ? location1.getPlatformId().equals(location2.getPlatformId()) : location2.getPlatformId() == null;
    }


}
