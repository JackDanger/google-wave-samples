package robot;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class UserInfo {

    @PrimaryKey
    @Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private String id = null;

    @Persistent
    private String name = null;

    @Persistent
    private String authSubToken = null;

    @Persistent
    Date lastUpdated = null;

    public UserInfo(String name, String authSubToken) {
      this.name = name;
      this.authSubToken = authSubToken;
      this.lastUpdated = new Date();
    }

    public String getId() {
      return id;
    }

    public Date getLastUpdated() {
      return lastUpdated;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public void setAuthSubToken(String authSubToken) {
      this.authSubToken = authSubToken;
      this.lastUpdated = new Date();
    }

    public String getAuthSubToken() {
      return authSubToken;
    }

}
