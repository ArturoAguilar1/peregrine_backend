package com.peregrineteam.peregrine_backend;

import com.peregrineteam.peregrine_backend.entities.User;
import com.peregrineteam.peregrine_backend.responses.UsersResponse;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;

import java.util.ArrayList;

public class Main extends AbstractVerticle {
    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();

        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(5432)
                .setHost("localhost")
                .setDatabase("postgres")
                .setUser("postgres");

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

        PgPool client = PgPool.pool(vertx, connectOptions, poolOptions);

        Router router = Router.router(vertx);

        router.get("/users").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "application/json");

            UsersResponse usersResponse = new UsersResponse();
            usersResponse.users = new ArrayList<>();

            client.preparedQuery("SELECT id, name FROM users", ar -> {
                if (ar.succeeded()) {
                    RowSet<Row> rows = ar.result();

                    for (Row row : rows) {
                        User user = new User();
                        user.id = row.getLong(0);
                        user.name = row.getString(1);
                        usersResponse.users.add(user);
                    }

                    response.setStatusCode(200).end(Json.encode(usersResponse));
                } else {
                    response.setStatusCode(500).end(ar.cause().getMessage());
                }
            });
        });

        server.requestHandler(router).listen(3000);
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Main());
    }
}
