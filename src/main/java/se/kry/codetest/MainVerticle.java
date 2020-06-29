package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.commons.validator.routines.UrlValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS"); // 2020-06-29 12:46:52.2524484
    private final BackgroundPoller poller = new BackgroundPoller();
    private final List<Service> services = new ArrayList<Service>();
    private DBConnector connector;

    @Override
    public void start(Future<Void> startFuture) {
        LoadServices();

        StartBackgroundPoller();

        SetupRouting(startFuture);
    }

    private void LoadServices() {
        connector = new DBConnector(vertx);
        getServicesFromDB().setHandler(waitForQueryToComplete -> {
            if (waitForQueryToComplete.succeeded()) {
                // wait for DB to load existing services before adding new ones
                addAndRemoveServices();
            }
        });
    }

    private void addAndRemoveServices() {
        addNewService(new Service("Malformd URL", "wwwforgotsomethingdotcom", "SomeUser1"));
        addNewService(new Service("Google", "https://www.google.com", "SomeUser1"));
        addNewService(new Service("Kry", "https://www.kry.se", "SomeUser1"));
        addNewService(new Service("DN", "https://www.dn.se", "SomeUser1"));

//        removeService(new Service("Google", "https://www.google.com", "SomeUser1"));
//        removeService(new Service("Kry", "https://www.kry.se", "SomeUser1"));
//        removeService(new Service("DN", "https://www.dn.se", "SomeUser1"));
    }

    private void StartBackgroundPoller() {
        WebClient client = WebClient.create(vertx);
        vertx.setPeriodic(1000 * 30, timerId -> poller.pollServices(services, client));
    }

    private Future<ResultSet> getServicesFromDB() {
        Future<ResultSet> query = Future.future();
        String sql = "Select * from service";
        connector.query(sql).setHandler(asyncResult -> {
            if (asyncResult.succeeded()) {
                System.out.println("services found in DB: " + asyncResult.result().getRows().size());
                for (JsonObject row : asyncResult.result().getRows()) {
                    services.add(new Service(
                            row.getString("url"),
                            row.getString("name"),
                            row.getString("response"),
                            LocalDateTime.parse(row.getString("added"), formatter),
                            row.getString("addedbyuser")
                    ));
                }
                query.complete();
            }
        });

        return query;

    }

    private Future<ResultSet> addNewService(Service service) {
        Future<ResultSet> query = Future.future();
        if (new UrlValidator().isValid(service.getUrl())) {
            services.add(service);
            String sql = "INSERT INTO service (url, name, response, addedbyuser, added) VALUES ('" + service.getUrl() + "', '" + service.getName() + "', '" + service.getResponse() + "', '" + service.getAddedByUser() + "', '" + java.sql.Timestamp.valueOf(service.getDateTime()) + "')";
            connector.query(sql).setHandler(asyncResult -> {
                if (asyncResult.succeeded()) {
                    query.complete();
                }
            });
        } else {
            System.out.println(service.getName() + " was not added, URL is not valid: " + service.getUrl());
        }
        return query;
    }

    private Future<ResultSet> removeService(Service service) {
        services.removeIf(s -> s.getUrl() == service.getUrl() && s.getAddedByUser() == service.getAddedByUser());

        Future<ResultSet> query = Future.future();
        String sql = "DELETE FROM service WHERE url='" + service.getUrl() + "' AND addedbyuser = '" + service.getAddedByUser() + "'";
        connector.query(sql).setHandler(asyncResult -> {
            if (asyncResult.succeeded()) {
                query.complete();
            }
        });
        return query;
    }

    private void SetupRouting(Future<Void> startFuture) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        setRoutes(router);

        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        System.out.println("KRY code test service started");
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }

    private void setRoutes(Router router) {
        router.route("/*").handler(StaticHandler.create());
        router.get("/service").handler(req -> {
            List<JsonObject> jsonServices = services
                    .stream()
                    .map(service ->
                            new JsonObject()
                                    .put("name", service.getName())
                                    .put("status", service.getResponse()))
                    .collect(Collectors.toList());
            req.response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonArray(jsonServices).encode());
        });
        router.post("/service").handler(req -> {
            JsonObject jsonBody = req.getBodyAsJson();

            //services.put(jsonBody.getString("url"), "UNKNOWN");

            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("OK");
        });
    }

}



