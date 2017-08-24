package eu.h2020.symbiote.ontology.utils;

import eu.h2020.symbIoTe.ontology.CoreInformationModel;
import eu.h2020.symbIoTe.ontology.MetaInformationModel;
import eu.h2020.symbiote.core.internal.PIMInstanceDescription;
import eu.h2020.symbiote.core.model.InterworkingService;
import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.core.model.RDFInfo;
import eu.h2020.symbiote.core.model.internal.CoreResource;
import eu.h2020.symbiote.ontology.errors.RDFParsingError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.JenaException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

/**
 * Helper class used to read generic information for symbIoTe objects: resource and platform from the RDF.
 *
 * Created by Szymon Mueller on 12/04/2017.
 */
public class RDFReader {

    private static final Log log = LogFactory.getLog(RDFReader.class);

    /**
     * Reads the information about the platform from the specified RDF.
     * @param rdfInfo RDF containing platform information.
     * @return Platform meta-information read from RDF.
     */
    public static PIMInstanceDescription readPlatformInstance(RDFInfo rdfInfo ) throws RDFParsingError, JenaException {
        PIMInstanceDescription pimInstance = null;
        Model model = ModelFactory.createDefaultModel();
        try( StringReader reader = new StringReader( rdfInfo.getRdf() ) ) {
            model.read(reader, null, rdfInfo.getRdfFormat().toString());
        }

        StmtIterator platformsIterator = model.listStatements(null,RDF.type, MetaInformationModel.Platform);
        if( platformsIterator.hasNext() ) {
            Resource platformRes = platformsIterator.next().getSubject();
            if( platformsIterator.hasNext() ) {
                throw new RDFParsingError("Found too many platforms in the RDF. It is possible to only register one platform during single registration." );
            }
            StmtIterator idIterator = model.listStatements(platformRes, CoreInformationModel.id, (RDFNode)null);
            String platformId = ensureSingleStatement(idIterator).getObject().asLiteral().toString();
            StmtIterator labelIterator = model.listStatements(platformRes, RDFS.label, (RDFNode) null);
            List<String> labelsList = new ArrayList<>();
            while( labelIterator.hasNext() ) {
                Statement labelStmt = labelIterator.next();
                labelsList.add(labelStmt.getObject().asLiteral().toString());
            }

            StmtIterator commentsIterator = model.listStatements(platformRes, RDFS.comment, (RDFNode) null);
            List<String> commentList = new ArrayList<>();
            while( commentsIterator.hasNext() ) {
                String comment = commentsIterator.next().getObject().asLiteral().toString();
                log.debug("Found comment of the resource: " + comment);
                commentList.add(comment);
            }

            StmtIterator interworkingInterfaceIterator = model.listStatements(null,RDF.type,MetaInformationModel.InterworkingService);
            List<InterworkingService> services = new ArrayList<>();
            while(interworkingInterfaceIterator.hasNext()) {
                Resource serviceRes = interworkingInterfaceIterator.next().getSubject();
                StmtIterator urlIterator = model.listStatements(serviceRes, MetaInformationModel.url, (RDFNode) null);
                String url = ensureSingleStatement(urlIterator).getObject().asLiteral().toString();

                StmtIterator informationModelIterator = model.listStatements(serviceRes, MetaInformationModel.usesInformationModel, (RDFNode) null);
                RDFNode informationModelRDFNode = ensureSingleStatement(informationModelIterator).getObject();
                if( informationModelRDFNode instanceof Resource ) {
                    Resource informationModelResource = informationModelRDFNode.asResource();
                    AnonId id = informationModelResource.getId();
                    RDFNode infModelIdRDFNode = ensureSingleStatement(model.listStatements(model.createResource(id), CoreInformationModel.id, (RDFNode) null)).getObject();
                    if( infModelIdRDFNode instanceof Literal ) {
                        String infModelId = infModelIdRDFNode.asLiteral().toString();
                        InterworkingService service = new InterworkingService();
                        service.setInformationModelId( infModelId);
                        service.setUrl( url );
                        services.add(service);
                    } else {
                        throw new RDFParsingError( "Could not find information model id in the RDF");
                    }
                } else if ( informationModelRDFNode instanceof  Literal ) {
                    Literal literal = informationModelRDFNode.asLiteral();
                    //TODO decide if information model can be passed as Literal
                    String message = "Information model passed as a literal is not handled yet. Literal: " + literal.toString();
                    log.info(message);
                    throw new RDFParsingError("Could not read information model property. Internal error: " + message);
                }
            }

            pimInstance = new PIMInstanceDescription();
            pimInstance.setId(platformId);
            pimInstance.setLabels(labelsList);
            pimInstance.setComments(commentList);
            pimInstance.setInterworkingServices(services);
            pimInstance.setRdf(rdfInfo.getRdf());
            pimInstance.setRdfFormat(rdfInfo.getRdfFormat());
        }

        return pimInstance;
    }

