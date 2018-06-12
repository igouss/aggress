package com.naxsoft.modules;

import com.naxsoft.Crawler;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class CrawlerModule {
    Crawler crawler;

    public CrawlerModule(Crawler crawler) {
        this.crawler = crawler;
    }

    @Provides
    @Singleton
    Crawler providesCrawler() {
        return crawler;
    }
}