package com.dearbella.server.service.member;

import com.dearbella.server.domain.*;
import com.dearbella.server.dto.request.admin.AdminCreateRequestDto;
import com.dearbella.server.dto.response.login.LoginResponseDto;
import com.dearbella.server.exception.hospital.HospitalIdNotFoundException;
import com.dearbella.server.exception.member.MemberIdNotFoundException;
import com.dearbella.server.exception.member.MemberLoginEmailNotFoundException;
import com.dearbella.server.repository.AdminRepository;
import com.dearbella.server.repository.HospitalRepository;
import com.dearbella.server.repository.MemberRepository;
import com.dearbella.server.repository.TokenRepository;
import com.dearbella.server.util.JwtUtil;
import com.dearbella.server.vo.GoogleIdTokenVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dearbella.server.config.MapperConfig.modelMapper;

@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final TokenRepository tokenRepository;
    private final AdminRepository adminRepository;

    @Override
    @Transactional
    public LoginResponseDto signUp(final GoogleIdTokenVo idTokenVo) {
        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();

        Member save = memberRepository.save(
                Member.builder()
                        .memberId(System.currentTimeMillis())
                        .loginEmail(idTokenVo.getEmail())
                        .signOut(false)
                        .profileImg(idTokenVo.getPicture())
                        .phone(null)
                        .nickname(idTokenVo.getName())
                        .authorities(List.of(authority))
                        .ban(false)
                        .build()
        );

        log.info("member: {}", save);

        final Token token = tokenRepository.save(
                Token.builder()
                        .memberId(save.getMemberId())
                        .accessToken(JwtUtil.createJwt(save.getMemberId()))
                        .accessTokenExpiredAt(LocalDate.now().plusDays(10))
                        .refreshToken(JwtUtil.createRefreshToken(save.getMemberId()))
                        .refreshTokenExpiredAt(LocalDate.now().plusYears(1))
                        .build()
        );

        return LoginResponseDto.of(save, token);
    }

    @Override
    @Transactional
    public LoginResponseDto signIn(final GoogleIdTokenVo idTokenVo) {
        LoginResponseDto responseDto = modelMapper.map(memberRepository.findMemberByLoginEmail(idTokenVo.getEmail()).orElseThrow(
                () -> new MemberLoginEmailNotFoundException(idTokenVo.getEmail())
        ), LoginResponseDto.class);

        final Token token = tokenRepository.findById(responseDto.getMemberId()).orElseThrow(
                () -> new MemberIdNotFoundException(responseDto.getMemberId().toString())
        );

        responseDto.setAccessToken(token.getAccessToken());
        responseDto.setRefreshToken(token.getRefreshToken());

        return responseDto;
    }

    @Override
    public Boolean isMember(final String email) {
        final Optional<Member> response = memberRepository.findMemberByLoginEmail(email);

        return response.isEmpty();
    }

    @Override
    public Member findById() {
        return memberRepository.findById(JwtUtil.getMemberId()).orElseThrow(
                () -> new MemberIdNotFoundException(JwtUtil.getMemberId().toString())
        );
    }

    @Override
    @Transactional
    public Admin createAdmin(AdminCreateRequestDto dto) {
        Long memberId = System.currentTimeMillis();

        Member roleAdmin = memberRepository.save(
                Member.builder()
                        .memberId(memberId)
                        .authorities(List.of(
                                Authority.builder()
                                        .authorityName("ROLE_ADMIN")
                                        .build()
                        ))
                        .ban(false)
                        .signOut(false)
                        .phone(null)
                        .profileImg("https://dearbella-bucket.s3.ap-northeast-2.amazonaws.com/profile.png")
                        .loginEmail(dto.getUserId())
                        .nickname(dto.getHospitalName())
                        .build()
        );

        tokenRepository.save(
                Token.builder()
                        .memberId(memberId)
                        .accessToken(JwtUtil.createJwt(memberId))
                        .refreshToken(JwtUtil.createRefreshToken(memberId))
                        .accessTokenExpiredAt(LocalDate.now().plusYears(1L))
                        .refreshTokenExpiredAt(LocalDate.now().plusYears(1L))
                        .build()
        );

        return adminRepository.save(
                Admin.builder()
                        .memberId(memberId)
                        .adminId(dto.getUserId())
                        .adminPassword(dto.getPassword())
                        .hospitalId(dto.getHospitalId())
                        .hospitalName(dto.getHospitalName())
                        .build()
        );
    }
}
