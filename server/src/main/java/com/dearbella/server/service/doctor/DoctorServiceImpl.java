package com.dearbella.server.service.doctor;

import com.dearbella.server.domain.*;
import com.dearbella.server.dto.request.doctor.DoctorAddRequestDto;
import com.dearbella.server.enums.doctor.CategoryEnum;
import com.dearbella.server.exception.doctor.CategoryNotFoundException;
import com.dearbella.server.repository.*;
import com.dearbella.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepository doctorRepository;
    private final CareerRepository careerRepository;
    private final IntroLinkRepository introLinkRepository;
    private final CategoryRepository categoryRepository;
    private final HospitalDoctorRepository hospitalDoctorRepository;

    /**
     * TODO
     * total rate
     * */

    @Override
    @Transactional
    public Doctor addDoctor(final DoctorAddRequestDto dto, String image) {
        List<Career> careers = new ArrayList<>();
        List<IntroLink> videos = new ArrayList<>();
        List<Category> categories = new ArrayList<>();

        for(int i = 0; i < dto.getCareer().size(); i++) {
            careers.add(
                    careerRepository.save(
                            Career.builder()
                                    .careerName(dto.getCareer().get(i))
                                    .careerDate(dto.getDate().get(i))
                                    .build()
                    )
            );
        }

        for(String video: dto.getVideoLinks()) {
            videos.add(
                    introLinkRepository.save(
                            IntroLink.builder()
                                    .linkUrl(video)
                                    .build()
                    )
            );
        }

        for(Long tag: dto.getTags()) {
            categories.add(
                    categoryRepository.findById(tag).orElseThrow(
                            () -> new CategoryNotFoundException(tag)
                    )
            );
        }

        final Doctor save = doctorRepository.save(
                Doctor.builder()
                        .doctorName(dto.getDoctorName())
                        .doctorImage(image)
                        .hospitalName(dto.getHospitalName())
                        .description(dto.getDescription())
                        .adminId(JwtUtil.getMemberId())
                        .career(careers)
                        .categories(categories)
                        .links(videos)
                        .sequence(dto.getSequence())
                        .totalRate(0.0F)
                        .build()
        );

        hospitalDoctorRepository.save(
                HospitalDoctor.builder()
                        .doctorId(save.getDoctorId())
                        .hospitalName(save.getHospitalName())
                        .build()
        );

        return save;
    }
}
