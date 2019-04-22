package present.engine;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import com.google.apphosting.api.ApiProxy;
import com.google.common.base.Stopwatch;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * App Engine utilities
 *
 * @author Bob Lee
 */
public class AppEngine {

  private static final Logger logger = LoggerFactory.getLogger(AppEngine.class);

  /** Application ID used by development server. */
  public static final String DEVELOPMENT_ID = "no_app_id";

  /**
   * Gets the App Engine application ID. Similar to SystemProperty.applicationId, except this works
   * with the Remote API. Returns null if we are not running in App Engine. Returns
   * "no_app_id" if running in the development server and no application ID is set.
   */
  public static String applicationId() {
    ApiProxy.Environment environment = ApiProxy.getCurrentEnvironment();
    if (environment == null) return null;
    String id = ApiProxy.getCurrentEnvironment().getAppId();
    // Work around a bug in App Engine.
    if (id.startsWith("s~")) id = id.substring(2);
    return id;
  }

  /**
   * Returns true if this is the development server or in a test.
   */
  public static boolean isDevelopment() {
    String applicationId = applicationId();
    return applicationId == null || applicationId.equals(DEVELOPMENT_ID);
  }

  /**
   * Routes App Engine API calls to the given server using the
   * <a href="https://cloud.google.com/appengine/docs/standard/python/tools/remoteapi">App Engine
   * Remote API</a>. Port defaults to 8080 for localhost and 443 for other hosts.
   * Wraps task with Objectify filter.
   *
   * @param server host and optional port (example: "localhost:8080")
   */
  public static void against(String server, Task task) {
    try {
      RemoteApiInstaller installer = installRemoteAPI(server);
      Stopwatch sw = Stopwatch.createUnstarted();
      try (Closeable closeable = ObjectifyService.begin()) {
        sw.start();
        task.run();
      } finally {
        logger.info("Completed in {}.", sw);
        installer.uninstall();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static RemoteApiInstaller installRemoteAPI(String server) throws IOException {
    String host;
    int port;
    int colon = server.indexOf(':');
    if (colon > -1) {
      host = server.substring(0, colon);
      port = Integer.parseInt(server.substring(colon + 1));
    } else {
      host = server;
      port = host.equals("localhost") ? 8080 : 443;
    }
    RemoteApiOptions options = new RemoteApiOptions().server(host, port);
    if (host.equals("localhost")) {
      options = options.useDevelopmentServerCredential();
    } else {
      options = options.useApplicationDefaultCredential();
    }
    RemoteApiInstaller installer = new RemoteApiInstaller();
    installer.install(options);
    return installer;
  }

  public interface Task {
    void run() throws Exception;
  }

  public static void main(String[] args) {
    System.out.println(applicationId());
    against("localhost", () -> {
      System.out.println(applicationId());
    });
    against("api.present.co", () -> {
      System.out.println(applicationId());
    });
  }
}
