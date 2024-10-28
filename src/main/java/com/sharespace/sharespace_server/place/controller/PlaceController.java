package com.sharespace.sharespace_server.place.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.utils.RequestParser;
import com.sharespace.sharespace_server.place.dto.PlaceDetailResponse;
import com.sharespace.sharespace_server.place.dto.PlaceRequest;
import com.sharespace.sharespace_server.place.dto.PlaceUpdateRequest;
import com.sharespace.sharespace_server.place.dto.PlacesResponse;
import com.sharespace.sharespace_server.place.service.PlaceService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/place")
@Slf4j
public class PlaceController {
	private final PlaceService placeService;

	// task: main 화면에 보일 장소 리스트 조회
	@GetMapping
	public BaseResponse<List<PlacesResponse>> getPlaces(HttpServletRequest httpRequest) {
		Long userId = RequestParser.extractUserId(httpRequest);
		return placeService.getAllPlaces(userId);
	}

	// task: product 카테고리에 맞는 장소 리스트 조회
	@GetMapping("/searchByProduct")
	public BaseResponse<List<PlacesResponse>> getLocationOptionsForItem(@RequestParam Long productId, HttpServletRequest httpRequest) {
		Long userId = RequestParser.extractUserId(httpRequest);
		return placeService.getLocationOptionsForItem(productId, userId);
	}

	// task: 장소 디테일 조회
	@GetMapping("/placeDetail")
	public BaseResponse<PlaceDetailResponse> getPlaceDetailsForItem(@RequestParam Long placeId) {
		return placeService.getPlaceDetail(placeId);
	}

	// task: 장소 등록
	@PostMapping
	public BaseResponse<String> registerPlace(@Valid @ModelAttribute PlaceRequest placeRequest,  HttpServletRequest httpRequest) {
		Long userId = RequestParser.extractUserId(httpRequest);
		return placeService.createPlace(placeRequest, userId);
	}

	// task: 장소 수정
	@PutMapping
	public BaseResponse<String> updatePlace(@Valid @ModelAttribute PlaceUpdateRequest placeRequest, HttpServletRequest httpRequest) {
		Long userId = RequestParser.extractUserId(httpRequest);
		return placeService.updatePlace(placeRequest, userId);
	}
}
