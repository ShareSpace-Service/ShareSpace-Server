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
		// 파일 확잠자 가져오기
		String extension = extractFileExtension(multipartFile.getOriginalFilename());
		// 파일 확장자 검증
		isAllowedFileExtension(extension);
		// 파일명을 UUID로 고유하게 설정
		String fileName = dirName + "/" + UUID.randomUUID() + "." + extension;

		// 파일 메타데이터 설정
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(multipartFile.getContentType());
		metadata.setContentLength(multipartFile.getSize());

		// S3 Bucket에 업로드
		putS3(fileName, multipartFile, metadata);

		// 업로드된 파일의 URL 반환
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

		for (MultipartFile file : multipartFiles) {
			String fileName = uploadSingleImage(file, dirName);
			uploadedUrls.add(fileName);
		}

		return uploadedUrls;
	}

	/**
	 * <p>다중 이미지 수정 메서드</p>
	 *
	 * <p>이 메서드는 기존 이미지의 일부를 삭제하고, 새로운 이미지를 업로드한 후
	 * 최종적으로 업데이트된 이미지 URL 리스트 반환.</p>
	 *
	 * <pre>{@code
	 * // 예시 사용법 (place/{userId} 폴더 내 이미지 삭제 및 업로드)
	 * List<String> updateImages = s3ImageUpload.updateImages(
	 * 			deleteImageUrl, newImageUrl,
	 * 			"place/" + user.getId(), existingImageUrl
	 * );
	 * }</pre>
	 *
	 * @param deleteImageUrls 삭제할 이미지의 URL 목록. (S3에서 삭제할 이미지 URL들)
	 * @param newImageUrl 업로드할 새로운 이미지 파일들의 리스트. (MultipartFile 타입)
	 * @param dirName S3에 저장할 디렉토리 이름. (예: "profile/", "places/{userId}")
	 * @param existingImageUrl 기존에 저장된 이미지 URL. (삭제 후 유지될 이미지 URL)
	 * @return 최종적으로 유지되거나 새로 추가된 이미지들의 URL 리스트.
	 *         S3에서 삭제한 URL을 제거하고, 새로 업로드한 URL을 추가한 리스트를 반환.
	 * @Author thereisname
	 */
	public List<String> updateImageSet(List<String> deleteImageUrls, List<MultipartFile> newImageUrl, String dirName, String existingImageUrl) {
		// 기존 이미지 URL을 리스트에 추가
		List<String> uploadedUrls = new ArrayList<>(List.of(existingImageUrl));

		// S3에서 이미지 삭제
		if (!deleteImageUrls.isEmpty()) {
			deleteImageUrls.forEach(this::removeImageFromS3);
			// 삭제된 URL을 목록에서 제거
			uploadedUrls.removeAll(deleteImageUrls);
		}

		// 새로운 이미지가 있으면 S3에 업로드
		if (hasValidImages(newImageUrl)) {
			// 새로운 이미지 파일을 S3에 업로드하고, URL을 리스트에 추가
			List<String> url = uploadImages(newImageUrl, dirName);
			uploadedUrls.addAll(url);
		}

		// 최종적으로 남은 이미지 URL 리스트 반환
		return uploadedUrls;
	}


	/**
	 * <p>기존 단일 이미지를 S3에서 삭제하고 새로운 이미지를 업로드하는 메서드.</p>
	 *
	 * <p>이 메서드는 주어진 기존 이미지 URL을 사용하여 이미지를 S3에서 삭제한 후, 새로 업로드된 이미지를 S3에 저장하고
	 * 해당 이미지의 S3 URL을 반환합니다.</p>
	 *
	 * <p>처리 과정:</p>
	 * <ul>
	 *     <li>기존 이미지 URL을 기반으로 S3에서 이미지를 삭제합니다.</li>
	 *     <li>새로운 이미지를 주어진 디렉토리에 업로드하고 해당 이미지의 S3 URL을 반환합니다.</li>
	 * </ul>
	 *
	 * @param deleteImageUrl 삭제할 기존 이미지의 S3 URL (String 타입)
	 * @param newImageUrl 업로드할 새로운 이미지 파일 (MultipartFile 타입)
	 * @param dirName S3에 저장될 이미지 폴더 경로 (예: "profile", "images/uploads/")
	 * @return 업로드된 파일의 S3 URL을 반환합니다.
	 *
	 * @Author thereisname
	 */
	public String updateImage(String deleteImageUrl, MultipartFile newImageUrl, String dirName) {
		removeImageFromS3(deleteImageUrl);
		return uploadSingleImage(newImageUrl, dirName);
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

	// task: 주어진 파일을 S3 버킷에 업로드하는 메서드
	private void putS3(String fileName, MultipartFile multipartFile, ObjectMetadata metadata) {
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

	// task: 한개의 이미지 삭제
	private void removeImageFromS3(String fileUrl) {
		try {
			amazonS3.deleteObject(
				bucketName, fileUrl.substring(fileUrl.indexOf(".com") + 5)
			);
		} catch (AmazonS3Exception e) {
			log.error("Image Delete Error: {}", e.getMessage());
			throw new CustomRuntimeException(ImageException.IMAGE_DELETE_FAIL);
		}
	}

	// task: file 확장자 가져오기
	private String extractFileExtension(String fileName) {
		// 파일 이름이 null이거나 확장자가 없는 경우 처리
		if (fileName != null && fileName.contains(".")) {
			// 마지막 점(.) 이후의 문자열을 확장자로 반환
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		} else {
			throw new IllegalArgumentException("Invalid file: no extension found.");
		}
	}

	// task: file 확장자 검증
	private void isAllowedFileExtension(String extension) {
		List<String> allowedExtensions = Arrays.asList("png", "jpeg", "jpg");

		// 확장자가 허용된 리스트에 있는지 검사
		if (!allowedExtensions.contains(extension.toLowerCase())) {
			throw new CustomRuntimeException(ImageException.INVALID_FILE_EXTENSION);
		}
	}
}