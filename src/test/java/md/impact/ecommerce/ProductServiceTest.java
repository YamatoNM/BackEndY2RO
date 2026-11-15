package md.impact.ecommerce;

import md.impact.ecommerce.dtos.ProductResponse;
import md.impact.ecommerce.entities.Product;
import md.impact.ecommerce.exceptions.ApiException;
import md.impact.ecommerce.repositories.ProductRepository;
import md.impact.ecommerce.services.DiscountService;
import md.impact.ecommerce.services.ProductService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
     @Mock ProductRepository productRepository;
     @Mock DiscountService discountService;


    ProductService productService;


     @BeforeEach
        void setUp(){
         productService = new ProductService(productRepository,discountService);
     }

     @Test
        void list_without_a_category_asks_repository_for_everything(){
         when(productRepository.findAllByOrderByIdAsc()).thenReturn(List.of());
         productService.list(null);

         verify(productRepository).findAllByOrderByIdAsc();
         verify(productRepository, never()).findByCategoryIdOrderByIdAsc(any());

     }
     @Test
    void list_with_a_category_filters_data_at_the_database_not_in_java(){
         when(productRepository.findByCategoryIdOrderByIdAsc(2L)).thenReturn(List.of());
         productService.list(2L);

         verify(productRepository).findByCategoryIdOrderByIdAsc(2L);

         verify(productRepository, never()).findAllByOrderByIdAsc();
     }

     @Test
    void list_maps_everything_through_the_discount_service(){
         Product p1 = mock(Product.class);
         Product p2 = mock(Product.class);

         when(p1.getPrice()).thenReturn(new BigDecimal("100.00"));
         when(p2.getPrice()).thenReturn(new BigDecimal("50.00"));

         when(productRepository.findAllByOrderByIdAsc()).thenReturn(List.of(p1,p2));
         when(discountService.finalPriceFor(p1)).thenReturn(new BigDecimal("85.00"));
         when(discountService.finalPriceFor(p2)).thenReturn(new BigDecimal("50.00"));

         List<ProductResponse> result = productService.list(null);
         assertThat(result).hasSize(2);
         assertThat(result.get(0).finalPrice()).isEqualByComparingTo("85.00");
         assertThat(result.get(1).finalPrice()).isEqualByComparingTo("50.00");
     }
    @Test
    void get_with_a_missing_id_the_404_and_a_null_pointer(){
         when(productRepository.findById(9999L)).thenReturn(Optional.empty());

         assertThatThrownBy(() ->
                 productService.get(9999L))
                 .isInstanceOf(ApiException.class)
                 .extracting(e -> ((ApiException) e).getStatus())
                 .isEqualTo(HttpStatus.NOT_FOUND);

    }



}
