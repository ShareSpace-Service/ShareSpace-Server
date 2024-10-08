package com.sharespace.sharespace_server.global.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.ImageException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class S3ImageUpload {

	private static AmazonS3 staticAmazonS3;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	private static String staticBucketName;

	public S3ImageUpload(AmazonS3 amazonS3) {
		staticAmazonS3 = amazonS3;
	}

	@PostConstruct
	public void init() {
		staticBucketName = bucketName;
	}

	/**
	 * <p>단일 이미지 파일을 S3 버킷에 업로드하는 메서드</p>
	 *
	 * <p>
	 * 주어진 `MultipartFile`을 S3의 지정된 디렉토리에 업로드하며, 업로드된 파일의 URL을 반환합니다.
	 * 파일은 UUID를 사용하여 고유한 이름으로 저장되며, S3에 Public 읽기 권한을 부여합니다.
	 * 업로드 실패 시, `ImageException.IMAGE_UPLOAD_FAIL` 예외를 던집니다.
	 * </p>
	 *
	 * <pre>{@code
	 * // 예시 사용법
	 * MultipartFile imageFile = placeRequest.getImageUrl();
	 * String imageUrl = uploadFile(imageFile, "profile"); // "profile" 디렉토리에 이미지 업로드
	 * }</pre>
	 *
	 * <p>업로드 프로세스:
	 * <pre>
	 * 1. `UUID.randomUUID()`를 사용하여 고유한 파일명을 생성하고, 원본 파일명과 함께 저장 경로를 설정합니다.
	 * 2. `ObjectMetadata`를 사용하여 파일의 Content-Type과 Content-Length를 설정합니다.
	 * 3. `putObject`를 통해 파일을 S3에 업로드하며, Public 읽기 권한을 부여합니다.
	 * 4. 업로드 완료 후 업로드된 파일의 URL을 반환합니다.
	 * </pre>
	 *
	 * 예외 처리:
	 * - 업로드 중 `IOException` 발생 시, 에러 로그를 출력하고 `CustomRuntimeException`을 통해 업로드 실패 예외를 던집니다.
	 *</p>
	 *
	 * @param multipartFile 업로드할 이미지 파일 (`MultipartFile` 타입)
	 * @param dirName S3에 저장될 이미지 폴더 경로 (예: "profile", "images/uploads/")
	 * @return 업로드된 파일의 S3 URL (예: "https://your-bucket.s3.amazonaws.com/profile/uuid_filename.jpg")
	 */
	public static String uploadFile(MultipartFile multipartFile, String dirName) {
		// 파일명을 UUID로 고유하게 설정
		String fileName = dirName + "/" + UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();

		// 파일 메타데이터 설정
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(multipartFile.getContentType());
		metadata.setContentLength(multipartFile.getSize());

		try {
			// S3에 파일 업로드
			staticAmazonS3.putObject(new PutObjectRequest(staticBucketName, fileName, multipartFile.getInputStream(), metadata)
				.withCannedAcl(CannedAccessControlList.PublicRead)); // Public 권한 부여
		} catch (IOException e) {
			log.error("Image Upload error: " + e.getMessage());
			throw new CustomRuntimeException(ImageException.IMAGE_UPLOAD_FAIL);
		}

		// 업로드된 파일의 URL 반환
		return staticAmazonS3.getUrl(staticBucketName, fileName).toString();
	}

	/**
	 * <p>다중 이미지 업로드 메서드</p>
	 *
	 * <p>주어진 이미지 파일 리스트를 받아 지정된 S3 버킷의 특정 디렉토리에 업로드</p>
	 * <p>업로드된 파일들의 URL은 콤마(",")로 구분된 문자열 반환</p>
	 *
	 * <p>
	 * 업로드 프로세스:
	 * <pre>
	 * 1. 주어진 `multipartFiles` 리스트에서 각 파일을 순차적으로 처리합니다.
	 * 2. 각 파일은 S3에 지정된 `dirName` 경로를 포함하여 업로드됩니다.
	 * 3. `uploadFile` 메서드를 통해 파일을 업로드하며, 해당 파일의 S3 URL을 반환받습니다.
	 * 4. 모든 파일이 업로드되면 각 파일의 S3 URL을 콤마(",")로 구분하여 하나의 문자열로 반환합니다.
	 * </pre>
	 * </p>
	 *
	 * @param multipartFiles 업로드할 이미지 파일들의 리스트. (비어 있거나 null인 경우 빈 문자열 반환)
	 * @param dirName S3에 저장될 이미지 폴더 경로 (예: "profile/", "places/3/")
	 * @return 업로드된 이미지의 S3 URL을 콤마로 구분한 문자열로 반환 (예: "url1,url2,url3")
	 */
	public static String uploadMultipleFiles(List<MultipartFile> multipartFiles, String dirName) {
		List<String> uploadedUrls = new ArrayList<>();

		for (MultipartFile file : multipartFiles) {
			String fileUrl = uploadFile(file, dirName);
			uploadedUrls.add(fileUrl);
		}

		return String.join(",", uploadedUrls);
	}

}
