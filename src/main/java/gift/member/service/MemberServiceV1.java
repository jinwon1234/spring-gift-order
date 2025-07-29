package gift.member.service;

import gift.domain.Member;
import gift.domain.Role;
import gift.domain.Social;
import gift.global.exception.AuthorizationException;
import gift.global.exception.BadRequestEntityException;
import gift.global.exception.DuplicateEntityException;
import gift.global.exception.NotFoundEntityException;
import gift.member.dto.MemberCreateDto;
import gift.member.dto.MemberResponse;
import gift.member.dto.MemberUpdateReqForAdmin;
import gift.member.dto.MemberUpdateRequest;
import gift.member.repository.MemberRepository;
import gift.oauth2.dto.SocialLoginRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Service
public class MemberServiceV1 implements MemberService{

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public MemberServiceV1(MemberRepository memberRepository, BCryptPasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Long save(MemberCreateDto memberCreateDto) {

        if (!memberCreateDto.getConfirmPassword().equals(memberCreateDto.getPassword()))
            throw new BadRequestEntityException("비밀번호와 확인 비밀번호가 다릅니다.");

        memberRepository.findByEmail(memberCreateDto.getEmail())
                .ifPresent(member -> {
                    throw new DuplicateEntityException(member.getEmail() + "은 이미 존재하는 이메일 입니다.");
                });

        String encodedPassword = passwordEncoder.encode(memberCreateDto.getPassword());

        Member saved = memberRepository.save(new Member(memberCreateDto.getEmail(), encodedPassword,
                Role.valueOf(memberCreateDto.getRole()), Social.NONE));

        return saved.getId();
    }

    @Override
    public Member socialLogin(SocialLoginRequest socialLoginRequest) {

        Optional<Member> findMember = memberRepository.findByEmail(socialLoginRequest.email());

        if (findMember.isPresent()) return findMember.get();

        String password = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(password);

        Member saved = memberRepository.save(new Member(socialLoginRequest.email(), encodedPassword, Role.REGULAR,socialLoginRequest.social()));

        return saved;
    }

    @Override
    public void changePassword(String email, MemberUpdateRequest memberUpdateRequest) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundEntityException("존재하는 회원이 아닙니다."));

        if (!passwordEncoder.matches(memberUpdateRequest.getPassword(), member.getPassword()))
            throw new AuthorizationException("비밀번호가 다릅니다.");


        if (!memberUpdateRequest.getConfirmPassword().equals(memberUpdateRequest.getNewPassword()))
            throw new BadRequestEntityException("새로운 비밀번호가 확인 비밀번호와 일치하지 않습니다.");

        String encodedPassword =  passwordEncoder.encode(memberUpdateRequest.getNewPassword());

        member.changePassword(encodedPassword);
    }

    @Override
    public void updateMemberForAdmin(Long id, MemberUpdateReqForAdmin memberUpdateReqForAdmin) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundEntityException("존재하는 회원이 아닙니다."));

        if (!passwordEncoder.matches(memberUpdateReqForAdmin.getPassword(), member.getPassword()))
            throw new AuthorizationException("비밀번호가 다릅니다.");


        if (!memberUpdateReqForAdmin.getConfirmPassword().equals(memberUpdateReqForAdmin.getNewPassword()))
            throw new BadRequestEntityException("새로운 비밀번호가 확인 비밀번호와 일치하지 않습니다.");

        String encodedPassword =  passwordEncoder.encode(memberUpdateReqForAdmin.getNewPassword());

        member.changePassword(encodedPassword);
        member.changeRole(Role.valueOf(memberUpdateReqForAdmin.getRole()));
    }

    @Override
    public MemberResponse findById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundEntityException("존재하는 회원이 아닙니다."));
        return new MemberResponse(member.getId(), member.getEmail(),member.getRole());
    }

    @Override
    public List<MemberResponse> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(m-> new MemberResponse(m.getId(), m.getEmail(),m.getRole()))
                .toList();
    }

    @Override
    public Page<MemberResponse> findAllByPage(Pageable pageable) {
        return memberRepository.findAllWithPage(pageable)
                .map(m-> new MemberResponse(m.getId(), m.getEmail(),m.getRole()));
    }

    public void deleteById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundEntityException("존재하는 회원이 아닙니다."));


        memberRepository.deleteById(member.getId());
    }

    @Override
    public void deleteByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundEntityException("존재하는 회원이 아닙니다."));


        memberRepository.deleteById(member.getId());
    }

    @Override
    public void validateToken(String email, String role) {
        Member findMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundEntityException("존재하는 회원이 아닙니다."));

        if (!findMember.getRole().toString().equals(role)) {
            throw new NotFoundEntityException("존재하는 회원이 아닙니다.");
        }
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundEntityException("존재하는 회원이 아닙니다."));
    }

    @Override
    public void isOwnerOrAdmin(String loginEmail, Long ownerId) {
        Member loginMember = memberRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new NotFoundEntityException("존재하는 회원이 아닙니다."));

        if (loginMember.getRole().equals(Role.ADMIN)) return;

        if (!loginMember.getId().equals(ownerId)) throw new BadRequestEntityException("본인의 상품이 아닙니다.");
    }

    @Override
    public MemberResponse validate(String email, String encodedPassword) {

        Member findMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundEntityException("존재하는 회원이 아닙니다"));

        if (!passwordEncoder.matches(encodedPassword, findMember.getPassword()))
            throw new AuthorizationException("비밀번호가 다릅니다.");

        return new  MemberResponse(findMember.getId(), findMember.getEmail(), findMember.getRole());
    }
}
