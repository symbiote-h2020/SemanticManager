package eu.h2020.symbiote.model;


import eu.h2020.symbiote.core.model.RDFInfo;

/**
 * Class storing the Meta Information Model information about the model.
 *
 * Created by Mael on 16/03/2017.
 */
public class PIMMetaModelDescription extends RDFInfo {

    private String uri;

    public PIMMetaModelDescription() {
    }

    public PIMMetaModelDescription(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }


}
