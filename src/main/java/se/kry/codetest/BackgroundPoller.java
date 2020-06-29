package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import java.util.ArrayList;
import java.util.List;

public class BackgroundPoller {

    public Future<List<Service>> pollServices(List<Service> services, WebClient client) {
        System.out.println("Background poller started");

        for (Service service : services) {
            client.get(80, service.getUrl(), "/")
                    .send(httpResponse -> {
                        if (httpResponse.succeeded()) {
                            HttpResponse<Buffer> response = httpResponse.result();

                            System.out.println("Received response with status code " + response.statusCode());
                            service.setResponse("OK");
                        } else {
                            System.out.println("Something went wrong "+ httpResponse.cause().getMessage());
                            service.setResponse("FAIL");
                        }
                    });
        }
        Future<List<Service>> serviceFuture = Future.future();
        serviceFuture.complete(services);
        return serviceFuture;
    }
}
