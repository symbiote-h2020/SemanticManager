/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.ontology.validation;

import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.core.model.RDFInfo;
import eu.h2020.symbiote.ontology.utils.CoreInformationModel;
import eu.h2020.symbiote.ontology.utils.StreamHelper;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 *
 * @author jab
 */
public class ValidationHelper {

    private static final Log log = LogFactory.getLog(ValidationHelper.class);
    private static ValidationHelper instance;

    private static final String QUERY_CARDINALITY_EXACTLY_OBJECT_PROPERTY = "SELECT \n"
            + "?class ?property ?cardinality ?onType (COUNT(DISTINCT ?instance) AS ?presentCardinality)\n"
            + "WHERE \n"
            + "{ \n"
            + "	?class rdfs:subClassOf [ \n"
            + "		a owl:Restriction; \n"
            + "		owl:onProperty ?property;\n"
            + "		owl:qualifiedCardinality ?cardinality ;\n"
            + "		owl:onClass ?onType ] .	\n"
            + "	OPTIONAL {\n"
            + "		?instance a ?class.\n"
            + "		?instance ?property [ a ?onType] .\n"
            + "	}\n"
            + "}\n"
            + "GROUP BY ?class ?property ?cardinality ?onType\n"
            + "HAVING(?cardinality != ?presentCardinality)";

    private static final String QUERY_CARDINALITY_EXACTLY_DATA_PROPERTY = "SELECT \n"
            + "?class ?property ?cardinality ?onType (COUNT(DISTINCT ?instance) AS ?presentCardinality)\n"
            + "WHERE \n"
            + "{ \n"
            + "	?class rdfs:subClassOf [ \n"
            + "		a owl:Restriction; \n"
            + "		owl:onProperty ?property;\n"
            + "		owl:qualifiedCardinality ?cardinality ;\n"
            + "		owl:onDataRange ?onType ] .	\n"
            + "	OPTIONAL {\n"
            + "		?instance a ?class.\n"
            + "		?instance ?property ?p .\n"
            + "		FILTER(DATATYPE(?p) = ?onType)\n"
            + "	}\n"
            + "}\n"
            + "GROUP BY ?class ?property ?cardinality ?onType\n"
            + "HAVING(?cardinality != ?presentCardinality)";

    private static final String QUERY_CARDINALITY_MIN_OBJECT_PROPERTY = "SELECT \n"
            + "?class ?property ?cardinality ?onType (COUNT(DISTINCT ?instance) AS ?presentCardinality)\n"
            + "WHERE \n"
            + "{ \n"
            + "	?class rdfs:subClassOf [ \n"
            + "		a owl:Restriction; \n"
            + "		owl:onProperty ?property;\n"
            + "		owl:minQualifiedCardinality ?cardinality ;\n"
            + "		owl:onClass ?onType ] .	\n"
            + "	OPTIONAL {\n"
            + "		?instance a ?class.\n"
            + "		?instance ?property [ a ?onType] .\n"
            + "	}\n"
            + "}\n"
            + "GROUP BY ?class ?property ?cardinality ?onType\n"
            + "HAVING(?cardinality > ?presentCardinality)";

    private static final String QUERY_CARDINALITY_MIN_DATA_PROPERTY = "SELECT \n"
            + "?class ?property ?cardinality ?onType (COUNT(DISTINCT ?instance) AS ?presentCardinality)\n"
            + "WHERE \n"
            + "{ \n"
            + "	?class rdfs:subClassOf [ \n"
            + "		a owl:Restriction; \n"
            + "		owl:onProperty ?property;\n"
            + "		owl:minQualifiedCardinality ?cardinality ;\n"
            + "		owl:onDataRange ?onType ] .	\n"
            + "	OPTIONAL {\n"
            + "		?instance a ?class.\n"
            + "		?instance ?property ?p .\n"
            + "		FILTER(DATATYPE(?p) = ?onType)\n"
            + "	}\n"
            + "}\n"
            + "GROUP BY ?class ?property ?cardinality ?onType\n"
            + "HAVING(?cardinality > ?presentCardinality)";

    private static final String QUERY_CARDINALITY_MAX_OBJECT_PROPERTY = "SELECT \n"
            + "?class ?property ?cardinality ?onType (COUNT(DISTINCT ?instance) AS ?presentCardinality)\n"
            + "WHERE \n"
            + "{ \n"
            + "	?class rdfs:subClassOf [ \n"
            + "		a owl:Restriction; \n"
            + "		owl:onProperty ?property;\n"
            + "		owl:maxQualifiedCardinality ?cardinality ;\n"
            + "		owl:onClass ?onType ] .	\n"
            + "	OPTIONAL {\n"
            + "		?instance a ?class.\n"
            + "		?instance ?property [ a ?onType] .\n"
            + "	}\n"
            + "}\n"
            + "GROUP BY ?class ?property ?cardinality ?onType\n"
            + "HAVING(?cardinality < ?presentCardinality)";

