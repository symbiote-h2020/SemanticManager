package eu.h2020.symbiote;

import eu.h2020.symbiote.core.internal.InformationModelValidationResult;
import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.core.internal.ResourceInstanceValidationRequest;
import eu.h2020.symbiote.core.internal.ResourceInstanceValidationResult;
import eu.h2020.symbiote.model.mim.InformationModel;
import eu.h2020.symbiote.ontology.SemanticManager;
import eu.h2020.symbiote.ontology.errors.PropertyNotFoundException;
import eu.h2020.symbiote.ontology.utils.SymbioteModelsUtil;

import java.io.IOException;
import java.util.Formatter;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static eu.h2020.symbiote.TestSetupConfig.*;
import eu.h2020.symbiote.semantics.ModelHelper;
import eu.h2020.symbiote.semantics.ontology.BIM;

/**
 * Created by Szymon Mueller on 03/05/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationTests {

    private String TEMPERATURE_NAME = "temperature";
    private String CARBON_MONOXIDE = "carbonMonoxideConcentration";
    private String NITROGEN = "nitrogenDioxideConcentration";
    private String PH = "pH";
    private String NONEXISTENT_NAME = "temperature1234566789";
//    private String BIM_RESOURCE_FILE = "/bim_resource.ttl";
    private String BIM_RESOURCE_FILE = "/bim_from_rest.ttl";

    @Before
    public void init() {
        loadBIM();
    }

    @Test
    public void testFindInSymbioteModelsFromQU() {
        try {
            String temperatureUri = SymbioteModelsUtil.findInSymbioteCoreModels(TEMPERATURE_NAME);
            assertNotNull(temperatureUri);
            System.out.println("Found temperature");
        } catch (PropertyNotFoundException e) {
            e.printStackTrace();
            fail("Property " + TEMPERATURE_NAME + " should be found in QUREC");
        }

        try {
            String temperatureUri = SymbioteModelsUtil.findInSymbioteCoreModels(CARBON_MONOXIDE);
            assertNotNull(temperatureUri);
            System.out.println("Found carbon monoxide");
        } catch (PropertyNotFoundException e) {
            e.printStackTrace();
            fail("Property " + CARBON_MONOXIDE + " should be found in BIM");
        }

        try {
            String nitrogen = SymbioteModelsUtil.findInSymbioteCoreModels(NITROGEN);
            assertNotNull(nitrogen);
            System.out.println("Found nitrogen");
        } catch (PropertyNotFoundException e) {
            e.printStackTrace();
            fail("Property " + NITROGEN + " should be found in BIM");
        }

        try {
            System.out.println("ph");
            String temperatureUri = SymbioteModelsUtil.findInSymbioteCoreModels(PH);
            assertNotNull(temperatureUri);
            System.out.println("Found ph");
        } catch (PropertyNotFoundException e) {
            e.printStackTrace();
            fail("Property " + PH + " should be found in BIM");
        }

        try {
            SymbioteModelsUtil.findInSymbioteCoreModels(NONEXISTENT_NAME);
            fail();
        } catch (PropertyNotFoundException e) {
        }
    }

    //TODO why apparently BIM does not have owl:Ontology?
    @Test
    public void testLoadBIMasPIM() {
        InformationModel rdfInfo = new InformationModel();
        try {
            rdfInfo.setRdfFormat(RDFFormat.Turtle);
            rdfInfo.setRdf(ModelHelper.writeAll(ModelHelper.readModel(BIM.getURI(), false, false), RDFFormat.Turtle));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Error occurred during ModelHelper.readModel(BIM.getURI())=" + BIM.getURI() + " msg: " + e.getMessage());
        }
        InformationModelValidationResult validationResult = SemanticManager.getManager().validatePIMMetaModel(rdfInfo);
        if (!validationResult.isSuccess()) {
            System.out.println(validationResult.getMessage());
            fail("Validation was not successful: " +validationResult.getMessage());
        }
    }

    @Test
    public void bimResourceValidationTest() {
        try {

            ResourceInstanceValidationRequest request = new ResourceInstanceValidationRequest();
            request.setInformationModelId("BIM");
            request.setRdfFormat(RDFFormat.Turtle);
            String resourceRdf = IOUtils.toString(this.getClass()
                    .getResource("/retailerDevice.ttl"));
            request.setRdf(resourceRdf);
            ResourceInstanceValidationResult result = SemanticManager.getManager().validateResourcesInstance(request);
            assertNotNull(result);
            assertNotNull(result.getObjectDescription());
            assertTrue(result.isSuccess());
            assertEquals("Should find 1 resource", 1, result.getObjectDescription().size());
            System.out.println("Result RDF:");
            System.out.println(result.getObjectDescription().values().iterator().next().getRdf());


        } catch (IOException e) {
            e.printStackTrace();
            fail("Error occurred when loading model from file");
        }
    }
}
