/**
 * Copyright 2014 Leadscope, Inc. All rights reserved.
 * LEADSCOPE PROPRIETARY and CONFIDENTIAL. Use is subject to license terms.
 */
package net.toxbank.client.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The list of all bio samples tested in an investigation
 */
public class InvestigationBioSamples {
  private AdjunctInvestigationInfo info;
  private Map<String, InvestigationBioSample> bioSamplesByUri = new HashMap<String, InvestigationBioSample>();
  private List<InvestigationBioSample> bioSamples = new ArrayList<InvestigationBioSample>();
  
  public InvestigationBioSamples(AdjunctInvestigationInfo info) {
    this.info = info;
  }
  
  public void addFactor(
      String bioSampleUri,
      String compoundUri,
      String compoundName,
      String sampleUri, 
      Float doseValue,
      String doseUnits,
      Float timeValue,
      String timeUnits) {
    if (bioSampleUri == null) {
      throw new IllegalArgumentException("No bio sample uri was found");
    }
    InvestigationBioSample bioSample = bioSamplesByUri.get(bioSampleUri);
    if (bioSample == null) {
      bioSample = new InvestigationBioSample(this, bioSampleUri);
      bioSamplesByUri.put(bioSampleUri, bioSample);
      bioSamples.add(bioSample);
    }
    bioSample.addSample(compoundUri, compoundName, sampleUri, doseValue, doseUnits, timeValue, timeUnits);
  }
  
  public AdjunctInvestigationInfo getInfo() {
    return info;
  }
  
  public List<InvestigationBioSample> getBioSamples() {
    return bioSamples;
  }  
  
  public List<InvestigationSample> getInvestigationSamples() {
    List<InvestigationSample> samples = new ArrayList<InvestigationSample>();
    for (InvestigationBioSample bioSample : bioSamples) {
      samples.addAll(bioSample.getInvestigationSamples());
    }
    return samples;
  }
  
  public int getSampleCount() {
    int count = 0;
    for (InvestigationBioSample sample : bioSamples) {
      count += sample.getSampleCount();
    }
    return count;
  }
}