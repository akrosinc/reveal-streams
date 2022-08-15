package com.revealprecision.revealstreams.models;

import com.revealprecision.revealstreams.enums.ReportTypeEnum;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableRow implements Serializable {
  private UUID parentLocationIdentifier;
  private ReportTypeEnum reportTypeEnum;
  private UUID planIdentifier;
  private List<RowData> rowData;
}
