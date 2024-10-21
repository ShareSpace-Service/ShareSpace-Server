package com.sharespace.sharespace_server.global.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>S3 이미지 업로드 관리 클래스</p>
 *
 * <p>이 클래스는 AWS S3를 사용하여 단일 또는 다중 이미지 파일을 업로드, 수정, 삭제하는 기능을 제공합니다.
 * 파일을 S3에 업로드할 때 고유한 파일명을 사용하며, Public 읽기 권한을 설정하여 URL을 통해 접근할 수 있도록 합니다.</p>
 *
 * <p>기능:</p>
 * <ul>
 *     <li>단일 이미지 파일 업로드</li>
 *     <li>다중 이미지 파일 업로드</li>
 *     <li>이미지 수정 (삭제 및 업로드)</li>
 *     <li>이미지 삭제</li>
 * </ul>
 *
 * <p><b>사용 예시:</b></p>
 * <pre>{@code
 * // S3ImageUpload 객체 주입 받기
 * private S3ImageUpload s3ImageUpload;
 *
 * // 이미지 업로드 예시
 * MultipartFile imageFile = placeRequest.getImageUrl();
 * String imageUrl = s3ImageUpload.uploadFile(imageFile, "profile");
 * }</pre>
 *
 * <p>주의사항:</p>
 * <ul>
 *     <li>업로드할 파일의 확장자는 반드시 허용된 확장자여야 합니다 (png, jpeg, jpg).</li>
 *     <li>이미지 삭제 시 파일 URL이 유효하지 않으면 예외가 발생할 수 있습니다.</li>
 * </ul>
 *
 * @Author thereisname
 */
@Component
@Slf4j
public class S3ImageUpload {

	private final AmazonS3 amazonS3;

	public S3ImageUpload(AmazonS3 amazonS3) {
		this.amazonS3 = amazonS3;
	}

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	/**
	 * <p>단일 이미지 파일을 S3 버킷에 업로드하는 메서드</p>
	 *
	 * <p>
	 * 주어진 `MultipartFile`을 S3의 지정된 디렉토리에 업로드하며, 업로드된 파일의 S3 경로를 반환합니다.
	 * 파일은 UUID를 사용하여 고유한 이름으로 저장되며, S3에 Public 읽기 권한을 부여합니다.
	 * 업로드 실패 시, `ImageException.IMAGE_UPLOAD_FAIL` 예외를 던집니다.
	 * </p>
	 *
	 * <pre>{@code
	 * // 예시 사용법
	 * MultipartFile imageFile = placeRequest.getImageUrl();
	 * String imageUrl = s3ImageUpload.uploadFile(imageFile, "profile"); // "profile" 디렉토리에 이미지 업로드
	 * }</pre>
	 *
	 * <p>업로드 프로세스:
	 * <pre>
	 * 1. `UUID.randomUUID()`를 사용하여 고유한 파일명을 생성하고, 지정된 디렉토리와 결합하여 저장 경로를 설정합니다.
	 * 2. `ObjectMetadata`를 사용하여 파일의 Content-Type과 Content-Length를 설정합니다.
	 * 3. `putObject`를 통해 파일을 S3에 업로드하며, Public 읽기 권한을 부여합니다.
	 * 4. 업로드 완료 후 업로드된 파일의 S3 내 경로(파일명)를 반환합니다.
	 * </pre>
	 *
	 * 예외 처리:
	 * - 업로드 중 `IOException` 발생 시, 에러 로그를 출력하고 `CustomRuntimeException`을 통해 업로드 실패 예외를 던집니다.
	 *</p>
	 *
	 * @param multipartFile 업로드할 이미지 파일 (`MultipartFile` 타입)
	 * @param dirName S3에 저장될 이미지 폴더 경로 (예: "profile", "images/uploads/")
	 * @return 업로드된 파일의 S3 경로 (예: "profile/uuid_filename.jpg")
	 * @throws CustomRuntimeException 이미지 업로드 실패
	 * @Author thereisname
	 */
	public String uploadSingleImage(MultipartFile multipartFile, String dirName) {
		validateFileExtension(multipartFile);
		String fileName = createUniqueFileName(dirName, multipartFile.getOriginalFilename());
		ObjectMetadata metadata = createMetadata(multipartFile);

		uploadFileToS3(fileName, multipartFile, metadata);	// S3 Bucket에 업로드

		return amazonS3.getUrl(bucketName, fileName).toString();
	}

