package com.revealprecision.revealstreams.persistence.repository;


import com.revealprecision.revealstreams.persistence.domain.Location;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

  List<Location> getLocationsByPeople_Identifier(UUID personIdentifier);
}
