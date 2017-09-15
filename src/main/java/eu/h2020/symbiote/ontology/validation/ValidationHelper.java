/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.ontology.validation;

import eu.h2020.symbIoTe.ontology.CoreInformationModel;
import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.ontology.utils.OntologyHelper;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 *
 * @author jab
 */
public class ValidationHelper {

    public static final String CIM_1_0_2_FILE = "core-v1.0.2.owl";

    private static final Log log = LogFactory.getLog(ValidationHelper.class);
    private static ValidationHelper instance;

    private static final String TAG_RESOURCE_URI = "?RESOURCE_URI";
    private static final String QUERY_PREFIXES
            = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
            + "PREFIX core: <http://www.symbiote-h2020.eu/ontology/core#> \n"
            + "\n";

    private static final String QUERY_CARDINALITY_EXACTLY_OBJECT_PROPERTY
            = QUERY_PREFIXES
            + "SELECT  ?property ?cardinality ?type (COUNT(DISTINCT ?value) AS ?presentCardinality)\n"
            + "WHERE\n"
            + "{ \n"
            + "	" + TAG_RESOURCE_URI + " a ?class .\n"
            + "	?class rdfs:subClassOf* core:Resource .\n"
            + "	?class  rdfs:subClassOf* [\n"
            + "		rdf:type owl:Restriction ;\n"
            + "		owl:onProperty ?property ;\n"
            + "		owl:qualifiedCardinality ?cardinality ;\n"
            + "		owl:onClass ?type \n"
            + "	]\n"
            + "	OPTIONAL\n"
            + "	{ \n"
            + "		" + TAG_RESOURCE_URI + " a  ?class ;\n"
            + "		?property  ?value\n"
            + "		BIND (datatype(?value) as ?x)\n"
            + "		FILTER EXISTS {?x rdfs:subClassOf* ?type }\n"
            + "	}\n"
            + "}\n"
            + "GROUP BY ?property ?cardinality ?type\n"
            + "HAVING(?cardinality != ?presentCardinality)\n"
            + "";

    private static final String QUERY_CARDINALITY_EXACTLY_DATA_PROPERTY
            = QUERY_PREFIXES
            + "SELECT  ?property ?cardinality ?type (COUNT(DISTINCT ?value) AS ?presentCardinality)\n"
            + "WHERE\n"
            + "  { \n"
            + "	" + TAG_RESOURCE_URI + " a ?class .\n"
            + "	?class rdfs:subClassOf* core:Resource .\n"
            + "	?class  rdfs:subClassOf* [\n"
            + "		   a owl:Restriction ;\n"
            + "            owl:onProperty ?property ;\n"
            + "            owl:qualifiedCardinality ?cardinality ;\n"
            + "            owl:onDataRange ?type \n"
            + "		]\n"
            + "    OPTIONAL\n"
            + "	{ \n"
            + "		" + TAG_RESOURCE_URI + "  a  ?class ;\n"
            + "			  ?property  ?value\n"
            + "		BIND (datatype(?value) as ?x)\n"
            + "		FILTER EXISTS {?x rdfs:subClassOf* ?type }\n"
            + "	}\n"
            + "  }\n"
            + "GROUP BY ?property ?cardinality ?type\n"
            + "HAVING(?cardinality != ?presentCardinality)";

    private static final String QUERY_CARDINALITY_MIN_OBJECT_PROPERTY
            = QUERY_PREFIXES
            + "SELECT  ?property ?cardinality ?type (COUNT(DISTINCT ?value) AS ?presentCardinality)\n"
            + "WHERE\n"
            + "{ \n"
            + "	" + TAG_RESOURCE_URI + " a ?class .\n"
            + "	?class rdfs:subClassOf* core:Resource .\n"
            + "	?class  rdfs:subClassOf* [\n"
            + "		rdf:type owl:Restriction ;\n"
            + "		owl:onProperty ?property ;\n"
            + "		owl:minQualifiedCardinality ?cardinality ;\n"
            + "		owl:onClass ?type \n"
            + "	]\n"
            + "	OPTIONAL\n"
            + "	{ \n"
            + "		" + TAG_RESOURCE_URI + " a  ?class ;\n"
            + "		?property  ?value\n"
            + "		BIND (datatype(?value) as ?x)\n"
            + "		FILTER EXISTS {?x rdfs:subClassOf* ?type }\n"
            + "	}\n"
            + "}\n"
            + "GROUP BY ?property ?cardinality ?type\n"
            + "HAVING(?cardinality > ?presentCardinality)";

