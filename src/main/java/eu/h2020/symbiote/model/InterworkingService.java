package eu.h2020.symbiote.model;

/**
 * Created by Mael on 17/03/2017.
 */
public class InterworkingService {

    private String id;

    private String url;

    private String informationModelId;

    public InterworkingService() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getInformationModelId() {
        return informationModelId;
    }

    public void setInformationModelId(String informationModelId) {
        this.informationModelId = informationModelId;
    }


}
