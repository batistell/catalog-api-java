package com.batistell.catalogapi.repository;

import com.batistell.catalogapi.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogRepository extends MongoRepository<Product, String> {
}
