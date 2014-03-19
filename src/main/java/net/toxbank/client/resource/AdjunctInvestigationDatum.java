/**
 * Copyright 2014 Leadscope, Inc. All rights reserved.
 * LEADSCOPE PROPRIETARY and CONFIDENTIAL. Use is subject to license terms.
 */
package net.toxbank.client.resource;

public class AdjunctInvestigationDatum {
  private String name;
  private String value;    
  private String uri;
  private String sampleUri;
  private String bioSampleUri;
  private String units;

  public AdjunctInvestigationDatum(String sampleUri, String bioSampleUri, String name, String value, String uri, String units) {
    this.sampleUri = sampleUri;
    this.bioSampleUri = bioSampleUri;
    this.name = name;
    this.value = value;
    this.uri = uri;
    this.units = units;
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
  public String getSampleUri() {
    return sampleUri;
  }
  public String getBioSampleUri() {
    return bioSampleUri;
  }  
  public String getUnits() {
    return units;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((bioSampleUri == null) ? 0 : bioSampleUri.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((sampleUri == null) ? 0 : sampleUri.hashCode());
    result = prime * result + ((units == null) ? 0 : units.hashCode());
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
    AdjunctInvestigationDatum other = (AdjunctInvestigationDatum) obj;
    if (bioSampleUri == null) {
      if (other.bioSampleUri != null)
        return false;
    } else if (!bioSampleUri.equals(other.bioSampleUri))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (sampleUri == null) {
      if (other.sampleUri != null)
        return false;
    } else if (!sampleUri.equals(other.sampleUri))
      return false;
    if (units == null) {
      if (other.units != null)
        return false;
    } else if (!units.equals(other.units))
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
