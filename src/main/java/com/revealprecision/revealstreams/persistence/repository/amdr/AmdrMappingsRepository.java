package com.revealprecision.revealstreams.persistence.repository.amdr;

import com.revealprecision.revealstreams.persistence.domain.amdr.AmdrMappings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmdrMappingsRepository extends JpaRepository<AmdrMappings, Integer> {

}
