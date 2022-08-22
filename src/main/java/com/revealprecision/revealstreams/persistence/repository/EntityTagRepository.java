package com.revealprecision.revealstreams.persistence.repository;

import com.revealprecision.revealstreams.persistence.domain.EntityTag;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityTagRepository extends JpaRepository<EntityTag, UUID> {
  Optional<EntityTag> getFirstByTag(String tag);

  Optional<EntityTag> findEntityTagsByTagAndLookupEntityType_Code(String tagName,
      String actionCode);

}
