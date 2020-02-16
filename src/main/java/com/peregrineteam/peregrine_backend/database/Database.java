package com.peregrineteam.peregrine_backend.database;

import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.sqlclient.PoolOptions;

public class Database {
    public static MongoClient initializeDb(Vertx vertx) {
        JsonObject config = new JsonObject();
        config.put("connection_string", "mongodb+srv://peregrine:Ines123@peregrinedb-bl4av.mongodb.net/peregrine");
        config.put("mongo_name", "peregrine");

        return MongoClient.createShared(vertx, config);
    }
}
