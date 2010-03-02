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
  private String waveId = null;

  @Persistent
  private String waveletId = null;

  @Persistent
  private String blipId = null;

  @Persistent
  private String text = null;

  @Persistent
  Date lastUpdated = null;

  public BlipEntry(Blip blip) {
    this(blip.getWaveId().getId(), blip.getWaveletId().getId(), blip.getBlipId());
  }

  public BlipEntry(String waveId, String waveletId, String blipId) {
    this.waveId = waveId;
    this.waveletId = waveletId;
    this.blipId = blipId;
    this.lastUpdated = new Date();
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

  public String getBlipId() {
    return blipId;
  }

  public String getText() {
    return text;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setText(String text) {
    this.text = text;
    lastUpdated = new Date();
  }

}
