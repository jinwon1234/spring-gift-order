package gift.product.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.PageResponse;
import gift.domain.Member;
import gift.domain.Option;
import gift.domain.Product;
import gift.domain.Role;
import gift.global.error.ErrorResponse;
import gift.jwt.JWTUtil;
import gift.member.repository.MemberRepository;
import gift.option.dto.OptionCreateRequest;
import gift.option.repository.OptionRepository;
import gift.product.dto.ProductCreateRequest;
import gift.product.dto.ProductResponse;
import gift.product.dto.ProductUpdateRequest;
import gift.product.repository.ProductRepository;
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
class ProductControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Member saved;

    RestClient restClient;

    @BeforeEach
    void setUp() {
        saved = memberRepository.save(new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR));

        String token = jwtUtil
                .createJWT(saved.getEmail(), saved.getRole().toString(), 1000 * 60L);

        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/products")
                .defaultCookie("Authorization", token)
                .build();
    }
    @AfterEach
    void clear() {
        optionRepository.deleteAll();
        productRepository.deleteAll();
        memberRepository.deleteAll();
    }


    @Test
    @DisplayName("상품 등록 성공")
    void addProductSuccess() {

        OptionCreateRequest options = new OptionCreateRequest("옵션1", 10);
        ProductCreateRequest productDto = new ProductCreateRequest("스윙칩", 3000, "data:image/~base64,", List.of(options));

        ResponseEntity<Void> response = restClient.post()
                .body(productDto)
                .retrieve()
                .toEntity(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("상품 등록 성공 - 상품 이름에 카카오 포함")
    void addProductFailCase1() {
        OptionCreateRequest options = new OptionCreateRequest("옵션1", 10);
        ProductCreateRequest productDto = new ProductCreateRequest("카카오", 3000, "data:image/~base64,", List.of(options));

        assertThatThrownBy(()->restClient.post()
                .body(productDto)
                .retrieve()
                .body(ErrorResponse.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    @DisplayName("상품 등록 실패 - 상품 가격이 0이하")
    void addProductFailCase2() {
        OptionCreateRequest options = new OptionCreateRequest("옵션1", 10);
        ProductCreateRequest productDto = new ProductCreateRequest("스윙칩", 0, "data:image/~base64,", List.of(options));

        assertThatThrownBy(()->restClient.post()
                .body(productDto)
                .retrieve()
                .body(ErrorResponse.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    @DisplayName("상품 등록 실패 - url이 빈 칸")
    void addProductFailCase3() {
        OptionCreateRequest options = new OptionCreateRequest("옵션1", 10);
        ProductCreateRequest productDto = new ProductCreateRequest("스윙칩", 1000, " ", List.of(options));

        assertThatThrownBy(()->restClient.post()
                .body(productDto)
                .retrieve()
                .body(ErrorResponse.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    @DisplayName("상품 등록 실패 - option이 존재하지 않음")
    void addProductFailCase4() {
        ProductCreateRequest productDto = new ProductCreateRequest("스윙칩", 1000, " ", List.of());

        assertThatThrownBy(()->restClient.post()
                .body(productDto)
                .retrieve()
                .body(ErrorResponse.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    @DisplayName("상품 조회 성공")
    void getProductSuccess() {
        Product product = addProductCase();

        ResponseEntity<ProductResponse> response = restClient.get()
                .uri("/{id}",product.getId())
                .retrieve()
                .toEntity(ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(product.getId());
        assertThat(response.getBody().getName()).isEqualTo(product.getName());
        assertThat(response.getBody().getPrice()).isEqualTo(product.getPrice());
        assertThat(response.getBody().getImageURL()).isEqualTo(product.getImageUrl());
    }

    @Test
    @DisplayName("상품 조회 실패")
    void getProductFail() {
        assertThatThrownBy(()-> {
            restClient.get()
            .uri("/{id}",1000L)
                    .retrieve()
                    .toEntity(ProductResponse.class);
        }).isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void deleteProductSuccess() {
        Product product = addProductCase();

        ResponseEntity<Void> response = restClient.delete()
                .uri("/{id}",product.getId())
                .retrieve()
                .toEntity(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("상품 업데이트 성공")
    void updateProductSuccess() {
        Product product = addProductCase();
        ProductUpdateRequest productDto = new ProductUpdateRequest("포카칩", 3000, "data:image/~base64,");
        ResponseEntity<Void> response = restClient.put()
                .uri("/{id}",product.getId())
                .body(productDto)
                .retrieve()
                .toEntity(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("상품 업데이트 실패 - 존재하는 상품 없음")
    void updateProductFailCase1() {

        ProductUpdateRequest productDto = new ProductUpdateRequest("포카칩", 3000, "data:image/~base64,");
        assertThatThrownBy(()->restClient.put()
                .uri("/{id}", 1000L)
                .body(productDto)
                .retrieve()
                .toEntity(Void.class)
        ).isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    @DisplayName("상품 업데이트 실패 - validation 통과 실패")
    void updateProductFailCase2() {

        Product product = addProductCase();
        ProductUpdateRequest productDto = new ProductUpdateRequest("카카오", -1, "data:image/~base64,");
        assertThatThrownBy(()->restClient.put()
                .uri("/{id}", product.getId())
                .body(productDto)
                .retrieve()
                .toEntity(Void.class)
        ).isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    @DisplayName("자신의 등록한 모든 상품 조회")
    void getAllProducts() throws JsonProcessingException {
        for (int i=0; i<10; i++) {
            addProductCase();
        }

        ResponseEntity<String> response = restClient.get()
                .uri("/mine")
                .retrieve()
                .toEntity(String.class);

        PageResponse<ProductResponse> page = objectMapper.readValue(
                response.getBody(),
                new TypeReference<PageResponse<ProductResponse>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page.getPage().getTotalElements()).isEqualTo(10);
    }



    @Test
    @DisplayName("상품 삭제 실패 - 존재하지 않는 상품")
    void deleteProductFail() {
        assertThatThrownBy(()-> {
            restClient.delete()
                    .uri("/{id}", 1000L)
                    .retrieve()
                    .toEntity(ProductResponse.class);
        }).isInstanceOf(HttpClientErrorException.NotFound.class);
    }


    @Test
    @DisplayName("특정 상품의 옵션 조회")
    void getOptionSuccess() {
        Product product = addProductCase();

        ResponseEntity<List> response = restClient.get()
                .uri("/{id}/options",product.getId())
                .retrieve()
                .toEntity(List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(1);
    }


    private Product addProductCase() {
        Product product = new Product("스윙칩", 3000, "data:image/~base64,", saved);
        Product save = productRepository.save(product);
        optionRepository.save(new Option("옵션1", 200, product));
        return save;
    }
}