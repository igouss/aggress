package com.naxsoft;

/**
 * Copyright NAXSoft 2015
 */
public class ApplicationContext {
    private boolean invalidateTemplateCache;

    public void setInvalidateTemplateCache(boolean invalidateTemplateCache) {
        this.invalidateTemplateCache = invalidateTemplateCache;
    }

    public boolean isInvalidateTemplateCache() {
        return invalidateTemplateCache;
    }
}