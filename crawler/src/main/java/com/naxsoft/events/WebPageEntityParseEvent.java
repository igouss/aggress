package com.naxsoft.events;

import com.lambdaworks.redis.event.Event;
import com.naxsoft.entity.WebPageEntity;

class WebPageEntityParseEvent implements Event {
    private final WebPageEntity entity;

    public WebPageEntityParseEvent(WebPageEntity entity) {
        this.entity = entity;
    }

    public WebPageEntity getEntity() {
        return entity;
    }
}
