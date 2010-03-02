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
public class IssueJdoEntry {

  @PrimaryKey
  @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private String id = null;
  @Persistent
  private String projectName = null;
  @Persistent
  private int issueId = -1;
  @Persistent
  private String domain = null;
  @Persistent
  private String waveId = null;
  @Persistent
  private String waveletId = null;
  @Persistent
  private String blipId = null;
  @Persistent
  Date lastUpdated = null;
  @Persistent
  int localCommentsCount = 0;

  public IssueJdoEntry(String domain, Blip blip, String projectName, int issueId) {
    waveId = blip.getWaveId().getId();
    waveletId = blip.getWaveletId().getId();
    blipId = blip.getBlipId();
    this.projectName = projectName;
    this.issueId = issueId;
    this.lastUpdated = new Date();
    this.domain = domain;
    localCommentsCount = 0;
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

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getProjectName() {
    return projectName;
  }

  public int getIssueId() {
    return issueId;
  }

  public int getLocalCommentsCount() {
    return localCommentsCount;
  }

  public void setLocalCommentsCount(int localCommentsCount) {
    this.localCommentsCount = localCommentsCount;
  }

  public String getDomain() {
    return domain;
  }
}
