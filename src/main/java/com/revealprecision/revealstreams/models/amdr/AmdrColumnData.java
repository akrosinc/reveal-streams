package com.revealprecision.revealstreams.models.amdr;

import com.revealprecision.revealstreams.models.ColumnData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString
public class AmdrColumnData extends ColumnData {

  private String amdrParent;

}
