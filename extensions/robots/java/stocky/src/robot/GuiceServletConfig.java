package robot;

import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class GuiceServletConfig extends GuiceServletContextListener {
  private static final Logger log = Logger.getLogger(GuiceServletConfig.class.getName());

  @Override
  protected Injector getInjector() {
    ServletModule servletModule = new ServletModule() {
      @Override
      protected void configureServlets() {
        serveRegex("\\/_wave/.*").with(Stocky.class);
      }
    };

    AbstractModule businessModule = new AbstractModule() {
      @Override
      protected void configure() {
      }

      @Provides
      @Singleton
      PersistenceManagerFactory providePmf() {
        return JDOHelper.getPersistenceManagerFactory("transactions-optional");
      }

      @Provides
      @Singleton
      @Named("Stocky")
      Pattern provideStockyPattern() {
        String re = "^\\$([a-zA-Z]+)([^a-zA-Z])";
        return Pattern.compile(re, Pattern.DOTALL);
      }
    };

    return Guice.createInjector(servletModule, businessModule);
  }
}
