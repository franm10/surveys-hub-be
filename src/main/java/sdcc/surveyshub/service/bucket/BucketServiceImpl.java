package sdcc.surveyshub.service.bucket;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import sdcc.surveyshub.exception.UploadImageException;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class BucketServiceImpl implements BucketService {

    private final Bucket bucket;

    public void uploadBase64Png(String objectPath, String imgDataBase64) throws UploadImageException {
        log.info("[BucketAPI][uploadImageOnBucket] Uploading image to bucket: {}", objectPath);
        try {
            byte[] bytes = Base64.getDecoder().decode(imgDataBase64);
            bucket.create(objectPath, bytes, "image/png");
            log.info("[BucketAPI][uploadImageOnBucket] File uploaded: gs://{}/{}", bucket.getName(), objectPath);
        } catch( IllegalArgumentException e ) {
            log.warn("[BucketAPI][uploadImageOnBucket] Invalid Base64 image data");
            throw new UploadImageException("Invalid Base64 image data");
        } catch( StorageException e ) {
            log.error("[BucketAPI][uploadImageOnBucket] Failed to save image on bucket");
            throw new UploadImageException("Failed to save image on bucket");
        } catch( Exception e ) {
            log.error("[BucketAPI][uploadImageOnBucket] Unknown error");
            throw new UploadImageException("Unknown error");
        }
    }

    public void deleteFolder(String name) {
        log.warn("[BucketAPI][BucketAPI][deleteFolderFromBucket] Deleting folder: {}", name);
        Page<Blob> blobs = bucket.list(Storage.BlobListOption.prefix(name + "/"));
        for( Blob b : blobs.iterateAll() )
            b.delete();
    }

}