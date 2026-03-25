package com.revealprecision.revealstreams.persistence.repository.amdr;

import com.revealprecision.revealstreams.persistence.domain.amdr.AmdrHeaderNames;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmdrHeaderNamesRepository extends JpaRepository<AmdrHeaderNames, String> {

  List<AmdrHeaderNames> findAllByColType(String colType);
}
