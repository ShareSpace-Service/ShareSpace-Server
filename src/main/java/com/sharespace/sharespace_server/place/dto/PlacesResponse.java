package com.sharespace.sharespace_server.place.dto;

import static com.sharespace.sharespace_server.global.utils.LocationTransform.*;

import java.util.List;
import java.util.Random;

import com.sharespace.sharespace_server.global.enums.Category;
import com.sharespace.sharespace_server.place.entity.Place;
import com.sharespace.sharespace_server.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PlacesResponse {
	private Long placeId;
	private String title;
	private Category category;
	private String imageUrl;
	private Integer distance;

	public static PlacesResponse from(Place place, User guest) {
		Integer distance = calculateDistance(guest.getLatitude(), guest.getLongitude(), place.getUser().getLatitude(), place.getUser().getLongitude());
		String randomImageUrl = getRandomImageUrl(place);

		return PlacesResponse.builder()
			.placeId(place.getId())
			.title(place.getTitle())
			.category(place.getCategory())
			.imageUrl(randomImageUrl)
			.distance(distance)
			.build();
	}

	// 랜덤 이미지를 선택하는 메서드
	private static String getRandomImageUrl(Place place) {
		List<String> imageList = List.of(place.getImageUrl().split(","));
		Random random = new Random();
		return imageList.get(random.nextInt(imageList.size()));
	}
}
