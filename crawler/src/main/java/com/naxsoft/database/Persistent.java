package com.naxsoft.database;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.hibernate.StatelessSession;
import rx.Observable;
import rx.functions.Func1;

public interface Persistent extends AutoCloseable, Cloneable {
    void close();

    Observable<Long> getUnparsedCount();

    Observable<Long> getUnparsedCount(String type);

    Observable<Integer> markWebPageAsParsed(Long webPageEntryId);

    Observable<Integer> markAllProductPagesAsIndexed();

    Observable<Boolean> save(ProductEntity productEntity);

    Observable<Boolean> save(WebPageEntity webPageEntity);

    Observable<ProductEntity> getProducts();

    Observable<WebPageEntity> getUnparsedByType(String type);


    <R> Observable<R> executeTransaction(Func1<StatelessSession, R> action);

    <T> Observable<T> scroll(String queryString);
}
