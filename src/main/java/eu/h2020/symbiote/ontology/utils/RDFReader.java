package eu.h2020.symbiote.ontology.utils;

import eu.h2020.symbIoTe.ontology.CoreInformationModel;
import eu.h2020.symbIoTe.ontology.MetaInformationModel;
import eu.h2020.symbiote.core.internal.PIMInstanceDescription;
import eu.h2020.symbiote.core.model.InterworkingService;
import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.core.model.RDFInfo;
import eu.h2020.symbiote.core.model.internal.CoreResource;
import eu.h2020.symbiote.ontology.errors.RDFParsingError;
import eu.h2020.symbiote.ontology.validation.ValidationHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.JenaException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import org.apache.jena.ontology.OntModel;

/**
 * Helper class used to read generic information for symbIoTe objects: resource
 * and platform from the RDF.
 *
 * Created by Szymon Mueller on 12/04/2017.
 */
public class RDFReader {

    private static final Log log = LogFactory.getLog(RDFReader.class);

    /**
     * Reads the information about the platform from the specified RDF.
     *
     * @param rdfInfo RDF containing platform information.
     * @return Platform meta-information read from RDF.
     */
    public static PIMInstanceDescription readPlatformInstance(RDFInfo rdfInfo) throws RDFParsingError, JenaException {
        PIMInstanceDescription pimInstance = null;
        Model model = ModelFactory.createDefaultModel();
        try (StringReader reader = new StringReader(rdfInfo.getRdf())) {
            model.read(reader, null, rdfInfo.getRdfFormat().toString());
        }

        StmtIterator platformsIterator = model.listStatements(null, RDF.type, MetaInformationModel.Platform);
        if (platformsIterator.hasNext()) {
            Resource platformRes = platformsIterator.next().getSubject();
            if (platformsIterator.hasNext()) {
                throw new RDFParsingError("Found too many platforms in the RDF. It is possible to only register one platform during single registration.");
            }
            StmtIterator idIterator = model.listStatements(platformRes, CoreInformationModel.id, (RDFNode) null);
            String platformId = ensureSingleStatement(idIterator).getObject().asLiteral().toString();
            StmtIterator labelIterator = model.listStatements(platformRes, RDFS.label, (RDFNode) null);
            List<String> labelsList = new ArrayList<>();
            while (labelIterator.hasNext()) {
                Statement labelStmt = labelIterator.next();
                labelsList.add(labelStmt.getObject().asLiteral().toString());
            }

            StmtIterator commentsIterator = model.listStatements(platformRes, RDFS.comment, (RDFNode) null);
            List<String> commentList = new ArrayList<>();
            while (commentsIterator.hasNext()) {
                String comment = commentsIterator.next().getObject().asLiteral().toString();
                log.debug("Found comment of the resource: " + comment);
                commentList.add(comment);
            }

            StmtIterator interworkingInterfaceIterator = model.listStatements(null, RDF.type, MetaInformationModel.InterworkingService);
            List<InterworkingService> services = new ArrayList<>();
            while (interworkingInterfaceIterator.hasNext()) {
                Resource serviceRes = interworkingInterfaceIterator.next().getSubject();
                StmtIterator urlIterator = model.listStatements(serviceRes, MetaInformationModel.url, (RDFNode) null);
                String url = ensureSingleStatement(urlIterator).getObject().asLiteral().toString();

                StmtIterator informationModelIterator = model.listStatements(serviceRes, MetaInformationModel.usesInformationModel, (RDFNode) null);
                RDFNode informationModelRDFNode = ensureSingleStatement(informationModelIterator).getObject();
                if (informationModelRDFNode instanceof Resource) {
                    Resource informationModelResource = informationModelRDFNode.asResource();
                    AnonId id = informationModelResource.getId();
                    RDFNode infModelIdRDFNode = ensureSingleStatement(model.listStatements(model.createResource(id), CoreInformationModel.id, (RDFNode) null)).getObject();
                    if (infModelIdRDFNode instanceof Literal) {
                        String infModelId = infModelIdRDFNode.asLiteral().toString();
                        InterworkingService service = new InterworkingService();
                        service.setInformationModelId(infModelId);
                        service.setUrl(url);
                        services.add(service);
                    } else {
                        throw new RDFParsingError("Could not find information model id in the RDF");
                    }
                } else if (informationModelRDFNode instanceof Literal) {
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
     *
     * @param rdfInfo RDF containing resources information.
     * @return Information about resources read from RDF.
     */
    public static Map<String, CoreResource> readResourceInstances(RDFInfo rdfInfo) throws RDFParsingError, JenaException {
        Map<String, CoreResource> resources = new HashMap<>();

        StringBuilder errorMessages = new StringBuilder();

        //TODO check if rdf is not null/empty
        if (rdfInfo == null || rdfInfo.getRdf() == null || rdfInfo.getRdfFormat() == null) {
            StringBuilder sb = new StringBuilder();
            if (rdfInfo == null) {
                sb.append("| rdfInfo is null |");
            } else {
                if (rdfInfo.getRdf() == null) {
                    sb.append("| rdf in rdfInfo is null |");
                }
                if (rdfInfo.getRdfFormat() == null) {
                    sb.append("| format of the rdf is null |");
                }
            }
            throw new RDFParsingError("Wrong rdf description was used: " + sb.toString());
        }
        OntModel inputModel;
        try {
            inputModel = OntologyHelper.read(rdfInfo, false, true);
        } catch (IOException ex) {
            throw new RDFParsingError("Could not parse RDF, reason: " + ex);
        }
        Map<Resource, Model> instances = ValidationHelper.sepearteResources(inputModel);
        for (Map.Entry<Resource, Model> entry : instances.entrySet()) {
            resources.put(
                    entry.getKey().getURI(),
                    createCoreResource(entry.getKey(), entry.getValue(), rdfInfo.getRdfFormat()));
        }
        if (resources.isEmpty()) {
            String finalMessage = errorMessages.toString();
            throw new RDFParsingError("RDF Parser read 0 resources from the specified RDF. " + (finalMessage.isEmpty() ? "" : "Additional info: " + finalMessage));
        }

        return resources;
    }

    public static CoreResource createCoreResource(Resource rdfResource, Model model, RDFFormat rdfFormat) throws RDFParsingError {
        CoreResource resource = new CoreResource();
        // TODO implement correctly - needs list of CoreResourceType
        resource.setType(SymbioteModelsUtil.getTypeForResource(rdfResource));
        // id        
        RDFNode idNode = rdfResource.getPropertyResourceValue(CoreInformationModel.id);
        String id;
        if (idNode == null || !idNode.isLiteral()) {
            id = String.valueOf(ObjectId.get());
            log.debug("Created id of the resource: " + id);
        } else {
            id = idNode.asLiteral().getString();
            log.debug("Found id of the resource: " + id);
        }
        resource.setId(id);
        // labels                
        resource.setLabels(rdfResource.listProperties(RDFS.label).mapWith(x -> x.getObject().asLiteral().getString()).toList());
        if (resource.getLabels().isEmpty()) {
            throw new RDFParsingError("Must define at least one label for resource " + rdfResource.getURI());
        }
        // comments
        resource.setComments(rdfResource.listProperties(RDFS.comment).mapWith(x -> x.getObject().asLiteral().getString()).toList());
        resource.setRdfFormat(rdfFormat);
        resource.setRdf(OntologyHelper.modelAsString(model, rdfFormat));
        return resource;
    }

    public static CoreResource createCoreResource(Resource ontResource, RDFFormat rdfFormat) throws RDFParsingError {
        return createCoreResource(ontResource, ontResource.getModel(), rdfFormat);
    }

    /**
     * Traverses the iterator and ensures it contains exactly one statement,
     * which is returned. In other cases error is thrown.
     *
     * @return The only statement of the iterator.
     * @throws RDFParsingError thrown in case statement contains 0 or more than
     * 1 statements.
     */
    private static Statement ensureSingleStatement(StmtIterator iterator) throws RDFParsingError {
        if (iterator.hasNext()) {
            Statement next = iterator.next();
            if (iterator.hasNext()) {
                throw new RDFParsingError("Iterator has more than one statement");
            }
            return next;
        } else {
            throw new RDFParsingError("Iterator contains no statements");
        }
    }

}
