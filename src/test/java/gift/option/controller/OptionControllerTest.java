package gift.option.controller;

import gift.domain.Member;
import gift.domain.Option;
import gift.domain.Product;
import gift.domain.Role;
import gift.jwt.JWTUtil;
import gift.member.repository.MemberRepository;
import gift.option.dto.OptionCreateListRequest;
import gift.option.dto.OptionCreateRequest;
import gift.option.dto.OptionResponse;
import gift.option.dto.OptionUpdateRequest;
import gift.option.repository.OptionRepository;
import gift.product.repository.ProductRepository;
import gift.wishproduct.repository.WishProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OptionControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private WishProductRepository wishProductRepository;

    @Autowired
    private JWTUtil jwtUtil;

    private Member member;
    private Product product;

    RestClient restClient;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR));
        Product save = productRepository.save(new Product("스윙칩", 3000, "data:image/~base64", member));
        product = save;

        String token = jwtUtil
                .createJWT(member.getEmail(), member.getRole().toString(), 1000 * 60L);

        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/options")
                .defaultCookie("Authorization", token)
                .build();
    }

    @AfterEach
    void clear() {
        wishProductRepository.deleteAllInBatch();
        optionRepository.deleteAll();
        productRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("옵션 제거 성공")
    void deleteOptionSuccess() {

        Option save = optionRepository.save(new Option("옵션1", 300, product));
        optionRepository.save(new Option("옵션2", 400, product));

        ResponseEntity<Void> response = restClient.delete()
                .uri("/{id}", save.getId())
                .retrieve()
                .toEntity(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("옵션 제거 실패 - 옵션 1개가 필수")
    void deleteOptionFail() {

        Option save = optionRepository.save(new Option("옵션1", 300, product));

        assertThatThrownBy(()->restClient.delete()
                .uri("/{id}", save.getId())
                .retrieve()
                .toEntity(Void.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    @DisplayName("옵션 수량 수정 성공")
    void changeQuantitySuccess() {

        Option save = optionRepository.save(new Option("옵션1", 300, product));

        ResponseEntity<Void> response = restClient.patch()
                .uri("/{id}", save.getId())
                .body(new OptionUpdateRequest(10))
                .retrieve()
                .toEntity(Void.class);

        Option updated = optionRepository.findById(save.getId()).get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(updated.getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("옵션 조회 성공")
    void getOptionSuccess() {

        Option save = optionRepository.save(new Option("옵션1", 300, product));

        ResponseEntity<OptionResponse> response = restClient.get()
                .uri("/{id}", save.getId())
                .retrieve()
                .toEntity(OptionResponse.class);


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(save.getId());
        assertThat(response.getBody().name()).isEqualTo(save.getName());
        assertThat(response.getBody().quantity()).isEqualTo(save.getQuantity());

    }

    @Test
    @DisplayName("옵션 추가 성공")
    void addOptionSuccess() {
        optionRepository.save(new Option("옵션1", 300, product));

        ResponseEntity<Void> response = restClient.post()
                .body(new OptionCreateListRequest(product.getId()
                        , List.of(new OptionCreateRequest("옵션2",10))))
                .retrieve()
                .toEntity(Void.class);


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    }

    @Test
    @DisplayName("옵션 추가 실패 - 중복된 옵션 이름")
    void addOptionFail() {
        optionRepository.save(new Option("옵션1", 300, product));

        assertThatThrownBy(()->restClient.post()
                .body(new OptionCreateListRequest(product.getId()
                        , List.of(new OptionCreateRequest("옵션1",10))))
                .retrieve()
                .toEntity(Void.class)
        ).isInstanceOf(HttpClientErrorException.BadRequest.class);

    }
}