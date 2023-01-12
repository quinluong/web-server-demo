package web.server.example;

import java.util.HashMap;
import java.util.Map;
import web.server.example.jetty.WebServer;

public class App {

    public static void main(String[] args) throws Exception {
        Map<String, String> configs = new HashMap();
        configs.put("port", "1234");

        WebServer webServer = new WebServer(configs);
        webServer.start();
    }
}
