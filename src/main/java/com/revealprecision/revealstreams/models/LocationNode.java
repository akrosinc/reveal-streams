package com.revealprecision.revealstreams.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class LocationNode implements Serializable {

  UUID id;
  UUID parentId;
  String name;
  String geoLevel;
  List<LocationNode> children;

  public LocationNode(UUID id, UUID parentId, String name, String geoLevel) {
    this.id = id;
    this.parentId = parentId;
    this.name = name;
    this.geoLevel = geoLevel;
    this.children = new ArrayList<>(2); // small default
  }

  public void addChild(LocationNode child) {
    children.add(child);
  }

}
