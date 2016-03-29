package com.naxsoft.providers;

import com.naxsoft.database.Database;
import com.naxsoft.database.WebPageService;
import dagger.Module;
import dagger.Provides;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
@Module(includes = {DatabaseModule.class})
public class WebPageServiceModule {
    @Provides
    @Singleton
    @NotNull
    public WebPageService get(Database db) {
        return new WebPageService(db);
    }
}
