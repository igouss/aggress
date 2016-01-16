package com.naxsoft;

/**
 * Copyright NAXSoft 2015
 *
 *
 *
 */
public class ApplicationContext {
    private boolean invalidateTemplateCache = false;

    /**
     *
     * @param invalidateTemplateCache
     */
    public void setInvalidateTemplateCache(boolean invalidateTemplateCache) {
        this.invalidateTemplateCache = invalidateTemplateCache;
    }

    /**
     *
     * @return
     */
    public boolean isInvalidateTemplateCache() {
        return invalidateTemplateCache;
    }
}
