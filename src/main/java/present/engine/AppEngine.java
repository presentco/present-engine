package present.engine;

import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;

/**
 * App Engine utilities
 *
 * @author Bob Lee
 */
public class AppEngine {

  /**
   * Get the deployment application id. Return an application id similar to the one reported by
   * SystemProperty.applicationId, but which can be used from remote code for migration.
   */
  public static String applicationId() {
    // Use ApiProxy instead of SystemProperty.applicationId so this works with RemoteTool.
    ApiProxy.Environment environment = ApiProxy.getCurrentEnvironment();
    if (environment == null) return null;
    String id = environment.getAppId();
    // Work around a bug in App Engine.
    if (id.startsWith("s~")) id = id.substring(2);
    return id;
  }

  /**
   * Returns true if this is the development server.
   */
  public static boolean isDevelopment() {
    String applicationId = applicationId();
    return applicationId == null || applicationId.equals("development");
  }

  public static void main(String[] args) {
    System.out.println(applicationId());
    System.out.println(SystemProperty.environment);
  }
}
