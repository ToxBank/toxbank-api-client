/**
 * Copyright 2013 Leadscope, Inc. All rights reserved.
 * LEADSCOPE PROPRIETARY and CONFIDENTIAL. Use is subject to license terms.
 */
package net.toxbank.client.resource;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of information that can be obtained for an investigation, but is
 * obtained separately from the investigation meta data 
 */
public class AdjunctInvestigationInfo {
  // factors
  public static final String DOSE_FACTOR = "dose";
  public static final String COMPOUND_FACTOR = "compound";
  public static final String SAMPLE_TIMEPOINT = "sample TimePoint";
  
  // details
  public static final String ENDPOINT = "endpoint";
  public static final String TECHNOLOGY = "technology";
  
  private String id;
  private List<Datum> details = new ArrayList<Datum>();
  private List<Datum> factors = new ArrayList<Datum>();
  private List<Datum> characteristics = new ArrayList<Datum>();
  
  public AdjunctInvestigationInfo(String id) {
    this.id = id;
  }
  
  public String getId() {
    return id;
  }
  
  public void addFactor(Datum factor) {
    if (!factors.contains(factor)) {
      factors.add(factor);
    }
  }
  
  public List<Datum> getFactors() {
    return factors;
  }
  
  public List<Datum> getFactorsByName(String name) {
    List<Datum> namedFactors = new ArrayList<Datum>();
    for (Datum factor : factors) {
      if (factor.getName().equals(name)) {
        namedFactors.add(factor);
      }
    }
    return namedFactors;
  }

  public void addCharacteristic(Datum characteristic) {
    if (!characteristics.contains(characteristic)) {
      characteristics.add(characteristic);
    }
  }
  
  public List<Datum> getCharacteristics() {
    return characteristics;
  }
  
  public List<Datum> getCharacteristicsByName(String name) {
    List<Datum> namedCharacteristics = new ArrayList<Datum>();
    for (Datum characteristic : characteristics) {
      if (characteristic.getName().equals(name)) {
        namedCharacteristics.add(characteristic);
      }
    }
    return namedCharacteristics;
  }

  public void addDetail(Datum detail) {
    if (!details.contains(detail)) {
      details.add(detail);
    }
  }
  
  public List<Datum> getDetails() {
    return details;
  }
  
  public List<Datum> getDetailsByName(String name) {
    List<Datum> namedDetails = new ArrayList<Datum>();
    for (Datum detail : details) {
      if (detail.getName().equals(name)) {
        namedDetails.add(detail);
      }
    }
    return namedDetails;
  }
  
  public static class Datum {
    private String name;
    private String value;    
    private String uri;
        
    public Datum(String name, String value, String uri) {
      this.name = name;
      this.value = value;
      this.uri = uri;
    }
    
    public String getName() {
      return name;
    }
    public String getValue() {
      return value;
    }
    public String getUri() {
      return uri;
    }    
    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((uri == null) ? 0 : uri.hashCode());
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      return result;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Datum other = (Datum) obj;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      if (uri == null) {
        if (other.uri != null)
          return false;
      } else if (!uri.equals(other.uri))
        return false;
      if (value == null) {
        if (other.value != null)
          return false;
      } else if (!value.equals(other.value))
        return false;
      return true;
    }
  }
}
