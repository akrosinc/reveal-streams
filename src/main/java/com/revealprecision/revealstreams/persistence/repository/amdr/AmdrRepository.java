package com.revealprecision.revealstreams.persistence.repository.amdr;

import com.revealprecision.revealstreams.persistence.domain.amdr.AmdrData;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmdrRepository extends JpaRepository<AmdrData, UUID> {

  List<AmdrData> findByTypeAndLocationIdIn(String type,List<UUID> locationIds);

  List<AmdrData> findByTypeAndLocationIdInAndCollectionYear(String type,List<UUID> locationIds, String collectionYear);
  AmdrData findByTypeAndLocationId(String type,UUID locationId);

  AmdrData findByTypeAndLocationIdAndCollectionYear(String type,UUID locationId, String collectionYear);

  List<AmdrData> findByLocationIdInAndCollectionYear(List<UUID> locationIds, String collectionYear);

  List<AmdrData> findByLocationIdIn(List<UUID> locationIds);





}
