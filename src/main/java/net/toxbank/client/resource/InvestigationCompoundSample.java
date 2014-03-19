/**
 * Copyright 2014 Leadscope, Inc. All rights reserved.
 * LEADSCOPE PROPRIETARY and CONFIDENTIAL. Use is subject to license terms.
 */
package net.toxbank.client.resource;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * List of investigation samples for a compound
 */
public class InvestigationCompoundSample {
  private InvestigationBioSample bioSample;
  private String compoundUri;
  private String compoundName;  
  private SortedSet<InvestigationSample> samples = new TreeSet<InvestigationSample>();
  
  public InvestigationCompoundSample(InvestigationBioSample bioSample, String compoundUri, String compoundName) {
    this.bioSample = bioSample;
    this.compoundUri = compoundUri;
    this.compoundName = compoundName;
    if (compoundUri == null) {
      compoundUri = compoundName;
    }
    if (compoundName == null) {
      compoundName = compoundUri;
    }
  }
  
  public void addSample(
      String sampleUri, 
      Float doseValue,
      String doseUnits,
      Float timeValue,
      String timeUnits) {
    InvestigationSample sample = new InvestigationSample(
        this, sampleUri, doseValue, doseUnits, timeValue, timeUnits);
    samples.add(sample);
  }

  public void updateTimeUnits(String timeUnits) {
    boolean updated = false;
    for (InvestigationSample sample : samples) {
      if (sample.updateTimeUnits(timeUnits)) {
        updated = true;
      }
    }
    if (updated) {
      SortedSet<InvestigationSample> updatedSamples = new TreeSet<InvestigationSample>();
      updatedSamples.addAll(samples);
      samples = updatedSamples;
    }
  }
    
  public InvestigationBioSample getBioSample() {
    return bioSample;
  }

  public String getCompoundUri() {
    return compoundUri;
  }

  public String getCompoundName() {
    return compoundName;
  }

  public SortedSet<InvestigationSample> getSamples() {
    return samples;
  }  
}
