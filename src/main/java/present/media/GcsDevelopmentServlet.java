package present.media;

import com.google.api.client.util.ByteStreams;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.apphosting.api.ApiProxy;
import java.io.IOException;
import java.nio.channels.Channels;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import present.engine.AppEngine;

import static com.google.appengine.api.utils.SystemProperty.Environment.Value.Development;

/**
 * Serves GCS files in development. Uses "development" for the bucket name.
 *
 * @author Bob Lee (bob@present.co)
 */
public class GcsDevelopmentServlet extends HttpServlet {
  private static final Logger logger = LoggerFactory.getLogger(GcsDevelopmentServlet.class);

  @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    if (SystemProperty.environment.value() != Development) {
      response.sendError(403);
      return;
    }
    GcsService gcsService = GcsServiceFactory.createGcsService();
    String bucket = AppEngine.applicationId();
    GcsFilename file = new GcsFilename(bucket, request.getPathInfo().substring(1));
    GcsFileMetadata metadata = gcsService.getMetadata(file);
    if (metadata == null) {
      logger.error("File not found: " + file);
      response.sendError(404);
      return;
    }
    response.setContentType(metadata.getOptions().getMimeType());
    response.setContentLength((int) metadata.getLength());
    GcsInputChannel channel = gcsService.openReadChannel(file, 0);
    ByteStreams.copy(Channels.newInputStream(channel), response.getOutputStream());
  }
}
