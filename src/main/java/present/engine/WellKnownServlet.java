package present.engine;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Forwards /.well-known/* to /*. Works around fact that App Engine can't serve up /.well-known.
 *
 * To use, add this to web.xml:
 *
 * <pre>
 *   <servlet>
 *     <servlet-name>wellKnown</servlet-name>
 *     <servlet-class>present.engine.WellKnownServlet</servlet-class>
 *   </servlet>
 *   <servlet-mapping>
 *     <servlet-name>wellKnown</servlet-name>
 *     <url-pattern>/.well-known/*</url-pattern>
 *   </servlet-mapping>
 * </pre>
 *
 * @author Bob Lee (bob@present.co)
 */
public class WellKnownServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.getRequestDispatcher(request.getPathInfo()).forward(request, response);
  }
}
