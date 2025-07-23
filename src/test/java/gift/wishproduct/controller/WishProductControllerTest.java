package gift.wishproduct.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.PageResponse;
import gift.domain.*;
import gift.jwt.JWTUtil;
import gift.member.repository.MemberRepository;
import gift.option.repository.OptionRepository;
import gift.product.repository.ProductRepository;
import gift.wishproduct.dto.WishProductCreateReq;
import gift.wishproduct.dto.WishProductResponse;
import gift.wishproduct.dto.WishProductUpdateReq;
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
class WishProductControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WishProductRepository wishProductRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private JWTUtil jwtUtil;
  
    @Autowired
    private ObjectMapper objectMapper;
  
    private Member member;
    private Product product;
    private Option option;

    RestClient restClient;


    @BeforeEach
    void setUp() {
        member = memberRepository.save(new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR));
        Product save = productRepository.save(new Product("스윙칩", 3000, "data:image/~base64", member));
        product = save;
        option = optionRepository.save(new Option("옵션1", 10, product));

        String token = jwtUtil
                .createJWT(member.getEmail(), member.getRole().toString(), 1000 * 60L);

        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/wish-products")
                .defaultCookie("Authorization", token)
                .build();
    }

    @AfterEach
    void clear() {
        wishProductRepository.deleteAll();
        optionRepository.deleteAll();
        productRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("위시리스트 추가 성공")
    void addWishProductSuccess() {

        // given
        WishProductCreateReq dto = new WishProductCreateReq(product.getId(), option.getId(),10);

        // when
        ResponseEntity<Void> response = restClient.post()
                .body(dto)
                .retrieve()
                .toEntity(Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().get("location")).isNotNull();
    }

    @Test
    @DisplayName("위시 리스트 조회 성공")
    void getMyWishList() throws JsonProcessingException {

        // given
        addWishProduct();

        // when
        ResponseEntity<String> response = restClient.get()
                .retrieve()
                .toEntity(String.class);

        PageResponse<WishProductResponse> page = objectMapper.readValue(
                response.getBody(),
                new TypeReference<PageResponse<WishProductResponse>>() {}
        );

        List<WishProductResponse> content = page.getContent();
        System.out.println(content);

        // then
        assertThat(page.getPage().getTotalElements()).isEqualTo(1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("위시 상품 삭제 성공")
    void deleteWishProductSuccess() {
        WishProduct wishProduct = addWishProduct();

        ResponseEntity<Void> response = restClient.delete()
                .uri("/{id}", wishProduct.getId())
                .retrieve()
                .toEntity(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("위시 상품 삭제 실패 - 존재하지 않는 위시 상품")
    void deleteWishProductFail() {

        assertThatThrownBy(()->restClient.delete()
                .uri("/{id}", 1000L)
                .retrieve()
                .toEntity(Void.class)
        ).isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    @DisplayName("위시 상품 수정 성공")
    void updateWishProductSuccess() {

        // given
        WishProduct wishProduct = addWishProduct();

        WishProductUpdateReq body = new WishProductUpdateReq(20);

        // when
        ResponseEntity<Void> response = restClient.patch()
                .uri("/{id}", wishProduct.getId())
                .body(body)
                .retrieve()
                .toEntity(Void.class);

        WishProduct updated = wishProductRepository.findById(wishProduct.getId()).get();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(updated.getQuantity()).isEqualTo(body.getQuantity());

    }

    @Test
    @DisplayName("위시 상품 수정 실패 - 0개로 수정 불가")
    void updateWishProductFail() {

        // given
        WishProduct wishProduct = addWishProduct();

        WishProductUpdateReq body = new WishProductUpdateReq(0);

        // when
        assertThatThrownBy(()->restClient.patch()
                .uri("/{id}", wishProduct.getId())
                .body(body)
                .retrieve()
                .toEntity(Void.class)
        ).isInstanceOf(HttpClientErrorException.BadRequest.class);

    }


    private WishProduct addWishProduct() {
        WishProduct wishProduct = new WishProduct(10, member, product,option);

        return wishProductRepository.save(wishProduct);
    }

}