package com.peregrineteam.peregrine_backend;

import com.peregrineteam.peregrine_backend.database.Database;
import com.peregrineteam.peregrine_backend.controllers.Users;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.mongo.MongoClient;

public class Main extends AbstractVerticle {
    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();

        MongoClient mongoClient = Database.initializeDb(vertx);

        Router router = Router.router(vertx);

        router.route().handler(routingContext -> {
            routingContext.put("db", mongoClient);
            routingContext.next();
        });

        router.route().handler(BodyHandler.create());
        router.get("/api/users").handler(Users::getUsers);
        router.post("/api/users").handler(Users::createUser);
        router.get("/api/users/:userId").handler(Users::getUserById);

        router.get("/api/users/:userId/addresses").handler(Users::getAddresses);
        router.post("/api/users/:userId/addresses").handler(Users::createAddress);
        router.get("/api/users/:userId/addresses/clear").handler(Users::clearAddresses);
        router.get("/api/users/:userId/addresses/:addressId").handler(Users::setAddress);

        server.requestHandler(router).listen(3000, ar ->
                System.out.println("Server running on port "+ ar.result().actualPort()));
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Main());
    }
}
