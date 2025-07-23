package gift.member.controller;

import gift.global.annotation.OnlyForAdmin;
import gift.member.annotation.MyAuthenticalPrincipal;
import gift.member.dto.*;
import gift.member.service.MemberService;
import gift.util.LocationGenerator;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping()
    public ResponseEntity<Void> joinMember(@Valid @RequestBody MemberCreateRequest memberCreateRequest) {

        Long id = memberService.save(memberCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).
                location(LocationGenerator.generate(id)).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> withDraw(@MyAuthenticalPrincipal AuthMember authMember) {

        memberService.deleteByEmail(authMember.getEmail());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping
    public ResponseEntity<Void> changePassword(@MyAuthenticalPrincipal AuthMember authMember, @Valid @RequestBody MemberUpdateRequest memberUpdateRequest) {

        memberService.changePassword(authMember.getEmail(),memberUpdateRequest);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @OnlyForAdmin
    @GetMapping()
    public ResponseEntity<Page<MemberResponse>> getMembers(@PageableDefault(page = 0, size=10, sort="id", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<MemberResponse> response = memberService.findAllByPage(pageable);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @OnlyForAdmin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {

        memberService.deleteById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @OnlyForAdmin
    @PostMapping("/admin")
    public ResponseEntity<Void> addMemberForAdmin(@Valid @RequestBody MemberCreateReqForAdmin memberCreateReqForAdmin) {
        Long id = memberService.save(memberCreateReqForAdmin);

        return ResponseEntity.status(HttpStatus.CREATED).
                location(LocationGenerator.generate(id)).build();
    }

    @OnlyForAdmin
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long id) {
        MemberResponse memberResponse = memberService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(memberResponse);
    }

    @OnlyForAdmin
    @PutMapping("/{id}")
    public ResponseEntity<Void> editMemberForAdmin(@PathVariable Long id, @Valid @RequestBody MemberUpdateReqForAdmin memberUpdateReqForAdmin) {
        memberService.updateMemberForAdmin(id, memberUpdateReqForAdmin);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
