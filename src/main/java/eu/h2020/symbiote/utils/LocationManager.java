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
    public LocationManager(@Value("semantic.insert.whole.location.for.existing") boolean insertWholeLocation,
                           LocationRepository locationRepo) {
        this.insertWholeLocation = insertWholeLocation;
        this.locationRepo = locationRepo;
    }


}
