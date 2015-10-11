package com.naxsoft.crawler;

import com.ning.http.client.AsyncHandler;
import rx.Observable;
import rx.functions.Func0;

import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
public class ObservableAsyncClient {
    public static <T> Observable<T> get(String url, AsyncHandler<T> handler) {
        return Observable.using(
                AsyncFetchClient::new,
                client -> Observable.from(client.get(url, handler)),
                AsyncFetchClient::close);
    }

//    public static <T> Observable<Future<T>> get(Observable<String> urls, AsyncHandler<T> handler) {
//        return Observable.using(
//                (Func0<AsyncFetchClient<T>>) AsyncFetchClient::new,
//                client -> urls.map(url -> client.get(url, handler)),
//                AsyncFetchClient::close);
//    }

    public static <T> Observable<T> post(String url, String content, AsyncHandler<T> handler) {
        return Observable.using(
                AsyncFetchClient::new,
                client -> Observable.from(client.post(url, content, handler)),
                AsyncFetchClient::close);
    }
}