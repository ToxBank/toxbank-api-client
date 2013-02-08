package net.toxbank.client.resource;

import java.net.URL;

/**
 * A url with an associated timestamp of when the specified resource has
 * last been updated. Can be used as a hash key.
 */
public class TimestampedUrl {
  private URL url;
  private long timestamp;
  
  public TimestampedUrl(URL url, long timestamp) {
    this.url = url;
    this.timestamp = timestamp;
  }
  
  public URL getUrl() {
    return url;
  }
  
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
    result = prime * result + ((url == null) ? 0 : url.hashCode());
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
    TimestampedUrl other = (TimestampedUrl) obj;
    if (timestamp != other.timestamp)
      return false;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    return true;
  }  
}