	/**
	 * <p>다중 이미지 업로드 메서드</p>
	 *
	 * <p>주어진 이미지 파일 리스트를 받아 지정된 S3 버킷의 특정 디렉토리에 업로드합니다.</p>
	 * <p>업로드된 파일들의 S3 경로(파일명)를 콤마(",")로 구분한 문자열로 반환합니다.</p>
	 *
	 * <pre>{@code
	 * // 예시 사용법 (이미지를 place/{userId} 폴더 내 저장)
	 * String combinedImagePaths = s3ImageUpload.uploadMultipleFiles(
	 * 		placeRequest.getImageUrl(), "place/" + user.getId()
	 * );
	 * }</pre>
	 *
	 * <p>
	 * 업로드 프로세스:
	 * <pre>
	 * 1. 주어진 `multipartFiles` 리스트에서 각 파일을 순차적으로 처리합니다.
	 * 2. 각 파일은 S3에 지정된 `dirName` 경로를 포함하여 업로드됩니다.
	 * 3. `uploadFile` 메서드를 호출하여 파일을 업로드하며, 해당 파일의 S3 경로(파일명)를 반환받습니다.
	 * 4. 모든 파일이 업로드되면 각 파일의 S3 경로(파일명)를 콤마(",")로 구분하여 하나의 문자열로 반환합니다.
	 * </pre>
	 * </p>
	 *
	 * 예외 처리:
	 * - `uploadFile` 메서드에서 발생하는 예외가 상위로 전파됩니다.
	 * - 업로드 중 오류가 발생할 경우, 해당 예외에 따라 적절한 오류 처리를 수행해야 합니다.
	 *
	 * @param multipartFiles 업로드할 이미지 파일들의 리스트. (비어 있거나 null인 경우 빈 문자열을 반환)
	 * @param dirName S3에 저장될 이미지 폴더 경로 (예: "profile/", "places/3/")
	 * @return 업로드된 이미지의 S3 경로(파일명)를 List<String> 타입으로 반환 (예: ["file1","file2","file3"])
	 * @Author thereisname
	 */
	public List<String> uploadImages(List<MultipartFile> multipartFiles, String dirName) {
		List<String> uploadedUrls = new ArrayList<>();
		multipartFiles.forEach(url -> uploadedUrls.add(uploadSingleImage(url, dirName)));
		return uploadedUrls;
	}

	/**
	 * S3에서 이미지 삭제 메서드
	 *
	 * @param fileUrl 삭제할 이미지의 S3 URL
	 */
	public void deleteImage(String fileUrl) {
		try {
			amazonS3.deleteObject(bucketName, extractKeyFromUrl(fileUrl));
		} catch (AmazonS3Exception e) {
			log.error("Image Delete Error: {}", e.getMessage());
			throw new CustomRuntimeException(ImageException.IMAGE_DELETE_FAIL);
		}
	}

	/**
	 * 이미지가 존재할 경우 삭제
	 *
	 * @param deleteImageUrl 삭제할 이미지 URL
	 */
	private void deleteImageIfExists(String deleteImageUrl) {
		if (deleteImageUrl != null && !deleteImageUrl.isEmpty()) {
			deleteImage(deleteImageUrl);
		}
	}

	/**
	 * 단일 이미지 업데이트 메서드 (단일 이미지)
	 *
	 * @param deleteImageUrl 삭제할 이미지 URL
	 * @param newImage 업로드할 새로운 이미지 파일
	 * @param dirName 저장할 디렉토리 이름
	 * @return 업데이트된 이미지의 S3 URL
	 */
	public String updateImage(String deleteImageUrl, MultipartFile newImage, String dirName) {
		deleteImageIfExists(deleteImageUrl);
		return uploadSingleImage(newImage, dirName);
	}

