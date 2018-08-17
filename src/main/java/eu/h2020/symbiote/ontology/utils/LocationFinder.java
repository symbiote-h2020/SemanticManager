//package eu.h2020.symbiote.ontology.utils;
//
//import eu.h2020.symbiote.utils.LocationInfo;
//import eu.h2020.symbiote.utils.LocationRepository;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
///**
// * Utility class for finding existing locations to reuse the URIs
// *
// * Created by Szymon Mueller on 05/05/2017.
// */
//public class LocationFinder {
//
//    private static final Log log = LogFactory.getLog(LocationFinder.class);
//
//    private static LocationFinder singleton = null;
//
////    private final String resourceExchange;
////
////    private final String sparqlBindingKey;
////
////    private Channel channel;
////
////    private String durableResponseQueueName;
////
////    private RabbitManager rabbitManager;
//
////    private LocationFinder(String resourceExchange, String sparqlBindingKey, Connection connection, RabbitManager rabbitManager ) {
////        this.resourceExchange = resourceExchange;
////        this.sparqlBindingKey = sparqlBindingKey;
////        this.rabbitManager = rabbitManager;
////        try {
////            channel = connection.createChannel();
////            this.durableResponseQueueName = this.channel.queueDeclare("symbIoTe-SemanticManager-searchLocationLookup",true,true,false,null).getQueue();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////
////    }
//
//    private final boolean insertWholeLocation;
//    private final LocationRepository locationRepo;
//
//    private LocationFinder( boolean insertWholeLocation,
//                            LocationRepository locationRepo) {
//        this.insertWholeLocation = insertWholeLocation;
//        this.locationRepo = locationRepo;
//    }
//
//    public static LocationFinder getSingleton(boolean insertWholeLocation, LocationRepository locationRepository) {
//        synchronized( LocationFinder.class ) {
//            if( singleton == null ) {
//                singleton = new LocationFinder(insertWholeLocation,locationRepository);
//            }
//            return singleton;
//        }
//    }
//
//    public static LocationFinder getSingleton() {
//        return singleton;
//    }
//
//
//    public Optional<LocationInfo> findExistingLocation(String name, String platformId, double latitude, double longitude, double altitude) {
//        List<LocationInfo> platformLocationsList = locationRepo.findByPlatformId(platformId);
//        List<LocationInfo> equalLocations = platformLocationsList.stream().filter(locationInfo -> locationDetailsAreEqual(locationInfo, new LocationInfo(null, platformId, latitude, longitude, altitude))).collect(Collectors.toList());
//        switch (equalLocations.size()) {
//            case 1: return Optional.of(equalLocations.get(0));
//            default: if(equalLocations.size() > 1 ) {
//                log.debug("Size of location entries fulfilling the equality is more than 1: " + equalLocations.size() + ". Returning first one found.");
//                return Optional.of(equalLocations.get(0));
//            } else {
//                return Optional.empty();
//            }
//        }
//    }
//
//    public boolean locationDetailsAreEqual( LocationInfo location1, LocationInfo location2 ) {
//        if( location1 == null || location2 == null ) return false;
//        if (location1 == location2) return true;
//
//        if (Double.compare(location1.getLatitude(), location2.getLatitude()) != 0) return false;
//        if (Double.compare(location1.getLongitude(), location2.getLongitude()) != 0) return false;
//        if (Double.compare(location1.getAltitude(), location2.getAltitude()) != 0) return false;
//        return location1.getPlatformId() != null ? location1.getPlatformId().equals(location2.getPlatformId()) : location2.getPlatformId() == null;
//    }
//
//
//
////    public String queryForLocationUri(Location location, String platformId ) {
////        String uri = null;
////        String query = null;
////
////        if( location instanceof WGS84Location) {
////            query = getWGS84SparqlQuery((WGS84Location) location, platformId);
////        }
////
////        //TODO other queries for WKTLocation and symbolic location
////
////        if( query != null ) {
////            String result = executeLocationQuery(query);
////
////            BufferedReader reader = new BufferedReader(new StringReader(result));
////            try {
////                String l1 = reader.readLine();
////                String l2 = reader.readLine();
////                String l3 = reader.readLine();
////                if (l1.equals("location")) {
////                    if (l3 != null) {
//////                        log.error("Found more than one locations: <" + l2 + "> and <" + l3 + ">");
////                    } else if( l2 == null) {
//////                        log.debug("Could not find any locations for the parameters");
////                    } else {
//////                        log.debug("Found location fulfilling the criteria! LocationURI: " + l2);
////                        uri = l2;
////                    }
////                } else {
//////                    log.error("Location query returned different value (instead of location): " + l1);
////                }
////                reader.close();
////            } catch (Exception e) {
//////                log.error("Exception occurred when executing location query: " + e.getMessage(), e);
////            }
////        }
////
////        return uri;
////    }
////
////    public String executeLocationQuery( String query ) {
////        try {
////            log.info("Creating location sparql query");
////            ObjectMapper mapper = new ObjectMapper();
////            CoreSparqlQueryRequest request = new CoreSparqlQueryRequest();
////            request.setBody(query);
////            //TODO set proper token
////            request.setSecurityRequest(null);
////            request.setOutputFormat(SparqlQueryOutputFormat.CSV);
////
////            String message = mapper.writeValueAsString(request);
////            String response = rabbitManager.sendRpcMessage(this.channel, this.durableResponseQueueName,this.resourceExchange, this.sparqlBindingKey, message, request.getClass().getCanonicalName());
////            if (response == null)
////                return null;
////            return mapper.readValue(response, String.class);
////        } catch (IOException e) {
////            log.error("Error when querying locations using sparql: " + e.getMessage(), e );
////        }
////        return null;
////    }
////
////
////    private String getWGS84SparqlQuery( WGS84Location location, String platformId ) {
////
////        StringBuilder query = new StringBuilder();
////        query.append("PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#> \n");
////        query.append("PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta#> \n");
////        query.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n");
////        query.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
////        query.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> \n");
////        query.append("PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> \n");
////
////        //Location test //dziala ok
////        query.append("SELECT ?location WHERE {\n" );
////        query.append("\t?location a cim:Location ;\n");
////        query.append("\t\ta cim:WGS84Location .\n");
////        //TODO locations are now array, need to ensure
//////        for( String label: location.getLabels() ) {
////            query.append("\t?location cim:name \"" + location.getName() + "\" .\n");
//////        }
////        for( String comment: location.getDescription()) {
////            query.append("\t?location cim:description \"" + comment + "\" .\n");
////        }
////
////        //Ensure that location is defined for this platform...
//////        query.append("\t?platform a owl:Ontology .\n");
////        query.append("\t?platform cim:id \"" + platformId + "\" ;\n");
////        query.append("\t\tmim:hasService ?service .\n");
////        query.append("\t?service mim:hasResource ?sensor .\n");
////        query.append("\t?sensor a cim:Resource ;\n");
////        query.append("\t\tcim:locatedAt ?location .\n");
////
////        //WGS84-specific
////        query.append("\t?location geo:lat \""+ location.getLatitude() + "\" ;\n");
////        query.append("\t\tgeo:long \""+ location.getLongitude() + "\" ;\n");
////        query.append("\t\tgeo:alt \""+ location.getAltitude() + "\" .\n");
////        query.append("}");
////
////        return query.toString();
////    }
////
//
//}