    private static final String QUERY_CARDINALITY_MAX_DATA_PROPERTY = "SELECT \n"
            + "?class ?property ?cardinality ?onType (COUNT(DISTINCT ?instance) AS ?presentCardinality)\n"
            + "WHERE \n"
            + "{ \n"
            + "	?class rdfs:subClassOf [ \n"
            + "		a owl:Restriction; \n"
            + "		owl:onProperty ?property;\n"
            + "		owl:maxQualifiedCardinality ?cardinality ;\n"
            + "		owl:onDataRange ?onType ] .	\n"
            + "	OPTIONAL {\n"
            + "		?instance a ?class.\n"
            + "		?instance ?property ?p .\n"
            + "		FILTER(DATATYPE(?p) = ?onType)\n"
            + "	}\n"
            + "}\n"
            + "GROUP BY ?class ?property ?cardinality ?onType\n"
            + "HAVING(?cardinality < ?presentCardinality)";

    private OntDocumentManager docManager;
    private OntModelSpec modelSpecOWL;
    private OntModelSpec modelSpecOWL_INF;

    public static ValidationHelper getInstance() {
        if (instance == null) {
            instance = new ValidationHelper();
            instance.init();
        }
        return instance;
    }

    private ValidationHelper() {
    }

    private void init() {
        docManager = new OntDocumentManager();
        docManager.setProcessImports(false);
        docManager.addAltEntry(CoreInformationModel.CIM_PREFIX, CoreInformationModel.CIM_1_0_2_FILE);
        modelSpecOWL_INF = OntModelSpec.OWL_DL_MEM_RDFS_INF;
        modelSpecOWL = OntModelSpec.OWL_DL_MEM;
        modelSpecOWL_INF.setDocumentManager(docManager);
        modelSpecOWL.setDocumentManager(docManager);
    }

    public OntModel read(RDFInfo rdfInfo, boolean includeImport, boolean withInference) throws IOException {
        OntModel model = ModelFactory.createOntologyModel(
                withInference
                        ? modelSpecOWL_INF
                        : modelSpecOWL,
                ModelFactory.createDefaultModel());
        try (InputStream is = new ByteArrayInputStream(rdfInfo.getRdf().getBytes())) {
            model.read(is, null, rdfInfo.getRdfFormat().name());
        }
        if (includeImport) {
            docManager.loadImports(model);
        }
        return model;
    }

    public OntModel withInf(OntModel model) {
        return ModelFactory.createOntologyModel(modelSpecOWL_INF, model);
    }

    public void loadImports(OntModel model) {
        docManager.loadImports(model);
    }

    public void unloadImports(OntModel model) {
        model.listImportedOntologyURIs().forEach(x -> docManager.unloadImport(model, x));
    }

    public Set<String> getOntologyDefinitions(OntModel model) {
        return model.listSubjectsWithProperty(RDF.type, OWL.Ontology)
                .toSet().stream()
                .map(x -> x.getURI())
                .collect(Collectors.toSet());
    }

    public boolean checkImportsCIM(OntModel model) {
        return model.listImportedOntologyURIs().contains(CoreInformationModel.CIM_PREFIX);
    }

    public Set<String> getDefinedClasses(OntModel model) {
        return model.listResourcesWithProperty(RDF.type, RDFS.Class)
                .filterDrop(x -> x.isAnon())
                .mapWith(x -> x.getURI())
                .toSet();
    }

    public Set<String> getUsedClasses(OntModel model) {
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

    public Set<String> getUndefinedButUsedClasses(OntModel model) {
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
        } catch (Exception e) {

        } finally {
            try {
                out.close();
            } catch (IOException closeException) {
                // ignore
            }
        }
    }

    public Set<String> getDefinedResourcesInNamespace(OntModel model, String namespace) {
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

    public List<String> checkCardinalityViolations(OntModel pim, Model instance) {
        List<String> result = new ArrayList<>();
        pim.addSubModel(instance);
        result.addAll(executeSelectWithResults(pim, QUERY_CARDINALITY_EXACTLY_DATA_PROPERTY, "exact cardinaility for data property violated - "));
        result.addAll(executeSelectWithResults(pim, QUERY_CARDINALITY_EXACTLY_OBJECT_PROPERTY, "exact cardinaility for object property violated - "));
        result.addAll(executeSelectWithResults(pim, QUERY_CARDINALITY_MIN_DATA_PROPERTY, "min cardinaility for data property violated - "));
        result.addAll(executeSelectWithResults(pim, QUERY_CARDINALITY_MIN_OBJECT_PROPERTY, "min cardinaility for object property violated - "));
        result.addAll(executeSelectWithResults(pim, QUERY_CARDINALITY_MAX_DATA_PROPERTY, "max cardinaility for data property violated - "));
        result.addAll(executeSelectWithResults(pim, QUERY_CARDINALITY_MAX_OBJECT_PROPERTY, "max cardinaility for object property violated - "));
        pim.removeSubModel(instance);
        return result;
    }
    
    private List<String> executeSelectWithResults(OntModel model, String query, String message) {
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {            
            return StreamHelper.stream(qexec.execSelect())
                    .map(qs
                            -> message + StreamHelper.stream(qs.varNames())
                            .map(x -> x + ": " + qs.get(x))
                            .collect(Collectors.joining(",")))
                    .collect(Collectors.toList());
        }
    }


}
