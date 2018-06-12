package com.naxsoft;

public class ApplicationContext {
    private boolean invalidateTemplateCache = false;

    public boolean isInvalidateTemplateCache() {
        return invalidateTemplateCache;
    }

    public void setInvalidateTemplateCache(boolean invalidateTemplateCache) {
        this.invalidateTemplateCache = invalidateTemplateCache;
    }
}
