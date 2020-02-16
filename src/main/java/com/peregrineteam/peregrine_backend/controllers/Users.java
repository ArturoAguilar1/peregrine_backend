package com.peregrineteam.peregrine_backend.controllers;

import com.peregrineteam.peregrine_backend.responses.UsersResponse;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.UUID;

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
                    UsersResponse.User user = buildUser(row, null);
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
        Long userId = Long.parseLong(routingContext.request().getParam("userId"));

        PgPool client = routingContext.get("db");

        ArrayList<UsersResponse.Location> locationsResponse = new ArrayList<>();

        client.getConnection(ar1 -> {
            if (ar1.succeeded()) {
                SqlConnection connection = ar1.result();

                connection.preparedQuery("SELECT * FROM users WHERE id=$1", Tuple.of(userId), ar2 -> {
                    if (ar2.succeeded()) {
                        connection.preparedQuery("SELECT * FROM locations WHERE user_id=$1", Tuple.of(userId), ar3 -> {
                            if (ar3.succeeded()) {
                                RowSet<Row> locationsRows = ar3.result();

                                for (Row locationRow : locationsRows) {
                                    locationsResponse.add(buildLocation(locationRow));
                                }
                            }
                            RowSet<Row> rows = ar2.result();
                            UsersResponse.User userResponse = null;
                            for (Row userRow : rows) {
                                userResponse = buildUser(userRow, locationsResponse);
                            }
                            response.setStatusCode(200).end(Json.encode(userResponse));
                            connection.close();
                        });
                    } else {
                        response.setStatusCode(500).end(ar2.cause().getMessage());
                        connection.close();
                    }
                });
            }
        });
    }

    public static void createUser(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");

        PgPool client = routingContext.get("db");

        JsonObject body = routingContext.getBodyAsJson();

        client.preparedQuery("INSERT INTO users (id, uuid, name, email, email_verified, phone, access, country_id) VALUES (DEFAULT, $1, $2, $3, DEFAULT, $4, $5, $6)",
                Tuple.of(
                        UUID.randomUUID(),
                        body.getString("name"),
                        body.getString("email"),
                        body.getString("phone"),
                        body.getString("access"),
                        body.getString("country_id")
                ),  ar -> {
            if (ar.succeeded()) {
                response.setStatusCode(200).end(Json.encode("OK"));
            } else {
                response.setStatusCode(500).end(ar.cause().getMessage());
            }
        });
    }

    public static void createLocation(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");
        Long userId = Long.parseLong(routingContext.request().getParam("userId"));

        PgPool client = routingContext.get("db");

        JsonObject body = routingContext.getBodyAsJson();

        client.preparedQuery("INSERT INTO locations (id, address, floor, extra_info, zip_code, country_id, user_id, is_default) VALUES (DEFAULT, $1, $2, $3, $4, $5, $6, DEFAULT)",
                Tuple.of(
                        body.getString("address"),
                        body.getString("floor"),
                        body.getString("extra_info"),
                        body.getLong("zip_code"),
                        body.getString("country_id"),
                        userId
                ),  ar -> {
                    if (ar.succeeded()) {
                        response.setStatusCode(200).end(Json.encode("OK"));
                    } else {
                        response.setStatusCode(500).end(ar.cause().getMessage());
                    }
                });
    }

    public static void getLocations(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");
        Long userId = Long.parseLong(routingContext.request().getParam("userId"));

        PgPool client = routingContext.get("db");

        ArrayList<UsersResponse.Location> locationsResponse = new ArrayList<>();

        client.preparedQuery("SELECT * FROM locations WHERE user_id=$1", Tuple.of(userId), ar -> {
            if (ar.succeeded()) {
                RowSet<Row> rows = ar.result();

                for (Row row : rows) {
                    UsersResponse.Location location = buildLocation(row);
                    locationsResponse.add(location);
                }

                response.setStatusCode(200).end(Json.encode(locationsResponse));
            } else {
                response.setStatusCode(500).end(ar.cause().getMessage());
            }
        });
    }

    private static UsersResponse.User buildUser(Row row, ArrayList<UsersResponse.Location> locations) {
        UsersResponse.User user = new UsersResponse.User();
        user.id = row.getLong(7);
        user.uuid = row.getUUID(0);
        user.name = row.getString(1);
        user.email = row.getString(2);
        user.email_verified = row.getBoolean(3);
        user.phone = row.getString(4);
        user.access = row.getString(5);
        user.country_id = row.getString(6);
        if (locations != null) {
            user.locations = locations;
        }
        return user;
    }

    private static UsersResponse.Location buildLocation(Row row) {
        UsersResponse.Location location = new UsersResponse.Location();
        location.id = row.getLong(0);
        location.address = row.getString(1);
        location.floor = row.getString(2);
        location.extra_info = row.getString(3);
        location.zip_code = row.getLong(4);
        location.country_id = row.getString(5);
        location.user_id = row.getLong(6);
        location.is_default = row.getBoolean(7);
        return location;
    }
}
