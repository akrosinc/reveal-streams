package com.revealprecision.revealstreams.persistence.repository.logging;

import com.revealprecision.revealstreams.persistence.domain.logging.HttpLogging;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HttpLoggingRepository extends JpaRepository<HttpLogging, UUID> {
  int deleteAllByRequestTimeBefore(LocalDateTime time);
}
