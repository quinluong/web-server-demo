package web.server.example.jetty;

import com.google.gson.JsonObject;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class WebServerHandler extends AbstractHandler {

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        String content = "{}";

        target = target.toLowerCase();

        switch (target) {
            case "/api/get":
                content = get(request);
                break;

            case "/stats":
                break;
        }

        response.setContentLength(content.length());

        try (PrintWriter printWriter = response.getWriter()) {
            printWriter.print(content);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    private String get(HttpServletRequest request) {
        JsonObject resultData = new JsonObject();
        String myKey = request.getParameter("my_key");
        resultData.addProperty("my_key", myKey);

        JsonObject result = new JsonObject();
        result.addProperty("error_code", 0);
        result.addProperty("error_message", "Success");
        result.add("data", resultData);

        return result.toString();
    }
}
