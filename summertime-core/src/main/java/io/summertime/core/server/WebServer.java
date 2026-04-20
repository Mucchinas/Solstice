package io.summertime.core.server;

import com.sun.net.httpserver.HttpServer;
import io.summertime.core.common.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer {

    private final HttpServer server;

    public WebServer(int port, RequestHandler requestHandler) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/", requestHandler::handle);
        this.server.setExecutor(null); // use the default implementation
        Logger.info("WebServer configured to run on port " + port);
    }

    public void start() {
        server.start();
        Logger.info("WebServer started.");
    }

    public void stop() {
        server.stop(0);
        Logger.info("WebServer stopped.");
    }
}
