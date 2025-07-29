package gift.option.service;

import gift.domain.*;
import gift.global.exception.BadRequestEntityException;
import gift.global.exception.NotFoundEntityException;
import gift.member.dto.AuthMember;
import gift.member.service.MemberService;
import gift.option.dto.OptionCreateRequest;
import gift.option.dto.OptionResponse;
import gift.option.dto.OptionUpdateRequest;
import gift.option.repository.OptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OptionServiceV1Test {

    @InjectMocks
    private OptionServiceV1 optionServiceV1;

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private MemberService memberService;


    @Test
    @DisplayName("삭제 성공")
    public void deleteSuccess() {

        // given
        Member member = new Member(1L , "ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
        Product product = new Product(1L, "스윙칩", 3000, "image", member);
        Option option = new Option(1L, "옵션1", 100, product);

        given(optionRepository.findByIdWithProduct(option.getId())).
                willReturn(Optional.of(option));

        // when
        optionServiceV1.deleteById(new AuthMember(member.getEmail(), member.getRole()), option.getId());

        // then
        verify(optionRepository).findByIdWithProduct(option.getId());
        verify(optionRepository).deleteById(option.getId());
        verify(memberService).isOwnerOrAdmin(member.getEmail(), member.getId());
        verifyNoMoreInteractions(optionRepository);
        verifyNoMoreInteractions(memberService);
    }

    @Test
    @DisplayName("삭제 실패 - 존재하지 않는 옵션")
    public void deleteFail1() {

        // given
        given(optionRepository.findByIdWithProduct(any())).
                willReturn(Optional.empty());


        // then
        assertThatThrownBy(()-> optionServiceV1.deleteById(new AuthMember("aa", Role.REGULAR), 1L))
                .isInstanceOf(NotFoundEntityException.class);
        verify(optionRepository).findByIdWithProduct(any());
        verifyNoMoreInteractions(optionRepository);
        verifyNoMoreInteractions(memberService);
    }

    @Test
    @DisplayName("삭제 실패 - 권한 없음")
    public void deleteFail2() {

        // given
        Member member = new Member(1L , "ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
        Product product = new Product(1L, "스윙칩", 3000, "image", member);
        Option option = new Option(1L, "옵션1", 100, product);

        given(optionRepository.findByIdWithProduct(option.getId())).
                willReturn(Optional.of(option));

        willThrow(new BadRequestEntityException("권한 없음"))
                .given(memberService)
                .isOwnerOrAdmin(any(), any());

        // when

        // then
        assertThatThrownBy(()-> optionServiceV1.deleteById(new AuthMember(member.getEmail(), member.getRole()), option.getId()))
                        .isInstanceOf(BadRequestEntityException.class);
        verify(optionRepository).findByIdWithProduct(option.getId());
        verify(memberService).isOwnerOrAdmin(member.getEmail(), member.getId());
        verifyNoMoreInteractions(optionRepository);
        verifyNoMoreInteractions(memberService);
    }

    @Test
    @DisplayName("옵션 수량 수정 성공")
    public void changeQuantitySuccess() {
        // given
        Member member = new Member(1L , "ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
        Product product = new Product(1L, "스윙칩", 3000, "image", member);
        Option option = new Option(1L, "옵션1", 100, product);

        given(optionRepository.findByIdWithProduct(option.getId()))
                .willReturn(Optional.of(option));

        // when
        optionServiceV1.changeQuantity(new AuthMember(member.getEmail(), member.getRole()),option.getId(), new OptionUpdateRequest(10));


        // then
        assertThat(option.getQuantity()).isEqualTo(10);
        verify(optionRepository).findByIdWithProduct(option.getId());
        verify(memberService).isOwnerOrAdmin(member.getEmail(), member.getId());
        verifyNoMoreInteractions(optionRepository);
        verifyNoMoreInteractions(memberService);
    }

    @Test
    @DisplayName("옵션 수량 수정 실패 - 권한 없음")
    public void changeQuantityFail() {
        // given
        Member member = new Member(1L , "ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
        Product product = new Product(1L, "스윙칩", 3000, "image", member);
        Option option = new Option(1L, "옵션1", 100, product);

        given(optionRepository.findByIdWithProduct(option.getId()))
                .willReturn(Optional.of(option));

        willThrow(new BadRequestEntityException("권한 없음"))
                .given(memberService)
                .isOwnerOrAdmin(any(), anyLong());


        // when & then
        assertThatThrownBy(()->optionServiceV1.
                changeQuantity(new AuthMember(member.getEmail(), member.getRole()),option.getId(),
                        new OptionUpdateRequest(10)))
                .isInstanceOf(BadRequestEntityException.class);

        verify(optionRepository).findByIdWithProduct(option.getId());
        verify(memberService).isOwnerOrAdmin(member.getEmail(), member.getId());
        verifyNoMoreInteractions(optionRepository);
        verifyNoMoreInteractions(memberService);
    }

    @Test
    @DisplayName("상품 아이디로 조회")
    void findOptionByProductId() {
        // given
        Member member = new Member(1L , "ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
        Product product = new Product(1L, "스윙칩", 3000, "image", member);
        Option option = new Option(1L, "옵션1", 100, product);

        given(optionRepository.findByProductId(product.getId()))
                .willReturn(List.of(option));

        // when
        List<OptionResponse> result = optionServiceV1.findByProduct(product);

        // then
        assertThat(result.size()).isEqualTo(1);
        verify(optionRepository).findByProductId(product.getId());
        verifyNoMoreInteractions(optionRepository);
    }

    @Test
    @DisplayName("옵션 추가 실패 - 중복된 옵션 이름")
    void addOptionFail() {
        // given
        Member member = new Member(1L , "ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
        Product product = new Product(1L, "스윙칩", 3000, "image", member);

        given(optionRepository.countByProductIdAndOptionNames(any(), any()))
                .willReturn(1L);

        // when & then
        assertThatThrownBy(()->optionServiceV1.save(List.of(new OptionCreateRequest("옵션1", 30)), product, new AuthMember(member.getEmail(), member.getRole())))
                .isInstanceOf(BadRequestEntityException.class);
        verify(memberService).isOwnerOrAdmin(any(), any());
        verify(optionRepository).countByProductIdAndOptionNames(any(), any());
        verifyNoMoreInteractions(optionRepository, memberService);
    }

}