    private static final String QUERY_CARDINALITY_MIN_DATA_PROPERTY
            = QUERY_PREFIXES
            + "SELECT  ?property ?cardinality ?type (COUNT(DISTINCT ?value) AS ?presentCardinality)\n"
            + "WHERE\n"
            + "{ \n"
            + "	" + TAG_RESOURCE_URI + " a ?class .\n"
            + "	?class rdfs:subClassOf* core:Resource .\n"
            + "	?class  rdfs:subClassOf* [\n"
            + "		rdf:type owl:Restriction ;\n"
            + "		owl:onProperty ?property ;\n"
            + "		owl:minQualifiedCardinality ?cardinality ;\n"
            + "		owl:onDataRange ?type \n"
            + "	]\n"
            + "	OPTIONAL\n"
            + "	{ \n"
            + "		" + TAG_RESOURCE_URI + " a  ?class ;\n"
            + "		?property  ?value\n"
            + "		BIND (datatype(?value) as ?x)\n"
            + "		FILTER EXISTS {?x rdfs:subClassOf* ?type }\n"
            + "	}\n"
            + "}\n"
            + "GROUP BY ?property ?cardinality ?type\n"
            + "HAVING(?cardinality > ?presentCardinality)";

    private static final String QUERY_CARDINALITY_MAX_OBJECT_PROPERTY
            = QUERY_PREFIXES
            + "SELECT  ?property ?cardinality ?type (COUNT(DISTINCT ?value) AS ?presentCardinality)\n"
            + "WHERE\n"
            + "{ \n"
            + "	" + TAG_RESOURCE_URI + " a ?class .\n"
            + "	?class rdfs:subClassOf* core:Resource .\n"
            + "	?class  rdfs:subClassOf* [\n"
            + "		rdf:type owl:Restriction ;\n"
            + "		owl:onProperty ?property ;\n"
            + "		owl:maxQualifiedCardinality ?cardinality ;\n"
            + "		owl:onClass ?type \n"
            + "	]\n"
            + "	OPTIONAL\n"
            + "	{ \n"
            + "		" + TAG_RESOURCE_URI + " a  ?class ;\n"
            + "		?property  ?value\n"
            + "		BIND (datatype(?value) as ?x)\n"
            + "		FILTER EXISTS {?x rdfs:subClassOf* ?type }\n"
            + "	}\n"
            + "}\n"
            + "GROUP BY ?property ?cardinality ?type\n"
            + "HAVING(?cardinality < ?presentCardinality)";

    private static final String QUERY_CARDINALITY_MAX_DATA_PROPERTY
            = QUERY_PREFIXES
            + "SELECT  ?property ?cardinality ?type (COUNT(DISTINCT ?value) AS ?presentCardinality)\n"
            + "WHERE\n"
            + "{ \n"
            + "	" + TAG_RESOURCE_URI + " a ?class .\n"
            + "	?class rdfs:subClassOf* core:Resource .\n"
            + "	?class  rdfs:subClassOf* [\n"
            + "		rdf:type owl:Restriction ;\n"
            + "		owl:onProperty ?property ;\n"
            + "		owl:maxQualifiedCardinality ?cardinality ;\n"
            + "		owl:onDataRange ?type \n"
            + "	]\n"
            + "	OPTIONAL\n"
            + "	{ \n"
            + "		" + TAG_RESOURCE_URI + " a  ?class ;\n"
            + "		?property  ?value\n"
            + "		BIND (datatype(?value) as ?x)\n"
            + "		FILTER EXISTS {?x rdfs:subClassOf* ?type }\n"
            + "	}\n"
            + "}\n"
            + "GROUP BY ?property ?cardinality ?type\n"
            + "HAVING(?cardinality < ?presentCardinality)";

    private static final ParameterizedSparqlString GET_RESOURCE_CLOSURE = new ParameterizedSparqlString(
            "CONSTRUCT {\n"
            + "	?s ?p ?o.\n"
            + "}\n"
            + "{\n"
            + "	SELECT DISTINCT ?s ?p ?o\n"
            + "	{\n"
            + "		{\n"
            + "			SELECT *\n"
            + "			{\n"
            + "				" + TAG_RESOURCE_URI + " ?p ?o.\n"
            + "				BIND( " + TAG_RESOURCE_URI + " as ?s)\n"
            + "			}\n"
            + "		}\n"
            + "		UNION\n"
            + "		{\n"
            + "			SELECT *\n"
            + "			WHERE {\n"
            + "				" + TAG_RESOURCE_URI + " (a|!a)+ ?s . \n"
            + "				?s ?p ?o.\n"
            + "			}\n"
            + "		}\n"
            + "	}\n"
            + "}");

    private ValidationHelper() {
    }

    public static boolean checkImportsCIM(OntModel model) {
        // check also in the import closure for CIM
        // return model.listImportedOntologyURIs(true).contains(CoreInformationModel.NS);
        return model.listImportedOntologyURIs().contains(CoreInformationModel.NS);
    }

    public static Set<String> getDefinedClasses(OntModel model) {
        return model.listResourcesWithProperty(RDF.type, RDFS.Class)
                .filterDrop(x -> x.isAnon())
                .mapWith(x -> x.getURI())
                .toSet();
    }

