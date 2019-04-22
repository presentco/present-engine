package present.media;

import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.common.base.Stopwatch;
import com.google.common.io.Resources;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import present.engine.AppEngine;
import present.engine.CurrentUser;
import present.objectify.AbstractEntity;
import present.rpc.ClientException;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Media entity
 *
 * @author Bob Lee
 */
@Entity @Cache public class Media extends AbstractEntity<Media> {

  private static final Logger logger = LoggerFactory.getLogger(Media.class);

  static {
    ObjectifyService.register(Media.class);
  }

  /** A unique id for this content */
  @Id public String uuid;

  /** MIME type. */
  public String type;

  /** Public GCS URL. */
  public String url;

  /** Optional Google image serving URL (https://stackoverflow.com/a/25438197/300162). */
  public String imageUrl;

  /** ID of user who uploaded the content. */
  public String uploadedBy;

  /** URL from where the media came. */
  public String sourceUrl;

  public MediaResponse toResponse() {
    return new MediaResponse(uuid, type, url());
  }

  /** Returns the public URL for the media. */
  public String url() {
    if (imageUrl != null) return imageUrl;
    return url;
  }

  @Override protected Media getThis() {
    return this;
  }

  public static Media load(String uuid) {
    return ofy().load().type(Media.class).id(uuid).now();
  }

  /**
   * Uploads media to GCS and creates a corresponding Media entity.
   */
  public static Media upload(String uuid, String type, ByteString bytes) throws IOException {
    return upload(uuid, type, bytes.asByteBuffer(), null);
  }

  private static OkHttpClient client = new OkHttpClient();

  /**
   * Copies media from another URL.
   */
  public static Media copy(String uuid, String sourceUrl) throws IOException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    Request request = new Request.Builder().url(sourceUrl).build();
    String type;
    byte[] bytes;
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new IOException(response.code() + ": " + response.message());
      }
      ResponseBody body = response.body();
      type = body.contentType().toString();
      bytes = body.bytes();
    }
    logger.info("Downloaded {} in {}.", sourceUrl, stopwatch);
    return upload(uuid, type, ByteBuffer.wrap(bytes), sourceUrl);
  }

  private static Media upload(String uuid, String type, ByteBuffer bytes, String sourceUrl)
      throws IOException {
    Media existing = load(uuid);
    String userId = CurrentUser.id();
    if (userId == null) throw new ClientException("Unauthorized");

    // Check if the file was already uploaded.
    if (existing != null) {
      if (!existing.uploadedBy.equals(userId)) {
        throw new ClientException("Unauthorized");
      }
      return existing;
    }

    // Upload file to GCS.
    Stopwatch stopwatch = Stopwatch.createStarted();
    String path = "media/" + uuid + Type.extensionFor(type);
    GcsFilename gcsFile = new GcsFilename(AppEngine.applicationId(), path);
    GcsService gcsService = GcsServiceFactory.createGcsService();
    GcsFileOptions options = new GcsFileOptions.Builder()
        .acl("public_read")
        .mimeType(type)
        .build();
    gcsService.createOrReplace(
        gcsFile,
        options,
        bytes);
    logger.info("Uploaded {} in {}.", gcsFile, stopwatch);
    stopwatch.reset();

    // Create entity.
    Media media = new Media();
    media.uuid = uuid;
    media.type = type;
    media.url = urlFor(gcsFile);
    media.uploadedBy = userId;
    media.sourceUrl = sourceUrl;

    // Serve images using Google Images Service.
    if (!AppEngine.isDevelopment() && Type.isImage(type)) {
      stopwatch.start();
      media.imageUrl = ImagesServiceFactory.getImagesService().getServingUrl(
          ServingUrlOptions.Builder.withGoogleStorageFileName(
              "/gs" + URI.create(media.url).getRawPath()).secureUrl(true));
      logger.info("Got serving URL in {}.", stopwatch);
    }

    media.save();
    return media;
  }

  /** Returns the public URL for the given GCS file name. */
  private static String urlFor(GcsFilename file) {
    String path = file.getObjectName();
    if (AppEngine.isDevelopment()) return "http://localhost:8081/gcs/" + path;
    return "https://storage-download.googleapis.com/" + file.getBucketName() + "/" + path;
  }

  public static final class Type {
    private Type() {}

    public static final String JPEG = "image/jpeg";
    public static final String PNG = "image/png";
    public static final String GIF = "image/gif";
    public static final String WEBP = "image/webp";

    public static boolean isImage(String type) {
      switch (type) {
        case JPEG:
        case PNG:
        case GIF:
        case WEBP:
          return true;
        default: return false;
      }
    }

    public static String extensionFor(String type) {
      switch (type) {
        case JPEG: return ".jpeg";
        case PNG: return ".png";
        case GIF: return ".gif";
        case WEBP: return ".webp";
        default: return "";
      }
    }
  }
}

