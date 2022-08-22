package com.revealprecision.revealstreams.persistence.repository;

import com.revealprecision.revealstreams.persistence.domain.metadata.PersonMetadata;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonMetadataRepository extends JpaRepository<PersonMetadata, UUID> {


  Optional<PersonMetadata> findPersonMetadataByPerson_Identifier(UUID personIdentifier);

}
