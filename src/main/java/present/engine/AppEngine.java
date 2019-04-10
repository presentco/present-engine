package present.engine;

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
    String id = ApiProxy.getCurrentEnvironment().getAppId();
    // Work around a bug in App Engine.
    if (id.startsWith("s~")) id = id.substring(2);
    return id;
  }
}
