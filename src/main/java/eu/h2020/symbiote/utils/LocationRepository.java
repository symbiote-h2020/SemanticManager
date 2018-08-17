package eu.h2020.symbiote.utils;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Szymon Mueller on 28/06/2018.
 */
@Repository
public interface LocationRepository extends MongoRepository<LocationInfo,String> {

    public List<LocationInfo> findByPlatformId(String platformId);

}
