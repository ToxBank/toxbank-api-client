/**
 * Copyright 2013 Leadscope, Inc. All rights reserved.
 * LEADSCOPE PROPRIETARY and CONFIDENTIAL. Use is subject to license terms.
 */
package net.toxbank.client.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * A set of information that can be obtained for an investigation, but is
 * obtained separately from the investigation meta data 
 */
public class AdjunctInvestigationInfo {
  private String url;
  private String id;
  private InvestigationBioSamples bioSamples = new InvestigationBioSamples(this);
  private List<AdjunctInvestigationDatum> characteristics = new ArrayList<AdjunctInvestigationDatum>();
  private List<AdjunctInvestigationDatum> details = new ArrayList<AdjunctInvestigationDatum>();
  
  public AdjunctInvestigationInfo(String url, String id) {
    this.url = url;
    this.id = id;
  }
  
  public String getUrl() {
    return url;
  }
  
  public String getId() {
    return id;
  }
  
  public InvestigationBioSamples getBioSamples() {
    return bioSamples;
  }
  
  public void addCharacteristic(AdjunctInvestigationDatum characteristic) {
    if (!characteristics.contains(characteristic)) {
      characteristics.add(characteristic);
    }
  }
  
  public List<AdjunctInvestigationDatum> getCharacteristics() {
    return characteristics;
  }
  
  public List<AdjunctInvestigationDatum> getCharacteristicsByName(String name) {
    return getCharacteristicsByName(Arrays.asList(name));
  }

  public List<AdjunctInvestigationDatum> getCharacteristicsByName(List<String> names) {
    List<AdjunctInvestigationDatum> namedCharacteristics = new ArrayList<AdjunctInvestigationDatum>();
    for (String name : names) {
      for (AdjunctInvestigationDatum characteristic : characteristics) {
        if (characteristic.getName().equalsIgnoreCase(name)) {
          namedCharacteristics.add(characteristic);
        }
      }
    }
    return namedCharacteristics;
  }

  public void addDetail(AdjunctInvestigationDatum detail) {
    if (!details.contains(detail)) {
      details.add(detail);
    }
  }
  
  public List<AdjunctInvestigationDatum> getDetails() {
    return details;
  }
  
  public List<AdjunctInvestigationDatum> getDetailsByName(String name) {
    return getDetailsByName(Arrays.asList(name));
  }

  public List<AdjunctInvestigationDatum> getDetailsByName(List<String> names) {
    List<AdjunctInvestigationDatum> namedDetails = new ArrayList<AdjunctInvestigationDatum>();
    for (String name : names) {
      for (AdjunctInvestigationDatum detail : details) {
        if (detail.getName().equalsIgnoreCase(name)) {
          namedDetails.add(detail);
        }
      }
    }
    return namedDetails;
  }  
  
  public List<InvestigationSample> getInvestigationSamples() {
    return bioSamples.getInvestigationSamples();
  }
  
  public int getSampleCount() {
    return bioSamples.getSampleCount();
  }
}
