package com.peregrineteam.peregrine_backend;

import com.peregrineteam.peregrine_backend.database.Database;
import com.peregrineteam.peregrine_backend.controllers.Users;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgPool;

public class Main extends AbstractVerticle {
    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();

        PgPool client = Database.initializeDb(vertx);

        Router router = Router.router(vertx);

        router.route().handler(routingContext -> {
            routingContext.put("db", client);
            routingContext.next();
        });

        router.route().handler(BodyHandler.create());
        router.get("/api/users").handler(Users::getUsers);
        router.get("/api/users/:id").handler(Users::getUserById);

        server.requestHandler(router).listen(3000, ar ->
                System.out.println("Server running on port "+ ar.result().actualPort()));
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Main());
    }
}
