package com.dearbella.server.service.hospital;

import com.dearbella.server.domain.*;
import com.dearbella.server.dto.request.hospital.HospitalAddRequestDto;
import com.dearbella.server.dto.response.doctor.DoctorResponseDto;
import com.dearbella.server.dto.response.hospital.HospitalDetailResponseDto;
import com.dearbella.server.dto.response.hospital.HospitalResponseDto;
import com.dearbella.server.dto.response.review.ReviewResponseDto;
import com.dearbella.server.exception.banner.BannerInfraNotFoundException;
import com.dearbella.server.exception.doctor.DoctorByHospitalNameNotFoundException;
import com.dearbella.server.exception.hospital.HospitalIdNotFoundException;
import com.dearbella.server.exception.hospital.HospitalResponseNullException;
import com.dearbella.server.repository.*;
import com.dearbella.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;

import static com.dearbella.server.config.MapperConfig.modelMapper;

@Slf4j
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {
    private final HospitalRepository hospitalRepository;
    private final ImageRepository imageRepository;
    private final InfraRepository infraRepository;
    private final HospitalMemberRepository hospitalMemberRepository;
    private final DoctorRepository doctorRepository;
    private final ReviewRepository reviewRepository;
    private final DoctorMemberRepository doctorMemberRepository;

    @Override
    public Hospital addHospital(final HospitalAddRequestDto dto, List<String> befores, List<String> afters, List<String> banners)  {
        List<Image> beforeImages = new ArrayList<>();
        List<Image> afterImages = new ArrayList<>();
        List<Image> bannerImages = new ArrayList<>();
        Set<Infra> infraList = new HashSet<>();

        for(String image: befores) {
            beforeImages.add(
                    imageRepository.save(
                        Image.builder()
                                .imageUrl(image)
                              .memberId(JwtUtil.getMemberId())
                              .build()
                    )
            );
        }

        for(String image: afters) {
            afterImages.add(
                    imageRepository.save(
                            Image.builder()
                                    .imageUrl(image)
                                    .memberId(JwtUtil.getMemberId())
                                    .build()
                    )
            );
        }

        for(String image: banners) {
            bannerImages.add(
                    imageRepository.save(
                            Image.builder()
                                    .imageUrl(image)
                                    .memberId(JwtUtil.getMemberId())
                                    .build()
                    )
            );
        }

        for(Long tag: dto.getInfras()) {
            infraList.add(
                    infraRepository.findById(tag).orElseThrow(
                            () -> new BannerInfraNotFoundException(tag)
                    )
            );
        }

        return hospitalRepository.save(
                Hospital.builder()
                        .adminId(JwtUtil.getMemberId())
                        .after(afterImages)
                        .before(beforeImages)
                        .banners(bannerImages)
                        .hospitalName(dto.getName())
                        .description(dto.getDescription())
                        .hospitalLocation(dto.getLocation())
                        .description(dto.getDescription())
                        .hospitalVideoLink(dto.getLink())
                        .sequence(dto.getSequence())
                        .totalRate(0F)
                        .infras(infraList)
                        .anesthesiologist(0L)
                        .plasticSurgeon(0L)
                        .dermatologist(0L)
                        .deleted(false)
                        .build()
        );
    }

    @Override
    @Transactional
    public List<HospitalResponseDto> getAll(final Long category, final Long sort) {
        List<Hospital> hospitals;
        List<HospitalResponseDto> responseDtos = new ArrayList<>();

        if(sort == 0) {
            hospitals = hospitalRepository.findAll(Sort.by(Sort.Direction.DESC, "totalRate"));
        }
        else
            hospitals = hospitalRepository.findAll(Sort.by(Sort.Direction.DESC, "viewNum"));

        String accessToken = JwtUtil.isExistAccessToken();
        Long memberId;

        if(accessToken == null)
            memberId = 0L;
        else
            memberId = JwtUtil.getMemberId(accessToken);

        for(Hospital hospital: hospitals) {
            Boolean isEmpty = true;
            if(memberId > 0) {
                isEmpty = hospitalMemberRepository.findByHospitalIdAndMemberId(
                        hospital.getHospitalId(),
                        memberId).isEmpty();
            }

            if(category == 0) {
                responseDtos.add(
                        HospitalResponseDto.builder()
                                .hospitalId(hospital.getHospitalId())
                                .hospitalName(hospital.getHospitalName())
                                .isMine(isEmpty ? false : true)
                                .location(hospital.getHospitalLocation())
                                .rate(hospital.getTotalRate())
                                .reviewNum(Long.valueOf(reviewRepository.findByHospitalId(hospital.getHospitalId()).size()))
                                .build()
                );
            }
            else
            {
                final List<Doctor> doctors = doctorRepository.findByHospitalName(hospital.getHospitalName());

                for(Doctor doctor: doctors) {
                    if(doctor.contains(category)) {
                        responseDtos.add(
                                HospitalResponseDto.builder()
                                        .hospitalId(hospital.getHospitalId())
                                        .hospitalImage(hospital.getBanners().get(0).getImageUrl())
                                        .hospitalName(hospital.getHospitalName())
                                        .isMine(isEmpty ? false : true)
                                        .location(hospital.getHospitalLocation())
                                        .rate(hospital.getTotalRate())
                                        .reviewNum(Long.valueOf(reviewRepository.findByHospitalId(hospital.getHospitalId()).size()))
                                        .build()
                        );
                    }
                }
            }
        }

        return responseDtos;
    }

    @Override
    @Transactional
    public HospitalDetailResponseDto findById(final Long id) {
        HospitalDetailResponseDto response;
        final Hospital hospital = hospitalRepository.findById(id).orElseThrow(
                () -> new HospitalIdNotFoundException(id)
        );
        String accessToken = JwtUtil.isExistAccessToken();

        Long memberId;

        if(accessToken == null) {
            memberId = 0L;
        }
        else
        {
            memberId = JwtUtil.getMemberId(accessToken);
        }

        Boolean isEmpty = hospitalMemberRepository.findByHospitalIdAndMemberId(hospital.getHospitalId(), memberId).isEmpty();

        final List<Doctor> doctors = doctorRepository.findByHospitalName(hospital.getHospitalName());
        List<DoctorResponseDto> doctorResponseDtos = new ArrayList<>();

        for(Doctor doctor: doctors) {
            Boolean empty = doctorMemberRepository.findByDoctorIdAndMemberId(doctor.getDoctorId(), memberId).isEmpty();

            doctorResponseDtos.add(
                    DoctorResponseDto.builder()
                            .doctorId(doctor.getDoctorId())
                            .doctorImage(doctor.getDoctorImage())
                            .doctorName(doctor.getDoctorName())
                            .intro(doctor.getDescription())
                            .rate(doctor.getTotalRate())
                            .parts(doctor.getCategories())
                            .reviewNum(Long.valueOf(reviewRepository.findByDoctorId(doctor.getDoctorId()).size()))
                            .isMine(empty ? false : true)
                            .hospitalName(hospital.getHospitalName())
                            .build()
            );
        }

        List<ReviewResponseDto> reviewResponseDtos = new ArrayList<>();
        final List<Review> byHospitalId = reviewRepository.findByHospitalId(hospital.getHospitalId());

        for(Review review: byHospitalId) {
            reviewResponseDtos.add(
                    ReviewResponseDto.builder()
                            .reviewId(review.getReviewId())
                            .title(review.getTitle())
                            .rate(review.getRate())
                            .build()
            );
        }

        response = HospitalDetailResponseDto.builder()
                .hospitalId(hospital.getHospitalId())
                .banners(hospital.getBanners())
                .hospitalName(hospital.getHospitalName())
                .location(hospital.getHospitalLocation())
                .isMine(isEmpty ? false : true)
                .rate(hospital.getTotalRate())
                .reviewNum(Long.valueOf(reviewRepository.findByHospitalId(hospital.getHospitalId()).size()))
                .intro(hospital.getDescription())
                .infras(hospital.getInfras())
                .doctors(doctorResponseDtos)
                .befores(hospital.getBefore())
                .afters(hospital.getAfter())
                .reviews(reviewResponseDtos)
                .build();

        return response;
    }

    @Override
    @Transactional
    public Set<HospitalResponseDto> findByQuery(final String query) {
        Set<HospitalResponseDto> responseDtoList = new HashSet<>();

        for (Hospital hospital : hospitalRepository.findByHospitalNameContainingAndDeletedFalse(query)) {
            final Hospital h = hospitalRepository.findById(hospital.getHospitalId()).orElseThrow(
                    () -> new HospitalIdNotFoundException(hospital.getHospitalId())
            );
            final List<Review> byHospitalId = reviewRepository.findByHospitalId(hospital.getHospitalId());

            responseDtoList.add(
                    HospitalResponseDto.builder()
                            .hospitalImage(h.getBanners().size() > 0 ? h.getBanners().get(0).getImageUrl() : null)
                            .reviewNum(Long.valueOf(byHospitalId.size()))
                            .rate(hospital.getTotalRate())
                            .location(hospital.getHospitalLocation())
                            .isMine(false)
                            .hospitalId(h.getHospitalId())
                            .hospitalName(hospital.getHospitalName())
                            .build()
            );
        }

        for (Hospital hospital : hospitalRepository.findByDescriptionContainingAndDeletedFalse(query)) {
            final Hospital h = hospitalRepository.findById(hospital.getHospitalId()).orElseThrow(
                    () -> new HospitalIdNotFoundException(hospital.getHospitalId())
            );
            final List<Review> byHospitalId = reviewRepository.findByHospitalId(hospital.getHospitalId());

            responseDtoList.add(
                    HospitalResponseDto.builder()
                            .hospitalImage(h.getBanners().size() > 0 ? h.getBanners().get(0).getImageUrl() : null)
                            .reviewNum(Long.valueOf(byHospitalId.size()))
                            .rate(hospital.getTotalRate())
                            .location(hospital.getHospitalLocation())
                            .isMine(false)
                            .hospitalId(h.getHospitalId())
                            .hospitalName(hospital.getHospitalName())
                            .build()
            );
        }

        if(responseDtoList.size() == 0)
            throw new HospitalResponseNullException();

        return responseDtoList;
    }
}
