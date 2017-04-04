package eu.h2020.symbiote.model;

import eu.h2020.symbiote.core.model.RDFInfo;

/**
 *
 *
 * Created by Szymon Mueller on 17/03/2017.
 */
public class InformationModel extends RDFInfo {

    //TODO decide if we need separate id and uri, or uri == id ? then we can remove uri from this declaration
    private String id;

    private String uri;

    public InformationModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
