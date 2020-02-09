package com.peregrineteam.peregrine_backend;

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
                .setHost("peregrine-db-demo.cdk0dshceixr.us-east-2.rds.amazonaws.com")
                .setDatabase("peregrine")
                .setUser("postgres")
                .setPassword("Ines1234");

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

        PgPool client = PgPool.pool(vertx, connectOptions, poolOptions);

        Router router = Router.router(vertx);

        router.get("/users").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "application/json");

            UsersResponse usersResponse = new UsersResponse();
            usersResponse.users = new ArrayList<>();

            client.preparedQuery("SELECT * FROM users", ar -> {
                if (ar.succeeded()) {
                    RowSet<Row> rows = ar.result();

                    for (Row row : rows) {
                        UsersResponse.User user = buildUser(row);
                        usersResponse.users.add(user);
                    }

                    response.setStatusCode(200).end(Json.encode(usersResponse));
                } else {
                    response.setStatusCode(500).end(ar.cause().getMessage());
                }
            });
        });

        router.get("/users/:id").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "application/json");
            Long userId = Long.parseLong(routingContext.request().getParam("id"));

            client.preparedQuery("SELECT * FROM users WHERE id=$1", Tuple.of(userId), ar -> {
                if (ar.succeeded()) {
                    RowSet<Row> rows = ar.result();

                    UsersResponse.User userResponse = null;

                    for (Row row : rows) {
                        userResponse = buildUser(row);
                    }

                    response.setStatusCode(200).end(Json.encode(userResponse));
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

    public static UsersResponse.User buildUser(Row row) {
        UsersResponse.User user = new UsersResponse.User();
        user.id = row.getLong(7);
        user.uuid = row.getUUID(0);
        user.name = row.getString(1);
        user.email = row.getString(2);
        user.email_verified = row.getBoolean(3);
        user.phone = row.getString(4);
        user.access = row.getString(5);
        user.country_id = row.getString(6);
        return user;
    }
}
