/**
 * Copyright 2015 Leadscope, Inc. All rights reserved.
 * LEADSCOPE PROPRIETARY and CONFIDENTIAL. Use is subject to license terms.
 */
package net.toxbank.client.resource;

/**
 * A dose-response entry for a gene
 */
public class GeneDoseResponse {
  private String gene;
  private String investigationUri;
  private String investigationTitle;
  private String dataTransformationName;
  private String compoundName;
  private String sampleName;
  private String cell;
  private double doseValue = Double.NaN;
  private String doseUnits;
  private double timeValue = Double.NaN;
  private String timeUnits;
  private double foldChange = Double.NaN;
  private double pValue = Double.NaN;
  private double qValue = Double.NaN;
  
  public String getGene() {
    return gene;
  }
  public void setGene(String gene) {
    this.gene = gene;
  }
  public String getInvestigationUri() {
    return investigationUri;
  }
  public void setInvestigationUri(String investigationUri) {
    this.investigationUri = investigationUri;
  }
  public String getInvestigationTitle() {
    return investigationTitle;
  }
  public void setInvestigationTitle(String investigationTitle) {
    this.investigationTitle = investigationTitle;
  }
  public String getDataTransformationName() {
    return dataTransformationName;
  }
  public void setDataTransformationName(String dataTransformationName) {
    this.dataTransformationName = dataTransformationName;
  }
  public String getCompoundName() {
    return compoundName;
  }
  public void setCompoundName(String compoundName) {
    this.compoundName = compoundName;
  }
  public String getSampleName() {
    return sampleName;
  }
  public void setSampleName(String sampleName) {
    this.sampleName = sampleName;
  }
  public double getDoseValue() {
    return doseValue;
  }
  public void setDoseValue(double doseValue) {
    this.doseValue = doseValue;
  }
  public String getDoseUnits() {
    return doseUnits;
  }
  public void setDoseUnits(String doseUnits) {
    this.doseUnits = doseUnits;
  }
  public double getTimeValue() {
    return timeValue;
  }
  public void setTimeValue(double timeValue) {
    this.timeValue = timeValue;
  }
  public String getTimeUnits() {
    return timeUnits;
  }
  public void setTimeUnits(String timeUnits) {
    this.timeUnits = timeUnits;
  }
  public double getFoldChange() {
    return foldChange;
  }
  public void setFoldChange(double foldChange) {
    this.foldChange = foldChange;
  }
  public double getpValue() {
    return pValue;
  }
  public void setpValue(double pValue) {
    this.pValue = pValue;
  }
  public double getqValue() {
    return qValue;
  }
  public void setqValue(double qValue) {
    this.qValue = qValue;
  }
  public String getCell() {
    return cell;
  }
  public void setCell(String cell) {
    this.cell = cell;
  }
}
