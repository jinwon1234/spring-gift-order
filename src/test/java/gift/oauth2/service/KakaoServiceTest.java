package gift.oauth2.service;

import gift.domain.KakaoToken;
import gift.domain.Member;
import gift.domain.Role;
import gift.domain.Social;
import gift.global.exception.KakaoKAuthException;
import gift.global.exception.KakaoKApiException;
import gift.jwt.JWTUtil;
import gift.member.service.MemberService;
import gift.oauth2.dto.KakaoTokenResponse;
import gift.oauth2.dto.KakaoUserInfoResponse;
import gift.oauth2.properties.KakaoProperties;
import gift.oauth2.repository.KakaoTokenRepository;
import gift.order.dto.KakaoOrderMessageTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;


import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(KakaoService.class)
class KakaoServiceTest {

    @Autowired
    private KakaoService kakaoService;

    @Autowired
    private MockRestServiceServer mockServer;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private KakaoTokenRepository kakaoTokenRepository;

    @MockitoBean
    private KakaoProperties kakaoProperties;

    @BeforeEach
    void setUp() {
        given(kakaoProperties.getkApiUri()).willReturn("https://kapi.kakao.com");
        given(kakaoProperties.getkAuthUri()).willReturn("https://kauth.kakao.com");
    }

    @Test
    @DisplayName("토큰 요청 성공")
    void getTokenSuccess() {

        // given
        String expectedResult = """
                                {
                                "token_type": "bearer",
                                "access_token": "accessToken",
                                "id_token": "idToken",
                                "expires_in": 43199,
                                "refresh_token": "refreshToken",
                                "refresh_token_expires_in": 518400,
                                "scope": "account_email profile"
                                }
                                """;
        mockServer
                .expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andRespond(withSuccess(expectedResult, MediaType.APPLICATION_JSON));

        // when

        KakaoTokenResponse token = kakaoService.getToken("code").get();

        // then
        assertSoftly(
                softly-> {
                    softly.assertThat(token.token_type()).isEqualTo("bearer");
                    softly.assertThat(token.access_token()).isEqualTo("accessToken");
                    softly.assertThat(token.id_token()).isEqualTo("idToken");
                    softly.assertThat(token.expires_in()).isEqualTo(43199);
                    softly.assertThat(token.refresh_token_expires_in()).isEqualTo(518400);
                    softly.assertThat(token.refresh_token()).isEqualTo("refreshToken");
                    softly.assertThat(token.scope()).isEqualTo("account_email profile");
                }
        );
    }

