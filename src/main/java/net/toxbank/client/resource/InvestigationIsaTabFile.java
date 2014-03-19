/**
 * Copyright 2014 Leadscope, Inc. All rights reserved.
 * LEADSCOPE PROPRIETARY and CONFIDENTIAL. Use is subject to license terms.
 */
package net.toxbank.client.resource;

/**
 * An ISA-Tab file entry in an investigation
 */
public class InvestigationIsaTabFile {  
  private String typeUri;
  private String type;
  private String filename;
  private String downloadUri;

  public InvestigationIsaTabFile(String typeUri, String filename, String downloadUri) {
    this.typeUri = typeUri;
    this.filename = filename;
    this.downloadUri = downloadUri;
    
    type = typeUri.substring(typeUri.lastIndexOf('/')+1);
    String[] typeTerms = type.split("_");
    StringBuilder sb = new StringBuilder();
    for (String typeTerm : typeTerms) {
      String mappedTerm = mapTypeTerm(typeTerm);
      if (mappedTerm != null) {
        if (sb.length() > 0) {
          sb.append(" ");
        }
        sb.append(mappedTerm);
      }
    }
    type = sb.toString();
  }
    
  public String getTypeUri() {
    return typeUri;
  }

  public String getType() {
    return type;
  }

  public String getFilename() {
    return filename;
  }

  public String getDownloadUri() {
    return downloadUri;
  }
  
  private String mapTypeTerm(String s) {
    if ("ms".equals(s)) {
      return "Mass";
    }
    else if ("spec".equals(s)) {
      return "Spectrometry";
    }
    else {
      if (s == null) {
        return "";
      }
      s = s.trim();
      if (s.length() == 0) {
        return null;
      }
      else {
        return s.substring(0,1).toUpperCase() + s.substring(1);
      }
    }
  }
}
