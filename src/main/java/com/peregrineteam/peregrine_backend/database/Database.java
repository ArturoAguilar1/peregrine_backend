package com.peregrineteam.peregrine_backend.database;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public class Database {
    public static PgPool initializeDb(Vertx vertx) {
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(5432)
                .setHost("peregrine-db-demo.cdk0dshceixr.us-east-2.rds.amazonaws.com")
                .setDatabase("peregrine")
                .setUser("postgres")
                .setPassword("Ines1234");

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

        return PgPool.pool(vertx, connectOptions, poolOptions);
    }
}
