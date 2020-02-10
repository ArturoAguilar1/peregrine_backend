package com.peregrineteam.peregrine_backend.controllers;

import com.peregrineteam.peregrine_backend.responses.UsersResponse;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;

public class Users {
    public static void getUsers(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");

        PgPool client = routingContext.get("db");

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
    }

    public static void getUserById(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");
        Long userId = Long.parseLong(routingContext.request().getParam("id"));

        PgPool client = routingContext.get("db");

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
    }

    private static UsersResponse.User buildUser(Row row) {
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
