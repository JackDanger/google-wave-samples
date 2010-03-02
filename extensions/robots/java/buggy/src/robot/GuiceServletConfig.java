package robot;

import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class GuiceServletConfig extends GuiceServletContextListener {
  private static final Logger log = Logger.getLogger(GuiceServletConfig.class.getName());

  @Override
  protected Injector getInjector() {
    ServletModule servletModule = new ServletModule() {
      @Override
      protected void configureServlets() {
        serveRegex("\\/_wave/.*").with(Buggy.class);
        serve("/authSubHandler").with(AuthSubHandler.class);
        serve("/getUserInfo").with(GetUserInfo.class);
        serve("/updateIssues").with(UpdateIssues.class);
        serve("/updateIssueTask").with(UpdateIssueTask.class);
      }
    };

    AbstractModule businessModule = new AbstractModule() {
      @Override
      protected void configure() {

      }

      @Provides
      @Singleton
      PersistenceManagerFactory getPmf() {
        return JDOHelper.getPersistenceManagerFactory("transactions-optional");
      }

      @Provides
      ProjectHostingHelper getProjectHostingHelper() {
        return new ProjectHostingHelper();
      }
    };

    return Guice.createInjector(servletModule, businessModule);
  }
}
