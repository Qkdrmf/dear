package com.dearbella.server.controller;

import com.dearbella.server.domain.Banner;
import com.dearbella.server.dto.response.banner.BannerDetailResponseDto;
import com.dearbella.server.dto.response.banner.BannerResponseDto;
import com.dearbella.server.dto.response.hospital.HospitalResponseDto;
import com.dearbella.server.dto.response.review.ReviewDetailResponseDto;
import com.dearbella.server.dto.response.review.ReviewResponseDto;
import com.dearbella.server.service.banner.BannerService;
import com.dearbella.server.service.hospital.HospitalService;
import com.dearbella.server.service.review.ReviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/free")
@RequiredArgsConstructor
@Slf4j
@Api(tags = {"권한 필요 없는 API"})
public class FreeController {
    private final BannerService bannerService;
    private final ReviewService reviewService;
    private final HospitalService hospitalService;

    /**
     * TODO
     * 추천 의사 10명
     * 추천 병원 15개
     * 추천 리뷰 20개
     * 광고 무제한
     * 더보기에서 카테고리별 항목 10개씩
     *
     * */

    /**
     * Banner API
     * */
    @ApiOperation("배너 조회")
    @GetMapping("/banner")
    public ResponseEntity<List<BannerResponseDto>> getBanners(@RequestParam Long location) {
        return ResponseEntity.ok(bannerService.getBanners(location == 1));
    }

    @ApiOperation("배너 상세 조회")
    @GetMapping("/banner/info")
    public ResponseEntity<BannerDetailResponseDto> getBanner(@RequestParam Long bannerId) {
        return ResponseEntity.ok(bannerService.findById(bannerId));
    }

    /**
     * Review Api
     * */
    @ApiOperation("추천 리뷰 조회(카테고리 포함)")
    @GetMapping("/review/search/category")
    public ResponseEntity<Set<ReviewResponseDto>> getReviews(@RequestParam Long category) {
        return ResponseEntity.ok(reviewService.findByCategory(category));
    }

    @ApiOperation("추천 리뷰 조회(검색어)")
    @GetMapping("/review/search/query")
    public ResponseEntity<Set<ReviewResponseDto>> getReviews(@RequestParam String query) {
        return ResponseEntity.ok(reviewService.findByQuery(query));
    }

    @ApiOperation("review 상세 조회")
    @GetMapping("/review/info")
    public ResponseEntity<ReviewDetailResponseDto> getInfo(@RequestParam Long reviewId) {
        return ResponseEntity.ok(reviewService.findById(reviewId));
    }

    @ApiOperation("병원 정보")
    @GetMapping("/hospital")
    private ResponseEntity<List<HospitalResponseDto>> getHospitals(@RequestParam Long category, @RequestParam Long sort) {
        return ResponseEntity.ok(hospitalService.getAll(category, sort));
    }
}
