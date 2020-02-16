package com.peregrineteam.peregrine_backend.controllers;

import com.peregrineteam.peregrine_backend.responses.UsersResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.UUID;

public class Users {
    public static void getUsers(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");

        MongoClient mongoClient = routingContext.get("db");

        UsersResponse usersResponse = new UsersResponse();
        usersResponse.users = new ArrayList<>();

        JsonObject query = new JsonObject();
        mongoClient.find("users", query, res -> {
            if (res.succeeded()) {
                for (JsonObject json : res.result()) {
                    UsersResponse.User user = buildUser(json);
                    usersResponse.users.add(user);
                }
                response.setStatusCode(200).end(Json.encode(res.result()));
            } else {
                res.cause().printStackTrace();
                response.setStatusCode(500).end(res.cause().getMessage());
            }
        });
    }

    public static void createUser(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");

        MongoClient mongoClient = routingContext.get("db");

        JsonObject body = routingContext.getBodyAsJson();

        JsonObject user = new JsonObject()
                .put("uuid", UUID.randomUUID().toString())
                .put("name", body.getString("name"))
                .put("email", body.getString("email"))
                .put("email_verified", body.getBoolean("email_verified"))
                .put("phone", body.getString("phone"))
                .put("access",  body.getString("access"))
                .put("country_id", body.getString("country_id"))
                .put("addresses", new JsonArray())
                .put("pictures", new JsonArray());

        mongoClient.insert("users", user, res -> {
            if (res.succeeded()) {
                String id = res.result();
                response.setStatusCode(200).end(Json.encode("OK id: " + id));
            } else {
                res.cause().printStackTrace();
                response.setStatusCode(500).end(res.cause().getMessage());
            }
        });
    }

    public static void createAddress(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");
        String userId = routingContext.request().getParam("userId");

        MongoClient mongoClient = routingContext.get("db");

        JsonObject body = routingContext.getBodyAsJson();

        JsonObject address = new JsonObject()
                .put("_id", new ObjectId().toHexString())
                .put("address", body.getString("address"))
                .put("floor", body.getString("floor"))
                .put("extra_info", body.getString("extra_info"))
                .put("zip_code",  body.getString("zip_code"))
                .put("country_id", body.getString("country_id"))
                .put("is_default", body.getBoolean("is_default"));

        JsonObject query = new JsonObject()
                .put("_id", userId);
        JsonObject update = new JsonObject().put("$push", new JsonObject()
                .put("addresses", address));
        mongoClient.updateCollection("users", query, update, res -> {
            if (res.succeeded()) {
                response.setStatusCode(200).end(Json.encode("OK"));
            } else {
                res.cause().printStackTrace();
                response.setStatusCode(500).end(res.cause().getMessage());
            }
        });
    }

    public static void clearAddresses(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");
        String userId = routingContext.request().getParam("userId");

        MongoClient mongoClient = routingContext.get("db");

        JsonObject query = new JsonObject()
                .put("_id", userId);
        JsonObject update = new JsonObject().put("$set", new JsonObject()
                .put("addresses", new JsonArray()));
        mongoClient.updateCollection("users", query, update, res -> {
            if (res.succeeded()) {
                response.setStatusCode(200).end(Json.encode("OK"));
            } else {
                res.cause().printStackTrace();
                response.setStatusCode(500).end(res.cause().getMessage());
            }
        });
    }

    public static void getUserById(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");
        String userId = routingContext.request().getParam("userId");

        MongoClient mongoClient = routingContext.get("db");

        JsonObject query = new JsonObject()
                .put("_id", userId);
        mongoClient.find("users", query, res -> {
            if (res.succeeded()) {
                UsersResponse.User user = new UsersResponse.User();
                for (JsonObject json : res.result()) {
                    user = buildUser(json);
                }
                response.setStatusCode(200).end(Json.encode(user));
            } else {
                res.cause().printStackTrace();
                response.setStatusCode(500).end(res.cause().getMessage());
            }
        });
    }

    public static void getAddresses(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");
        String userId = routingContext.request().getParam("userId");

        MongoClient mongoClient = routingContext.get("db");

        ArrayList<UsersResponse.Address> addresses = new ArrayList<>();

        JsonObject query = new JsonObject()
                .put("_id", userId);
        mongoClient.find("users", query, res -> {
            if (res.succeeded()) {
                UsersResponse.Address address;

                for (JsonObject json : res.result()) {
                    if (json.getJsonArray("addresses") != null) {
                        for (int i = 0; i < json.getJsonArray("addresses").size(); i++) {
                            address = buildAddress(json.getJsonArray("addresses").getJsonObject(i));
                            addresses.add(address);
                        }
                    }
                }

                response.setStatusCode(200).end(Json.encode(addresses));
            } else {
                res.cause().printStackTrace();
                response.setStatusCode(500).end(res.cause().getMessage());
            }
        });
    }

    private static UsersResponse.User buildUser(JsonObject userResponse) {
        UsersResponse.User user = new UsersResponse.User();
        user.id = userResponse.getString("id");
        user.uuid = userResponse.getString("uuid");
        user.name = userResponse.getString("name");
        user.email = userResponse.getString("email");
        user.email_verified = userResponse.getBoolean("email_verified");
        user.phone = userResponse.getString("phone");
        user.access = userResponse.getString("access");
        user.country_id = userResponse.getString("country_id");

        ArrayList<UsersResponse.Address> addresses = new ArrayList<>();
        ArrayList<UsersResponse.Picture> pictures = new ArrayList<>();

        if (userResponse.getJsonArray("addresses") != null) {
            for (int i = 0; i < userResponse.getJsonArray("addresses").size(); i++) {
                UsersResponse.Address address = buildAddress(userResponse.getJsonArray("addresses").getJsonObject(i));
                addresses.add(address);
            }
        }

        user.addresses = addresses;

        if (userResponse.getJsonArray("pictures") != null) {
            for (int i = 0; i < userResponse.getJsonArray("pictures").size(); i++) {
                UsersResponse.Picture picture = buildPicture(userResponse.getJsonArray("pictures").getJsonObject(i));
                pictures.add(picture);
            }
        }

        user.pictures = pictures;

        return user;
    }

    private static UsersResponse.Address buildAddress(JsonObject addressResponse) {
        UsersResponse.Address address = new UsersResponse.Address();
        address.id = addressResponse.getString("id");
        address.address = addressResponse.getString("address");
        address.floor = addressResponse.getString("floor");
        address.extra_info = addressResponse.getString("extra_info");
        address.zip_code = addressResponse.getString("zip_code");
        address.country_id = addressResponse.getString("country_id");
        address.is_default = addressResponse.getBoolean("is_default");
        return address;
    }

    private static UsersResponse.Picture buildPicture(JsonObject pictureResponse) {
        UsersResponse.Picture picture = new UsersResponse.Picture();
        picture.id = pictureResponse.getString("id");
        picture.src = pictureResponse.getString("src");
        return picture;
    }
}
