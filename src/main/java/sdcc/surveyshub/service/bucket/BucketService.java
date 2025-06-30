package sdcc.surveyshub.service.bucket;

import sdcc.surveyshub.exception.UploadImageException;

public interface BucketService {

    void uploadBase64Png(String imgDataBase64, String objectPath) throws UploadImageException;

    void deleteFolder(String surveyId);

}
