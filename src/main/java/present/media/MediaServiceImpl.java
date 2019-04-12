package present.media;

import java.io.IOException;

public class MediaServiceImpl implements MediaService {

  @Override public MediaResponse upload(UploadRequest request) throws IOException {
    return Media.upload(request.uuid, request.type, request.bytes).toResponse();
  }
}
