package com.batistell.catalogapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

/**
 * Registers a MongoTransactionManager so that @Transactional is backed
 * by real MongoDB multi-document transactions.
 *
 * ⚠️  IMPORTANT: MongoDB multi-document transactions require the database
 * to be running as a REPLICA SET (even a single-node replica set works).
 *
 * For local Docker development, update your docker-compose.yml to add:
 *   command: ["--replSet", "rs0", "--bind_ip_all"]
 * Then initialise with:
 *   docker exec -it catalog_mongodb mongosh --eval "rs.initiate()"
 *
 * Without a replica set, @Transactional methods still run but rollback
 * will NOT work — MongoDB will throw an error on commit.
 */
@Configuration
public class MongoConfig {

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}
