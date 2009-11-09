package com.google.wave.extensions.tweety.util;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

/**
 * Helper class that contains a factory method to get an instance of
 * {@link PersistenceManager}, that is used to fetch and persist object from and
 * to Google App Engine data store.
 *
 * @author mprasetya@google.com (Marcel Prasetya)
 */
public final class PersistenceManagerHelper {

  /**
   * A factory of {@link PersistenceManager}. It is expensive to create this
   * factory, so we are instantiating a static copy once, and reuse this.
   */
  private static final PersistenceManagerFactory pmfInstance =
      JDOHelper.getPersistenceManagerFactory("transactions-optional");

  /**
   * Returns a {@link PersistenceManager}.
   *
   * @return A {@link PersistenceManager} that can be used to save and query
   *     object.
   */
  public static PersistenceManager getPersistenceManager() {
    return pmfInstance.getPersistenceManager();
  }
}
