package com.naxsoft.performance;

import com.naxsoft.parsers.webPageParsers.Wolverinesupplies.WolverinesuppliesProductPageParser;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public class HibernateStats {
    private static final Logger logger = LoggerFactory.getLogger(HibernateStats.class);
    private SessionFactory sessionFactory;

    public HibernateStats(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Observable<Statistics> getStatistics() {
        Statistics stats = sessionFactory.getStatistics();
        return Observable.just(stats);
    }
//    HibernateStats hibernateStats = new HibernateStats(db.getSessionFactory());
//    Observable.interval(0, 15, TimeUnit.SECONDS).flatMap(i -> hibernateStats.getStatistics()).subscribeOn(Schedulers.computation()).observeOn(Schedulers.io()).subscribe(stats -> {
//        stats.logSummary();
//        logger.info(stats.toString());
//    });

}