	/**
	 * 다중 이미지 수정 메서드
	 *
	 * @param deleteImageUrls 삭제할 이미지의 URL 목록
	 * @param newImageFiles 업로드할 새로운 이미지 파일들의 리스트
	 * @param dirName 저장할 디렉토리 이름
	 * @param existingImageUrl 기존에 저장된 이미지 URL
	 * @return 최종적으로 유지되거나 새로 추가된 이미지들의 URL 리스트
	 */
	public List<String> updateImageSet(List<String> deleteImageUrls, List<MultipartFile> newImageFiles, String dirName, String existingImageUrl) {
		List<String> updatedUrls = new ArrayList<>(Arrays.asList(existingImageUrl.split(",")));

		if (deleteImageUrls != null && !deleteImageUrls.isEmpty()) {
			deleteImageUrls.forEach(this::deleteImageIfExists);
			updatedUrls.removeAll(deleteImageUrls);
		}

		if (hasValidImages(newImageFiles)) {
			updatedUrls.addAll(uploadImages(newImageFiles, dirName));
		}

		return updatedUrls;
	}

	/**
	 * 파일 확장자 유효성 검사 메서드
	 *
	 * @param multipartFile 업로드할 파일
	 */
	private void validateFileExtension(MultipartFile multipartFile) {
		String extension = extractFileExtension(multipartFile.getOriginalFilename());
		List<String> allowedExtensions = Arrays.asList("png", "jpeg", "jpg");
		if (!allowedExtensions.contains(extension.toLowerCase())) {
			throw new CustomRuntimeException(ImageException.INVALID_FILE_EXTENSION);
		}
	}

	/**
	 * 파일 확장자 추출 메서드
	 *
	 * @param fileName 파일 이름
	 * @return 파일 확장자
	 */
	private String extractFileExtension(String fileName) {
		if (fileName != null && fileName.contains(".")) {
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		} else {
			throw new CustomRuntimeException(ImageException.IMAGE_NOT_EXCEPTION);
		}
	}

	/**
	 * 고유한 파일명 생성 메서드
	 *
	 * @param dirName 디렉토리 이름
	 * @param originalFileName 원본 파일 이름
	 * @return 고유한 파일명
	 */
	private String createUniqueFileName(String dirName, String originalFileName) {
		String extension = extractFileExtension(originalFileName);
		return dirName + "/" + UUID.randomUUID() + "." + extension;
	}

	/**
	 * 파일 메타데이터 생성 메서드
	 *
	 * @param multipartFile 파일
	 * @return 파일 메타데이터
	 */
	private ObjectMetadata createMetadata(MultipartFile multipartFile) {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(multipartFile.getContentType());
		metadata.setContentLength(multipartFile.getSize());
		return metadata;
	}

	/**
	 * 파일을 S3에 업로드하는 메서드
	 *
	 * @param fileName 파일 이름
	 * @param multipartFile 업로드할 파일
	 * @param metadata 파일 메타데이터
	 */
	private void uploadFileToS3(String fileName, MultipartFile multipartFile, ObjectMetadata metadata) {
		try {
			// S3에 파일 업로드
			amazonS3.putObject(
				new PutObjectRequest(bucketName, fileName, multipartFile.getInputStream(), metadata)
					.withCannedAcl(CannedAccessControlList.PublicRead)); // Public 권한 부여
			multipartFile.getInputStream().close();
		} catch (IOException e) {
			log.error("Image Upload error: {}", e.getMessage());
			throw new CustomRuntimeException(ImageException.IMAGE_UPLOAD_FAIL);
		}
	}

	/**
	 * S3 URL에서 Key 추출 메서드
	 *
	 * @param fileUrl 파일 URL
	 * @return S3 Key
	 */
	private String extractKeyFromUrl(String fileUrl) {
		return fileUrl.substring(fileUrl.indexOf(".com") + 5);
	}

	/**
	 * 이미지 파일 리스트에서 null 값이나 비어있는 파일이 있는지 확인합니다.
	 *
	 * @param files 업로드할 이미지 파일 리스트 (MultipartFile 타입)
	 * @return 모든 파일이 유효한 경우 true, null 값 또는 비어있는 파일이 하나라도 있으면 false 반환
	 * @Author thereisname
	 */
	public boolean hasValidImages(List<MultipartFile> files) {
		return files != null && !files.isEmpty() && files.stream().allMatch(file -> file != null && !file.isEmpty());
	}
}