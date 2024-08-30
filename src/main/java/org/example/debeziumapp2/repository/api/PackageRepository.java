package org.example.debeziumapp2.repository.api;

import org.example.debeziumapp2.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {


}
