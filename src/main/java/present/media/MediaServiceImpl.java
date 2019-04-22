package present.media;

import java.io.IOException;

public class MediaServiceImpl implements MediaService {

  @Override public MediaResponse upload(UploadRequest request) throws IOException {
    return Media.upload(request.uuid, request.type, request.bytes).toResponse();
  }

  @Override public MediaResponse copy(CopyRequest request) throws IOException {
    return Media.copy(request.uuid, request.url).toResponse();
  }
}