    /**
     * Reads the information about the resources from the specified RDF.
     * @param rdfInfo RDF containing resources information.
     * @return Information about resources read from RDF.
     */
    public static Map<String,CoreResource> readResourceInstances( RDFInfo rdfInfo, String platformId ) throws RDFParsingError, JenaException {
        Map<String,CoreResource> resources = new HashMap<>();

        StringBuilder errorMessages = new StringBuilder();

        //TODO check if rdf is not null/empty
        if( rdfInfo == null || rdfInfo.getRdf() == null || rdfInfo.getRdfFormat() == null) {
            StringBuilder sb = new StringBuilder();
            if (rdfInfo == null) sb.append("| rdfInfo is null |");
            if (rdfInfo.getRdf() == null) sb.append("| rdf in rdfInfo is null |");
            if (rdfInfo.getRdfFormat() == null ) sb.append( "| format of the rdf is null |");
            throw new RDFParsingError( "Wrong rdf description was used: " + sb.toString() );
        }
        Model model = ModelFactory.createDefaultModel();
        try( StringReader reader = new StringReader( rdfInfo.getRdf() ) ) {
            model.read(reader, null, rdfInfo.getRdfFormat().toString());
        }


        StmtIterator resourceIterator = model.listStatements(null,RDF.type,CoreInformationModel.Resource);
        log.debug("Resources: ");
        List<Resource> rdfResources = new ArrayList<>();
        while( resourceIterator.hasNext()) {
            Statement next = resourceIterator.next();
            log.debug(next);
            rdfResources.add(next.getSubject());
//            log.debug( " Got URI: <" + next.getSubject().getURI() + ">");
        }

        StmtIterator interworkingInterfaceIterator = model.listStatements(null,RDF.type,MetaInformationModel.InterworkingService);

        log.debug("Interworking services: ");
        Map<String,String> interworkingServices = new HashMap<>();
        while( interworkingInterfaceIterator.hasNext() ) {
            Statement next = interworkingInterfaceIterator.next();
            log.debug(next);
            Resource subject = next.getSubject();
            StmtIterator urls = model.listStatements(subject, MetaInformationModel.url, (RDFNode) null);
            if( urls.hasNext() ) {
                String url = urls.next().getObject().toString();
                log.debug(" Found <" + subject.getURI()+"> with uri " + url );
                interworkingServices.put(subject.getURI(),url);
            }
        }

        log.debug("Searching and parsing features");

        for( Resource res: rdfResources ) {
            log.debug("Searching mapping for " + res.getURI());
            StmtIterator searchForResourcesInServices = model.listStatements(null, MetaInformationModel.hasResource, res );
            if( !searchForResourcesInServices.hasNext() ) {
                //Just for logging
                String msg = "Could not find interworking service that has resource: " + res.getURI();
                log.debug( msg );
                errorMessages.append( msg );
            }
            while( searchForResourcesInServices.hasNext() ) {

                Statement next = searchForResourcesInServices.next();
                log.debug(next);
                Resource foundService = next.getSubject();
                //Check if it is really a service
                if( interworkingServices.containsKey(foundService.getURI()) ) {
                    log.debug("Found interworking service, creating a resource... ");
                    CoreResource coreResource = createCoreResource(rdfInfo, model, res, interworkingServices.get(foundService.getURI()));
                    resources.put(res.getURI(),coreResource);
                } else {
                    log.warn("Subject is not an interworking service: " + foundService.getURI());
                }
            }
        }
        if( resources.size() == 0 ) {
            String finalMessage = errorMessages.toString();
            throw new RDFParsingError("RDF Parser read 0 resources from the specified RDF. " + (finalMessage.isEmpty()?"":"Additional info: " + finalMessage
             ));
        }

        return resources;
    }

    private static CoreResource createCoreResource( RDFInfo rdfInfo, Model model, Resource resourceRes, String serviceURL) throws RDFParsingError {
        CoreResource resource = new CoreResource();

        resource.setType(SymbioteModelsUtil.getTypeForResource(resource));

        StmtIterator idIterator = model.listStatements(resourceRes, CoreInformationModel.id, (RDFNode) null);
        String resourceId;
        if( idIterator.hasNext() ) {
            Statement next = idIterator.next();
            resourceId = next.getObject().asLiteral().toString();
            log.debug("Found id of the resource: " + resourceId);
        } else {
            resourceId = String.valueOf(ObjectId.get());
            log.debug("Created id of the resource: " + resourceId);
            model.add(resourceRes, CoreInformationModel.id, resourceId);
        }
        resource.setId(resourceId);

        //Labels
        StmtIterator labelIterator = model.listStatements(resourceRes, RDFS.label, (RDFNode) null);
        List<String> labelList = new ArrayList<>();
        while( labelIterator.hasNext() ) {
            String label = labelIterator.next().getObject().asLiteral().toString();
            log.debug("Found label of the resource: " + label);
            labelList.add(label);
        }
        if( labelList.size() == 0 ) {
            //Throws error because no existing labels
            throw new RDFParsingError("Must define at least one label for resource " + resourceRes.getURI());
        }
        resource.setLabels(labelList);

        //Comments
        StmtIterator commentsIterator = model.listStatements(resourceRes, RDFS.comment, (RDFNode) null);
        List<String> commentList = new ArrayList<>();
        while( commentsIterator.hasNext() ) {
            String comment = commentsIterator.next().getObject().asLiteral().toString();
            log.debug("Found comment of the resource: " + comment);
            commentList.add(comment);
        }
        resource.setComments(commentList);

        if( serviceURL == null || serviceURL.isEmpty() ) {
            throw new RDFParsingError("Could not create Core Resource for null or empty service URL");
        }
        resource.setInterworkingServiceURL(serviceURL);
        try (StringWriter writer = new StringWriter() ) {
            model.write(writer, rdfInfo.getRdfFormat().toString());
            resource.setRdf(writer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        resource.setRdfFormat(rdfInfo.getRdfFormat());
        return resource;
    }

    /**
     * Traverses the iterator and ensures it contains exactly one statement, which is returned. In other cases error is thrown.
     *
     * @return The only statement of the iterator.
     * @throws RDFParsingError thrown in case statement contains 0 or more than 1 statements.
     */
    private static Statement ensureSingleStatement( StmtIterator iterator ) throws RDFParsingError {
        if( iterator.hasNext() ) {
            Statement next = iterator.next();
            if( iterator.hasNext() ) {
                throw new RDFParsingError("Iterator has more than one statement");
            }
            return next;
        } else {
            throw new RDFParsingError("Iterator contains no statements");
        }
    }

}