    @Test
    @DisplayName("토큰 요청 실패")
    void getTokenFail() {


        // given
        String errorResponse = """
                {
                    "error" : "KOE001",
                    "error_description" : "잘못된 형식의 요청인 경우"
                }
                """;

        mockServer.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andRespond(
                        withStatus(HttpStatus.FORBIDDEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(errorResponse)
                );

        // when & then
        assertThatThrownBy(()->kakaoService.getToken("code"))
                .isInstanceOf(KakaoKAuthException.class)
                .satisfies((ex)-> {
                    KakaoKAuthException exception = (KakaoKAuthException) ex;
                    assertSoftly(softly -> {
                        softly.assertThat(exception.getkAuthExceptionResponse().error())
                                .isEqualTo("KOE001");
                        softly.assertThat(exception.getkAuthExceptionResponse().error_description()).isEqualTo("잘못된 형식의 요청인 경우");
                    });
                });
    }

    @Test
    @DisplayName("유저 정보 반환 성공")
    void getUserInfo() {
        // given
        String expectedResult = """
                {
                    "id":123456789,
                    "connected_at": "2022-04-11T01:45:28Z",
                    "kakao_account": {
                        "profile_nickname_needs_agreement": false,
                        "profile_image_needs_agreement": false,
                        "profile": {
                            "nickname": "홍길동",
                            "thumbnail_image_url": "http://yyy.kakao.com/.../img_110x110.jpg",
                            "profile_image_url": "http://yyy.kakao.com/dn/.../img_640x640.jpg",
                            "is_default_image":false,
                            "is_default_nickname": false
                        },
                        "name_needs_agreement":false,
                        "name":"홍길동",
                        "email_needs_agreement":false,
                        "is_email_valid": true,
                        "is_email_verified": true,
                        "email": "sample@sample.com"
                    }
                }
                """;

        mockServer.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andRespond(withSuccess(expectedResult, MediaType.APPLICATION_JSON));

        // when
        KakaoUserInfoResponse userInfo = kakaoService.getUserInfo(new KakaoTokenResponse("temp", "temp", "temp", 30L,
                "temp", 30L, "temp")).get();

        // then
        assertSoftly(softly-> {
            softly.assertThat(userInfo.id()).isEqualTo(123456789L);
            softly.assertThat(userInfo.kakao_account().email()).isEqualTo("sample@sample.com");
            softly.assertThat(userInfo.kakao_account().email_needs_agreement()).isEqualTo(false);
            softly.assertThat(userInfo.kakao_account().is_email_valid()).isEqualTo(true);
            softly.assertThat(userInfo.kakao_account().is_email_verified()).isEqualTo(true);
        });

    }

    @Test
    @DisplayName("유저 정보 반환 실패")
    void getUserInfoFail() {

        // given
        String errorResponse = """
        {
            "msg": "[spring-gift] App disabled [talk_message] scopes for [TALK_MEMO_DEFAULT_SEND] API on developers.kakao.com. Enable it first.",
            "code": -3
        }
    """;

        mockServer.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andRespond(
                        withStatus(HttpStatus.BAD_REQUEST)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(errorResponse)
                );

        // when & then
        assertThatThrownBy(()->kakaoService.getUserInfo(new KakaoTokenResponse("temp", "temp", "temp", 30L,
                "temp", 30L, "temp")).get())
                .isInstanceOf(KakaoKApiException.class)
                .satisfies(ex-> {
                    KakaoKApiException exception = (KakaoKApiException) ex;
                    assertSoftly(softly -> {
                        softly.assertThat(exception.getkApiExceptionResponse().msg())
                                .isEqualTo("[spring-gift] App disabled [talk_message] scopes for [TALK_MEMO_DEFAULT_SEND] API on developers.kakao.com. Enable it first.");
                        softly.assertThat(exception.getkApiExceptionResponse().code())
                                .isEqualTo(-3);
                    });
                });
    }

    @Test
    @DisplayName("메시지 보내기 성공")
    void sendMessageSuccess() {

        // given
        Member member = new Member("user@email.com", "Qwer1234", Role.REGULAR, Social.KAKAO);
        KakaoToken kakaoToken = new KakaoToken("accessToken", "refreshToken", member);

        given(kakaoTokenRepository.findByMemberId(any()))
                .willReturn(Optional.of(kakaoToken));

        mockServer.expect(requestTo(kakaoProperties.getkApiUri() + "/v2/api/talk/memo/send"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON));


        // when & then
        kakaoService.sendOrderMessage(new KakaoOrderMessageTemplate
                ("상품1", "옵션1", 100, 20,
                        "메시지", 2000), kakaoToken);
    }


    @Test
    @DisplayName("메시지 보내기 실패 - 토큰 재발급 실패")
    void sendMessageFail()  {

        // given
        String errorResponse1 = """
        {
            "msg": "액세스 토큰 만료",
            "code": -401
        }
    """;

        String errorResponse2 = """
                {
                    "error" : "KOE001",
                    "error_description" : "잘못된 형식의 요청인 경우"
                }
                """;

        Member member = new Member("user@email.com", "Qwer1234", Role.REGULAR, Social.KAKAO);
        KakaoToken kakaoToken = new KakaoToken("accessToken", "refreshToken", member);

        given(kakaoTokenRepository.findByMemberId(any()))
                .willReturn(Optional.of(kakaoToken));

        mockServer.expect(requestTo(kakaoProperties.getkApiUri() + "/v2/api/talk/memo/send"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorResponse1));


        mockServer.expect(requestTo(kakaoProperties.getkAuthUri() + "/oauth/token"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorResponse2));

        // when & then
        assertThatThrownBy(()->kakaoService.sendOrderMessage(new KakaoOrderMessageTemplate
                ("상품1", "옵션1", 100, 20,
                        "메시지", 2000), kakaoToken)
        ).isInstanceOf(KakaoKAuthException.class);

    }
}