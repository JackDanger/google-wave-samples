package robot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Util {
  private final Logger log = Logger.getLogger(Util.class.getName());
  
  private PersistenceManagerFactory pmf = null;
  
  @Inject
  public Util(PersistenceManagerFactory pmf) {
    this.pmf = pmf;
  }
  
  public PersistenceManager getPm() {
    return pmf.getPersistenceManager();
  }


  public void persistUserInfo(UserInfo userInfo) {
    PersistenceManager pm = pmf.getPersistenceManager();
    try {
      pm.makePersistent(userInfo);
    } finally {
      pm.close();
    }
  }

  public Object persistJdo(Object entry) {
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      entry = pm.makePersistent(entry);
      entry = pm.detachCopy(entry);
    } finally {
      pm.close();
    }

    return entry;
  }

  public void removeJdo(Object entry) {
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      pm.deletePersistent(entry);
    } finally {
      pm.close();
    }
  }

  @SuppressWarnings("unchecked")
  public UserInfo getUserInfo(String name) {
    UserInfo userInfo = null;
    PersistenceManager pm = pmf.getPersistenceManager();
    try {
      String filters = "name == name_";
      Query query = pm.newQuery(UserInfo.class, filters);
      query.declareParameters("String name_");
      List<UserInfo> list = (List<UserInfo>) query.executeWithArray(new Object[] {name});
      if (list.size() > 0) {
        userInfo = list.get(0);
        //entry = pm.detachCopy(entry);
      }
    } finally {
      pm.close();
    }
    return userInfo;
  }

  @SuppressWarnings("unchecked")
  public IssueJdoEntry getIssueJdoEntry(String waveId) {
    IssueJdoEntry entry = null;
    PersistenceManager pm = pmf.getPersistenceManager();
    try {
      String filters = "waveId == waveId_";
      Query query = pm.newQuery(IssueJdoEntry.class, filters);
      query.declareParameters("String waveId_");
      List<IssueJdoEntry> list =
          (List<IssueJdoEntry>) query.executeWithArray(new Object[] {waveId});
      if (list.size() > 0) {
        entry = list.get(0);
        entry = pm.detachCopy(entry);
      }
    } finally {
      pm.close();
    }
    return entry;
  }

	public String fetchUrl(String url) throws Exception {
		HttpURLConnection connection = (HttpURLConnection) new URL(url)
				.openConnection();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		StringBuilder result = new StringBuilder();
		try {
			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
				result.append(inputLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result.toString();
	}

	public static String getSelfUrl(HttpServletRequest request) {
		StringBuffer url = new StringBuffer();

		url.append(request.getRequestURL());
		String queryString = request.getQueryString();
		if (queryString != null && queryString.length() > 0) {
			url.append("?");
			url.append(queryString);
		}

		return url.toString();
	}
  public String getPostBody(HttpServletRequest req) throws IOException {
    InputStream is = req.getInputStream();

    StringBuffer body = new StringBuffer();
    String line = null;
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    while ((line = br.readLine()) != null) {
      body.append(line);
      body.append("\n");
    }
    return body.toString();
  }
}
