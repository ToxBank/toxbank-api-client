/**
 * Copyright 2014 Leadscope, Inc. All rights reserved.
 * LEADSCOPE PROPRIETARY and CONFIDENTIAL. Use is subject to license terms.
 */
package net.toxbank.client.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a series of investigation samples for a specific bio sample
 */
public class InvestigationBioSample {
  public static List<String> cellNames = Arrays.asList("cell", "organ", "tissue", "cell line");
  private InvestigationBioSamples allBioSamples;
  private String uri;
  private String organism;
  private String sex;
  private String cell;
  private Map<String, InvestigationCompoundSample> compoundSamplesByUri = new HashMap<String, InvestigationCompoundSample>();
  private List<InvestigationCompoundSample> compoundSamples = new ArrayList<InvestigationCompoundSample>();

  public InvestigationBioSample(InvestigationBioSamples allBioSamples, String uri) {
    this.allBioSamples = allBioSamples;
    this.uri = uri;
  }

  public void addSample(
      String compoundUri,
      String compoundName,
      String sampleUri, 
      Float doseValue,
      String doseUnits,
      Float timeValue,
      String timeUnits) {
    InvestigationCompoundSample compoundSample = compoundSamplesByUri.get(compoundUri);
    if (compoundSample == null) {
      compoundSample = new InvestigationCompoundSample(this, compoundUri, compoundName);
      compoundSamplesByUri.put(compoundUri, compoundSample);
      compoundSamples.add(compoundSample);
    }
    compoundSample.addSample(sampleUri, doseValue, doseUnits, timeValue, timeUnits);
  }
  
  public static boolean isCellCharacteristic(AdjunctInvestigationDatum c) {
    for (String name : cellNames) {
      if (name.equalsIgnoreCase(c.getName())) {
        return true;
      }
    }
    return false;
  }
  
  public void addCharacteristic(AdjunctInvestigationDatum c) {
    if (isCellCharacteristic(c)) {
      if (c.getValue() != null) {
        if (cell == null) {
          cell = c.getValue();
        }
        else if (!cell.toLowerCase().contains(c.getValue().toLowerCase())) {
          cell += " " + c.getValue();
        }
      }
    }
    else if ("organism".equalsIgnoreCase(c.getName())) {
      organism = c.getValue();
    }
    else if ("sex".equalsIgnoreCase(c.getName())) {
      if (sex == null) {
        sex = c.getValue();
      }
      else if (!sex.toLowerCase().contains(c.getValue().toLowerCase())) {
        sex += " " + c.getValue();
      }
    }
    else if ("sample timepointunit".equalsIgnoreCase(c.getName())) {
      for (InvestigationCompoundSample compoundSample : compoundSamples) {
        compoundSample.updateTimeUnits(c.getValue());
      }
    }
  }
   
  public InvestigationBioSamples getAllBioSamples() {
    return allBioSamples;
  }
  
  public String getId() {
    if (uri == null) {
      return null;
    }
    return uri.substring(uri.lastIndexOf('/')+1);
  }
  
  public String getUri() {
    return uri;
  }

  public String getOrganism() {
    return organism;
  }

  public String getSex() {
    return sex;
  }
  
  public String getCell() {
    return cell;
  }

  public List<InvestigationCompoundSample> getCompoundSamples() {
    return compoundSamples;
  }
  
  public List<InvestigationSample> getInvestigationSamples() {
    List<InvestigationSample> samples = new ArrayList<InvestigationSample>();
    for (InvestigationCompoundSample compoundSample : compoundSamples) {
      samples.addAll(compoundSample.getSamples());
    }
    return samples;
  }
  
  public int getSampleCount() {
    int count = 0;
    for (InvestigationCompoundSample sample : compoundSamples) {
      count += sample.getSamples().size();
    }
    return count;
  }
}
