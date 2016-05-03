package com.naxsoft.database;

import com.naxsoft.entity.SourceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 *
 */
public class SourceService {
    private final static Logger LOGGER = LoggerFactory.getLogger(SourceService.class);
    private final Persistent database;

    /**
     * @param database
     */
    public SourceService(Persistent database) {
        this.database = database;
    }

    /**
     * @return
     */
    public Observable<SourceEntity> getSources() {
        String queryString = "from SourceEntity as s where s.enabled = true order by rand()";
        return database.scroll(queryString);
    }

    /**
     * @param sourceEntity
     * @return
     */
    public Observable<Boolean> save(SourceEntity sourceEntity) {
        return database.executeTransaction(session -> {
            LOGGER.debug("Saving {}", sourceEntity);
            session.insert(sourceEntity);
            return true;
        });
    }
}
