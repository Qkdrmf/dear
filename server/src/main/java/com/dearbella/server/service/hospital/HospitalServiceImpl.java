package com.dearbella.server.service.hospital;

import com.dearbella.server.domain.Hospital;
import com.dearbella.server.domain.Image;
import com.dearbella.server.domain.Infra;
import com.dearbella.server.dto.request.hospital.HospitalAddRequestDto;
import com.dearbella.server.exception.banner.BannerInfraNotFoundException;
import com.dearbella.server.repository.HospitalRepository;
import com.dearbella.server.repository.ImageRepository;
import com.dearbella.server.repository.InfraRepository;
import com.dearbella.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {
    private final HospitalRepository hospitalRepository;
    private final ImageRepository imageRepository;
    private final InfraRepository infraRepository;

    /**
     * TODO
     * 병원 시설 저장
     * */

    @Override
    public Hospital addHospital(final HospitalAddRequestDto dto, List<String> befores, List<String> afters)  {
        Set<Image> beforeImages = new HashSet<>();
        Set<Image> afterImages = new HashSet<>();
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
                        .build()
        );
    }
}
