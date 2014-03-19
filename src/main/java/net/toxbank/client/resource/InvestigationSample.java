/**
 * Copyright 2014 Leadscope, Inc. All rights reserved.
 * LEADSCOPE PROPRIETARY and CONFIDENTIAL. Use is subject to license terms.
 */
package net.toxbank.client.resource;


/**
 * A sample in an investigation
 */
public class InvestigationSample implements Comparable<InvestigationSample> {
  private InvestigationCompoundSample compoundSample;
  private String uri;
  private Float doseValue;
  private String doseUnits;
  private Float timeValue;
  private String timeUnits;
  
  public InvestigationSample(
      InvestigationCompoundSample compoundSample,
      String uri,
      Float doseValue,
      String doseUnits,
      Float timeValue,
      String timeUnits) {
    this.compoundSample = compoundSample;
    this.uri = uri;
    this.doseValue = doseValue;
    this.doseUnits = doseUnits;
    this.timeValue = timeValue;
    this.timeUnits = timeUnits;
  }
  
  public InvestigationCompoundSample getCompoundSample() {
    return compoundSample;
  }

  public InvestigationBioSample getBioSample() {
    return compoundSample.getBioSample();
  }
  
  public InvestigationBioSamples getAllBioSamples() {
    return getBioSample().getAllBioSamples();
  }
  
  public AdjunctInvestigationInfo getInfo() {
    return getAllBioSamples().getInfo();
  }
  
  public String getUri() {
    return uri;
  }

  public Float getDoseValue() {
    return doseValue;
  }

  public String getDoseUnits() {
    return doseUnits;
  }

  public Float getTimeValue() {
    return timeValue;
  }

  public String getTimeUnits() {
    return timeUnits;
  }

  public boolean updateTimeUnits(String timeUnits) {
    if (this.timeUnits == null) {
      this.timeUnits = timeUnits;
      return true;
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((doseUnits == null) ? 0 : doseUnits.hashCode());
    result = prime * result + ((doseValue == null) ? 0 : doseValue.hashCode());
    result = prime * result + ((timeUnits == null) ? 0 : timeUnits.hashCode());
    result = prime * result + ((timeValue == null) ? 0 : timeValue.hashCode());
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
    InvestigationSample other = (InvestigationSample) obj;
    if (doseUnits == null) {
      if (other.doseUnits != null)
        return false;
    } else if (!doseUnits.equals(other.doseUnits))
      return false;
    if (doseValue == null) {
      if (other.doseValue != null)
        return false;
    } else if (!doseValue.equals(other.doseValue))
      return false;
    if (timeUnits == null) {
      if (other.timeUnits != null)
        return false;
    } else if (!timeUnits.equals(other.timeUnits))
      return false;
    if (timeValue == null) {
      if (other.timeValue != null)
        return false;
    } else if (!timeValue.equals(other.timeValue))
      return false;
    return true;
  }  
  
  private int compareQuantity(Float value1, String units1, Float value2, String units2) {
    if (units1 == null) {
      if (units2 != null) {
        return -1;
      }
    }
    else if (units2 == null) {
      return 1;
    }
    
    if (units1 != null && units2 != null) {
      int result = units1.compareToIgnoreCase(units2);
      if (result != 0) {
        return result;
      }
    }
    
    if (value1 == null || value1.isNaN()) {
      if (value2 != null && !value2.isNaN()) {
        return -1;
      }
      else {
        return 0;
      }
    }
    else if (value2 == null || value2.isNaN()) {
      return 1;
    }
    
    return value1.compareTo(value2);
  }
    
  @Override
  public int compareTo(InvestigationSample o2) {
    int result = compareQuantity(this.doseValue, this.doseUnits, o2.doseValue, o2.doseUnits);
    if (result != 0) {
      return result;
    }
    return compareQuantity(this.timeValue, this.timeUnits, o2.timeValue, o2.timeUnits);
  }
}
