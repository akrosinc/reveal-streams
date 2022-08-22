package com.revealprecision.revealstreams.persistence.repository;

import com.revealprecision.revealstreams.persistence.domain.LookupEntityType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LookupEntityTypeRepository extends JpaRepository<LookupEntityType, UUID> {


  Optional<LookupEntityType> findLookupEntityTypeByCode(String code);
}