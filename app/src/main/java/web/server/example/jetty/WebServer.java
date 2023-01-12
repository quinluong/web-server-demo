package web.server.example.jetty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class WebServer {

    private AtomicBoolean _isRunning;
    private int _port;
    private int _minThreads;
    private int _maxThreads;
    private int _idleTimeoutInSeconds;
    private int _numberOfConnectors;
    private int _numberOfAcceptors;
    private int _numberOfSelectors;
    private int _acceptQueueSize;
    private Server _server;
    private Thread _thread;
    private List<String> _portList;

    public WebServer(Map<String, String> configs) throws Exception {
        if (configs == null || configs.isEmpty()) {
            throw new Exception("Invalid parameters");
        }

        _isRunning = new AtomicBoolean(false);
        _port = Integer.parseInt(configs.getOrDefault("port", "1"));
        _minThreads = Integer.parseInt(configs.getOrDefault("min-threads", "16"));
        _maxThreads = Integer.parseInt(configs.getOrDefault("max-threads", String.valueOf(_minThreads * 2)));
        _idleTimeoutInSeconds = Integer.parseInt(configs.getOrDefault("idle-timeout-in-seconds", "5"));
        _numberOfConnectors = Integer.parseInt(configs.getOrDefault("number-of-connectors", "1"));
        _numberOfAcceptors = Integer.parseInt(configs.getOrDefault("number-of-acceptors", "2"));
        _numberOfSelectors = Integer.parseInt(configs.getOrDefault("number-of-selectors", "4"));
        _acceptQueueSize = Integer.parseInt(configs.getOrDefault("accept-queue-size", "1000"));

        if (_port <= 0
                || _minThreads <= 0
                || _maxThreads < _minThreads
                || _idleTimeoutInSeconds <= 0
                || _numberOfConnectors <= 0
                || _numberOfAcceptors <= 0
                || _numberOfSelectors <= 0
                || _acceptQueueSize <= 0) {
            throw new Exception("Invalid parameters");
        }

        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("WebServer");
        threadPool.setMinThreads(_minThreads);
        threadPool.setMaxThreads(_maxThreads);
        threadPool.setIdleTimeout(_idleTimeoutInSeconds * 1000);

        _server = new Server(threadPool);
        _portList = new ArrayList();

        ServerConnector[] serverConnectors = new ServerConnector[_numberOfConnectors];
        for (int i = 0; i < _numberOfConnectors; i++) {
            int port = _port + i;

            ServerConnector serverConnector = new ServerConnector(_server, _numberOfAcceptors, _numberOfSelectors);
            serverConnector.setHost("0.0.0.0");
            serverConnector.setPort(port);
            serverConnector.setIdleTimeout(_idleTimeoutInSeconds * 1000);
            serverConnector.setAcceptQueueSize(_acceptQueueSize);
            serverConnectors[i] = serverConnector;

            _portList.add(String.valueOf(port));
        }

        _server.setConnectors(serverConnectors);
        _server.setStopAtShutdown(true);

        GzipHandler gzipHandler = new GzipHandler();
        gzipHandler.setHandler(new WebServerHandler());
        gzipHandler.setMinGzipSize(0);

        _server.setHandler(gzipHandler);
    }

    public boolean start() {
        if (_server == null) {
            return false;
        }

        if (_isRunning.get()) {
            return false;
        }

        try {
            _server.start();

            _thread = new Thread(() -> {
                System.out.println("Starting WebServer at port(s): " + String.join(",", _portList));

                _isRunning.set(true);

                try {
                    _server.join();
                } catch (InterruptedException ex) {
                    System.err.println(ex.getMessage());
                }

                _isRunning.set(false);
            });

            _thread.start();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            stop();
        }

        return true;
    }

    public void stop() {
        if (_server == null || !_isRunning.get()) {
            return;
        }

        try {
            _server.stop();

            if (_thread != null) {
                _thread.join();
                _thread = null;
            } else {
                _isRunning.set(false);
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        _server.destroy();
        _server = null;
    }

}
