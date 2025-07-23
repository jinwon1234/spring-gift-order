package gift.member.service;

import gift.domain.Member;
import gift.member.dto.MemberCreateDto;
import gift.member.dto.MemberResponse;
import gift.member.dto.MemberUpdateReqForAdmin;
import gift.member.dto.MemberUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberService {

    Long save(MemberCreateDto memberCreateDto);

    void changePassword(String email, MemberUpdateRequest memberUpdateRequest);

    void updateMemberForAdmin(Long id, MemberUpdateReqForAdmin memberUpdateReqForAdmin);

    MemberResponse findById(Long id);

    List<MemberResponse> findAll();

    Page<MemberResponse> findAllByPage(Pageable pageable);

    void deleteByEmail(String email);

    void deleteById(Long id);

    void validateToken(String email, String role);

    MemberResponse validate(String email, String password);

    Member findByEmail(String email);

    void isOwnerOrAdmin(String email, Long memberId);
}
