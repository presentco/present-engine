package present.media;

import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.io.Resources;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import javax.annotation.Nullable;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import present.objectify.AbstractEntity;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Media entity
 *
 * @author Bob Lee
 */
@Entity @Cache public class Media extends AbstractEntity<Media> {

  private static final Logger logger = LoggerFactory.getLogger(Media.class);

  /** A unique id for this content */
  @Id public String uuid;

  /** MIME type. */
  public String type;

  /** Optional Google image serving URL (https://stackoverflow.com/a/25438197/300162). */
  public String servingUrl;

  @Override protected Media getThis() {
    return this;
  }

  //public Content() { }
  //
  //public Content(String uuid, ContentType type) {
  //  this.uuid = uuid;
  //  this.type = type;
  //}
  //
  //public void setServingUrl() {
  //  // TODO: Mock support for Images Service in development.
  //  if (Environment.isDevelopment()) return;
  //  Preconditions.checkState(type == ContentType.JPEG);
  //  URI uri = URI.create(originalUrl());
  //  servingUrl = ImagesServiceFactory.getImagesService().getServingUrl(
  //      ServingUrlOptions.Builder.withGoogleStorageFileName("/gs" + uri.getRawPath())
  //          .secureUrl(true));
  //}
  //
  ///**
  // * Create a new Content item and upload the media to storage.
  // */
  //public static Content createAndUpload(ContentUploadRequest request) throws IOException {
  //  if (request.content.size() == 0) throw new ClientException("Empty content");
  //  GoogleCloudStorage.upload(request);
  //  Content content = new Content(request.uuid, request.type);
  //  if (request.type == ContentType.JPEG) content.setServingUrl();
  //  content.save().now();
  //  return content;
  //}
  //
  ///**
  // * Capture an external photo from the specified URL, upload it to our content service,
  // * and return the newly created Content.
  // * @param test Each call with test true uses the same test UUID.
  // * @param photoUrl
  // */
  //public static @Nullable Content capturePhotoToContent(String photoUrl, boolean test) throws IOException {
  //  if (photoUrl != null) {
  //    Stopwatch sw = Stopwatch.createStarted();
  //    ByteString photo = ByteString.of(Resources.toByteArray(new URL(photoUrl)));
  //    String uuid = test ? Uuids.NULL : Uuids.newUuid();
  //    Content content = Content.createAndUpload(
  //        new ContentUploadRequest(uuid, ContentType.JPEG, photo, null));
  //    logger.info("Copied photo in {}.", sw);
  //    return content;
  //  }
  //  return null;
  //}
  //
  //
  ///** Returns the URL for the attached content or null if no content is attached. */
  //public String originalUrl() {
  //  if (type == null) return null;
  //  return GoogleCloudStorage.urlForContent(type, uuid, GoogleCloudStorage.DEFAULT_CONTENT_PATH);
  //}
  //
  ///** Returns the URL for the attached content or null if no content is attached. */
  //public String url() {
  //  if (type == null) return null;
  //  if (servingUrl != null) return servingUrl;
  //  if (type == ContentType.JPEG) {
  //    // This shouldn't happen.
  //    logger.error("Missing servingUrl for Content #{}.", uuid);
  //  }
  //  return originalUrl();
  //}
  //
  ///**
  // * Returns a URL for an image scaled and center-cropped to the given width and height.
  // */
  //public String url(int width, int height) {
  //  if (type == null) return null;
  //  Preconditions.checkState(type == ContentType.JPEG);
  //  if (servingUrl != null) return servingUrl + "=w" + width + "-h" + height + "-n-rj";
  //  logger.warn("Missing servingUrl for Content #{}.", uuid);
  //  return originalUrl();
  //}
  //
  ///**
  // * Returns a URL for a center cropped square image scaled given size.
  // */
  //public String squareUrl(int size) {
  //  if (type == null) return null;
  //  Preconditions.checkState(type == ContentType.JPEG);
  //  // param '=s' size
  //  // param '-c' crop
  //  // param '-p' "smart crop"
  //  if (servingUrl != null) return servingUrl + "=s" + size + "-p";
  //  logger.warn("Missing servingUrl for Content #{}.", uuid);
  //  return originalUrl();
  //}
  //
  ///**
  // * Returns a URL for a circular cropped image scaled and center-cropped to the given width and height.
  // */
  //public String circleUrl(int diameter) {
  //  if (type == null) return null;
  //  Preconditions.checkState(type == ContentType.JPEG);
  //  if (servingUrl != null) return servingUrl + "=w" + diameter + "-h" + diameter + "-cc-nu";
  //  logger.warn("Missing servingUrl for Content #{}.", uuid);
  //  return originalUrl();
  //}
  //
  ///**
  // * Returns the URL for a JPG thumbnail representation of the attached content
  // * or null if no thumbnail is available.
  // **/
  //@Nullable public String thumbnailUrl() {
  //  if (type == null) return null;
  //  return GoogleCloudStorage.urlForContentThumbnail(type, uuid,
  //      GoogleCloudStorage.DEFAULT_CONTENT_PATH);
  //}
  //
  //@Nullable public ContentResponse toResponse() {
  //  return new ContentResponse(uuid, type, url(), thumbnailUrl());
  //}
  //
  //public static Key<Content> keyFor(String uuid) {
  //  return Key.create(Content.class, uuid);
  //}
  //
  //public static Ref<Content> refFor(String uuid) {
  //  return Ref.create(keyFor(uuid));
  //}
  //
  //@Nullable public static Content get(Key<Content> key) {
  //  return ofy().load().key(key).now();
  //}
  //
  //public static Ref<Content> refFor(ContentReferenceRequest request) {
  //  return refFor(request.uuid);
  //}
  //
  //@Nullable public static Content get(String uuid) {
  //  return get(keyFor(uuid));
  //}
  //
  //@Nullable public static Content get(ContentReferenceRequest request) {
  //  // TODO: Validate that the request content type matches the stored content type here?
  //  return get(request.uuid);
  //}
  //
  //@Override public String toString() {
  //  return MoreObjects.toStringHelper(this)
  //      .add("uuid", uuid)
  //      .add("type", type)
  //      .add("servingUrl", servingUrl)
  //      .add("createdTime", createdTime)
  //      .add("url", url())
  //      .toString();
  //}
  //
  //@Override protected Content getThis() {
  //  return this;
  //}
}

