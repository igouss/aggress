package com.naxsoft;

/**
 * Copyright NAXSoft 2015
 */
public class ApplicationContext {
    private final boolean invalidateTemplateCache;

    public ApplicationContext(boolean invalidateTemplateCache) {
        this.invalidateTemplateCache = invalidateTemplateCache;
    }

    public boolean isInvalidateTemplateCache() {
        return invalidateTemplateCache;
    }
}
