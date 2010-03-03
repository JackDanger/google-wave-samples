package robot;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.wave.api.Blip;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class BlipEntry {

  @PrimaryKey
  @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private String id = null;
  @Persistent
  private String domain = null;
  @Persistent
  private String waveId = null;
  @Persistent
  private String waveletId = null;
  @Persistent
  private String blipId = null;
  @Persistent
  private Date lastUpdated = null;
  @Persistent
  private Date created = null;

  public BlipEntry(String domain, Blip blip) {
    waveId = blip.getWaveId().getId();
    waveletId = blip.getWaveletId().getId();
    blipId = blip.getBlipId();
    this.lastUpdated = new Date();
    this.created = new Date();
    this.domain = domain;
  }

  public String getId() {
    return id;
  }

  public String getWaveId() {
    return waveId;
  }

  public String getWaveletId() {
    return waveletId;
  }

  public String getDomain() {
    return domain;
  }

  public String getBlipId() {
    return blipId;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public Date getCreated() {
    return created;
  }
}