    public static Set<String> getUsedClasses(OntModel model) {
        Set<String> usedClasses = new HashSet<>();
        StmtIterator iterator = model.listStatements();
        while (iterator.hasNext()) {
            Statement stmt = iterator.nextStatement();
            if (stmt.getPredicate().equals(RDFS.subClassOf)) {
                if (stmt.getSubject().isURIResource()) {
                    usedClasses.add(stmt.getSubject().getURI());
                }
                if (stmt.getObject().isURIResource()) {
                    usedClasses.add(stmt.getObject().asResource().getURI());
                }
            }
            if (stmt.getObject().isURIResource()
                    && (stmt.getPredicate().equals(RDF.type)
                    || stmt.getPredicate().equals(RDFS.domain)
                    || stmt.getPredicate().equals(RDFS.range))) {
                usedClasses.add(stmt.getObject().asResource().getURI());
            }
        }
        return usedClasses;
    }

    public static Set<String> getUndefinedButUsedClasses(OntModel model) {
        Set<String> definedClasses = getDefinedClasses(model);
        Set<String> usedClasses = getUsedClasses(model);
        usedClasses.removeAll(definedClasses);
        return usedClasses;
    }

    private static void writeModelToFile(Model model, String filename, RDFFormat format) {
        FileWriter out = null;
        try {
            out = new FileWriter(filename);
            model.write(out, format.name());
        } catch (IOException e) {

        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException closeException) {
                // ignore
            }
        }
    }

    public static Map<Resource, Model> sepearteResources(OntModel instances, Model pim) {
        Map<Resource, Model> result = new HashMap<>();
        instances.addSubModel(pim);
        Set<Individual> resourcesDefinedInPIM = OntologyHelper.withInf(pim).listIndividuals(CoreInformationModel.Resource).toSet();
        Set<Individual> resourceIndividuals = instances.listIndividuals(CoreInformationModel.Resource).toSet();
        resourceIndividuals.removeAll(resourcesDefinedInPIM);
        instances.removeSubModel(pim);
        for (Individual resource : resourceIndividuals) {
            GET_RESOURCE_CLOSURE.setIri(TAG_RESOURCE_URI, resource.getURI());
            try (QueryExecution qexec = QueryExecutionFactory.create(GET_RESOURCE_CLOSURE.asQuery(), instances.getRawModel())) {
                Model resourceClosure = qexec.execConstruct();
                result.put(resourceClosure.getResource(resource.getURI()), resourceClosure);
            }
        }
        return result;
    }

    public static Set<String> getDefinedResourcesInNamespace(OntModel model, String namespace) {
        // check everyhting that has rdf:type
        Set<String> result = model
                .listSubjectsWithProperty(RDF.type)
                .filterKeep(x -> x.getNameSpace() != null && x.getNameSpace().equals(namespace))
                .mapWith(x -> x.toString())
                .toSet();
        // check all individuals
        result.addAll(model
                .listIndividuals()
                .filterKeep(x -> x.getNameSpace() != null && x.getNameSpace().equals(namespace))
                .mapWith(x -> x.toString())
                .toSet());
        return result;
    }
    
    private static String setResourceUri(String source, Resource resource) {
        return source.replace(TAG_RESOURCE_URI, "<" + resource.getURI() + ">");
    }

    public static List<String> checkCardinalityViolations(Resource instance, OntModel pim, Model instanceData) {
        List<String> result = new ArrayList<>();
        pim.addSubModel(instanceData);
        result.addAll(OntologyHelper.executeSelectWithResults(pim, 
                setResourceUri(QUERY_CARDINALITY_EXACTLY_DATA_PROPERTY, instance),
                "exact cardinaility for data property violated - "));
        result.addAll(OntologyHelper.executeSelectWithResults(pim, 
                setResourceUri(QUERY_CARDINALITY_EXACTLY_OBJECT_PROPERTY, instance),
                "exact cardinaility for object property violated - "));
        result.addAll(OntologyHelper.executeSelectWithResults(pim, 
                setResourceUri(QUERY_CARDINALITY_MIN_DATA_PROPERTY, instance),
                "min cardinaility for data property violated - "));
        result.addAll(OntologyHelper.executeSelectWithResults(pim, 
                setResourceUri(QUERY_CARDINALITY_MIN_OBJECT_PROPERTY, instance),
                "min cardinaility for object property violated - "));
        result.addAll(OntologyHelper.executeSelectWithResults(pim, 
                setResourceUri(QUERY_CARDINALITY_MAX_DATA_PROPERTY, instance),
                "max cardinaility for data property violated - "));
        result.addAll(OntologyHelper.executeSelectWithResults(pim, 
                setResourceUri(QUERY_CARDINALITY_MAX_OBJECT_PROPERTY, instance),
                "max cardinaility for object property violated - "));
        pim.removeSubModel(instanceData);
        return result;
    }
}
