package org.swisspush.gateleen.routing;

import io.vertx.core.*;
import io.vertx.core.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;


/**
 * Decorates an {@link HttpClient} to only effectively close the client
 * if there are no more requests in progress.
 * <p>
 * HINT: We for now only fix the issue in the exact call we know to misbehave
 * in our concrete scenario. Feel free to implement the (few...) remaining
 * methods.
 */
public class DeferCloseHttpClient implements HttpClient {

    private final int CLOSE_ANYWAY_AFTER_MS = 86_400_000; // <- TODO: Find a good value.
    private static final Logger logger = LoggerFactory.getLogger(DeferCloseHttpClient.class);
    private final Vertx vertx;
    private final HttpClient delegate;
    private int countOfRequestsInProgress = 0;
    private boolean doCloseWhenDone = false;

    /**
     * See {@link DeferCloseHttpClient}.
     */
    public DeferCloseHttpClient(Vertx vertx, HttpClient delegate) {
        this.vertx = vertx;
        this.delegate = delegate;
    }


    @Override
    public void request(HttpMethod method, int port, String host, String requestURI, Handler<AsyncResult<HttpClientRequest>> handler) {
        logger.debug("({}:{}).request({}, \"{}\")", host, port, method, requestURI);
        countOfRequestsInProgress += 1;
        logger.debug("Pending request count: {}", countOfRequestsInProgress);
        delegate.request(method, port, host, requestURI).onComplete(handler);
    }

    private void onEndOfRequestResponseCycle() {
        countOfRequestsInProgress -= 1;
        logger.debug("Pending request count: {}", countOfRequestsInProgress);
        if (countOfRequestsInProgress == 0 && doCloseWhenDone) {
            logger.debug("No pending request right now. And someone called 'close()' earlier. So close now.");
            doCloseWhenDone = false;
            try {
                delegate.close();
            } catch (Exception e) {
                logger.warn("delegate.close() failed", e);
            }
        }
    }

    @Override
    public Future<Void> close() {
        if (countOfRequestsInProgress > 0) {
            logger.debug("Do NOT close right now. But close as soon there are no more pending requests (pending={})", countOfRequestsInProgress);
            doCloseWhenDone = true;
            // Still use a timer. Because who knows.
            vertx.setTimer(CLOSE_ANYWAY_AFTER_MS, timerId -> {
                if (doCloseWhenDone) {
                    logger.warn("RequestResponse cycle still running after {} ms. Will close now to prevent resource leaks.", CLOSE_ANYWAY_AFTER_MS);
                    doCloseWhenDone = false;
                    delegate.close();
                }
            });
            return Future.succeededFuture();
        }
        logger.debug("Client idle. Close right now");
        delegate.close();
        return Future.succeededFuture();
    }

    ///////////////////////////////////////////////////////////////////////////////
    // Below are only the remaining methods which all just delegate.
    ///////////////////////////////////////////////////////////////////////////////
    @Override
    public Future<HttpClientRequest> request(RequestOptions options) {
        return delegate.request(options);
    }


    @Override
    public void request(RequestOptions options, Handler<AsyncResult<HttpClientRequest>> handler) {
        delegate.request(options, handler);
    }

    @Override
    public Future<HttpClientRequest> request(HttpMethod method, int port, String host, String requestURI) {
        return delegate.request(method, port, host, requestURI);
    }

    @Override
    public void request(HttpMethod method, String host, String requestURI, Handler<AsyncResult<HttpClientRequest>> handler) {
        delegate.request(method, host, requestURI, handler);
    }

    @Override
    public Future<HttpClientRequest> request(HttpMethod method, String host, String requestURI) {
        return delegate.request(method, host, requestURI);
    }

    @Override
    public void request(HttpMethod method, String requestURI, Handler<AsyncResult<HttpClientRequest>> handler) {
        delegate.request(method, requestURI, handler);
    }

    @Override
    public Future<HttpClientRequest> request(HttpMethod method, String requestURI) {
        return delegate.request(method, requestURI);
    }

    @Override
    public void webSocket(int port, String host, String requestURI, Handler<AsyncResult<WebSocket>> handler) {
        delegate.webSocket(port, host, requestURI, handler);
    }

    @Override
    public Future<WebSocket> webSocket(int port, String host, String requestURI) {
        return delegate.webSocket(port, host, requestURI);
    }

    @Override
    public void webSocket(String host, String requestURI, Handler<AsyncResult<WebSocket>> handler) {
        delegate.webSocket(host, requestURI, handler);
    }

    @Override
    public Future<WebSocket> webSocket(String host, String requestURI) {
        return delegate.webSocket(host, requestURI);
    }

    @Override
    public void webSocket(String requestURI, Handler<AsyncResult<WebSocket>> handler) {
        delegate.webSocket(requestURI, handler);
    }

    @Override
    public Future<WebSocket> webSocket(String requestURI) {
        return delegate.webSocket(requestURI);
    }

    @Override
    public void webSocket(WebSocketConnectOptions options, Handler<AsyncResult<WebSocket>> handler) {
        delegate.webSocket(options, handler);
    }

    @Override
    public Future<WebSocket> webSocket(WebSocketConnectOptions options) {
        return delegate.webSocket(options);
    }

    @Override
    public void webSocketAbs(String url, MultiMap headers, WebsocketVersion version, List<String> subProtocols, Handler<AsyncResult<WebSocket>> handler) {
        delegate.webSocketAbs(url, headers, version, subProtocols, handler);
    }

    @Override
    public Future<WebSocket> webSocketAbs(String url, MultiMap headers, WebsocketVersion version, List<String> subProtocols) {
        return delegate.webSocketAbs(url, headers, version, subProtocols);
    }

    @Override
    public HttpClient connectionHandler(Handler<HttpConnection> handler) {
        return delegate.connectionHandler(handler);
    }

    @Override
    public HttpClient redirectHandler(Function<HttpClientResponse, Future<RequestOptions>> handler) {
        return delegate.redirectHandler(handler);
    }

    @Override
    public Function<HttpClientResponse, Future<RequestOptions>> redirectHandler() {
        return delegate.redirectHandler();
    }

    @Override
    public void close(Handler<AsyncResult<Void>> handler) {
        delegate.close(handler);
    }
}
