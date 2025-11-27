package com.northernchile.api.cart;

import com.northernchile.api.availability.AvailabilityValidator;
import com.northernchile.api.availability.AvailabilityValidator.AvailabilityResult;
import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.cart.dto.CartItemReq;
import com.northernchile.api.cart.dto.CartRes;
import com.northernchile.api.model.Cart;
import com.northernchile.api.model.CartItem;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.TourScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Tests")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private TourScheduleRepository tourScheduleRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AvailabilityValidator availabilityValidator;

    @InjectMocks
    private CartService cartService;

    private Tour testTour;
    private TourSchedule testSchedule;
    private Cart testCart;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Set up test tour
        testTour = new Tour();
        testTour.setId(UUID.randomUUID());
        testTour.setNameTranslations(Map.of("es", "Tour de Prueba"));
        testTour.setPrice(new BigDecimal("50000"));

        // Set up test schedule
        testSchedule = new TourSchedule();
        testSchedule.setId(UUID.randomUUID());
        testSchedule.setTour(testTour);
        testSchedule.setStartDatetime(Instant.now().plus(7, ChronoUnit.DAYS));
        testSchedule.setMaxParticipants(10);

        // Set up test cart
        testCart = new Cart();
        testCart.setId(UUID.randomUUID());
        testCart.setStatus("ACTIVE");
        testCart.setItems(new ArrayList<>());
        testCart.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));

        // Set up test user using constructor
        testUser = new User(
            UUID.randomUUID(),
            "test@example.com",
            null,
            "Test User",
            null,
            null,
            null,
            "ROLE_CLIENT",
            "LOCAL",
            null
        );
    }

    @Nested
    @DisplayName("Get or Create Cart Tests")
    class GetOrCreateCartTests {

        @Test
        @DisplayName("Should create new cart for guest user without cart ID")
        void shouldCreateNewCartForGuest() {
            // Given
            Cart newCart = new Cart();
            newCart.setId(UUID.randomUUID());
            when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

            // When
            Cart result = cartService.getOrCreateCart(Optional.empty(), Optional.empty());

            // Then
            assertThat(result).isNotNull();
            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        @DisplayName("Should return existing cart for guest with cart ID")
        void shouldReturnExistingCartForGuest() {
            // Given
            when(cartRepository.findByIdWithDetails(testCart.getId()))
                    .thenReturn(Optional.of(testCart));

            // When
            Cart result = cartService.getOrCreateCart(Optional.empty(), Optional.of(testCart.getId()));

            // Then
            assertThat(result).isEqualTo(testCart);
        }

        @Test
        @DisplayName("Should return user cart for logged-in user")
        void shouldReturnUserCartForLoggedInUser() {
            // Given
            testCart.setUser(testUser);
            when(cartRepository.findByUserId(testUser.getId()))
                    .thenReturn(Optional.of(testCart));
            when(cartRepository.findByIdWithDetails(testCart.getId()))
                    .thenReturn(Optional.of(testCart));

            // When
            Cart result = cartService.getOrCreateCart(Optional.of(testUser), Optional.empty());

            // Then
            assertThat(result).isEqualTo(testCart);
        }

        @Test
        @DisplayName("Should create new cart if guest cart ID not found")
        void shouldCreateNewCartIfGuestCartNotFound() {
            // Given
            UUID nonExistentCartId = UUID.randomUUID();
            when(cartRepository.findByIdWithDetails(nonExistentCartId))
                    .thenReturn(Optional.empty());

            Cart newCart = new Cart();
            newCart.setId(UUID.randomUUID());
            when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

            // When
            Cart result = cartService.getOrCreateCart(Optional.empty(), Optional.of(nonExistentCartId));

            // Then
            assertThat(result).isNotNull();
            verify(cartRepository).save(any(Cart.class));
        }
    }

    @Nested
    @DisplayName("Add Item to Cart Tests")
    class AddItemToCartTests {

        @Test
        @DisplayName("Should add item to cart successfully")
        void shouldAddItemToCartSuccessfully() {
            // Given
            CartItemReq itemReq = new CartItemReq();
            itemReq.setScheduleId(testSchedule.getId());
            itemReq.setNumParticipants(2);

            when(tourScheduleRepository.findById(testSchedule.getId()))
                    .thenReturn(Optional.of(testSchedule));
            when(availabilityValidator.validateAvailability(any(), anyInt(), any(), any()))
                    .thenReturn(new AvailabilityResult(true, 10, 2, 0, 8, 2));
            when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
            when(cartRepository.findByIdWithDetails(testCart.getId()))
                    .thenReturn(Optional.of(testCart));

            // When
            Cart result = cartService.addItemToCart(testCart, itemReq);

            // Then
            assertThat(result).isNotNull();
            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        @DisplayName("Should reject item when schedule not found")
        void shouldRejectItemWhenScheduleNotFound() {
            // Given
            CartItemReq itemReq = new CartItemReq();
            itemReq.setScheduleId(UUID.randomUUID());
            itemReq.setNumParticipants(2);

            when(tourScheduleRepository.findById(any())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> cartService.addItemToCart(testCart, itemReq))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("TourSchedule not found");
        }

        @Test
        @DisplayName("Should reject item when no availability")
        void shouldRejectItemWhenNoAvailability() {
            // Given
            CartItemReq itemReq = new CartItemReq();
            itemReq.setScheduleId(testSchedule.getId());
            itemReq.setNumParticipants(15); // More than max participants

            when(tourScheduleRepository.findById(testSchedule.getId()))
                    .thenReturn(Optional.of(testSchedule));
            when(availabilityValidator.validateAvailability(any(), anyInt(), any(), any()))
                    .thenReturn(new AvailabilityResult(false, 10, 10, 0, 0, 15));

            // When/Then
            assertThatThrownBy(() -> cartService.addItemToCart(testCart, itemReq))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No hay suficientes cupos disponibles");
        }

        @Test
        @DisplayName("Should accumulate participants when adding same schedule")
        void shouldAccumulateParticipantsForSameSchedule() {
            // Given - Cart already has 3 participants for this schedule
            CartItem existingItem = new CartItem();
            existingItem.setId(UUID.randomUUID());
            existingItem.setSchedule(testSchedule);
            existingItem.setNumParticipants(3);
            existingItem.setCart(testCart);
            testCart.getItems().add(existingItem);

            CartItemReq itemReq = new CartItemReq();
            itemReq.setScheduleId(testSchedule.getId());
            itemReq.setNumParticipants(2); // Adding 2 more

            when(tourScheduleRepository.findById(testSchedule.getId()))
                    .thenReturn(Optional.of(testSchedule));
            // Validator should receive total: 3 existing + 2 new = 5
            when(availabilityValidator.validateAvailability(eq(testSchedule), eq(5), eq(testCart.getId()), any()))
                    .thenReturn(new AvailabilityResult(true, 10, 0, 5, 5, 5));
            when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
            when(cartRepository.findByIdWithDetails(testCart.getId()))
                    .thenReturn(Optional.of(testCart));

            // When
            Cart result = cartService.addItemToCart(testCart, itemReq);

            // Then
            assertThat(result).isNotNull();
            verify(availabilityValidator).validateAvailability(eq(testSchedule), eq(5), any(), any());
        }
    }

    @Nested
    @DisplayName("Remove Item from Cart Tests")
    class RemoveItemFromCartTests {

        @Test
        @DisplayName("Should remove item from cart")
        void shouldRemoveItemFromCart() {
            // Given
            UUID itemId = UUID.randomUUID();
            when(cartRepository.findByIdWithDetails(testCart.getId()))
                    .thenReturn(Optional.of(testCart));

            // When
            Cart result = cartService.removeItemFromCart(testCart, itemId);

            // Then
            assertThat(result).isNotNull();
            verify(cartItemRepository).deleteById(itemId);
        }
    }

    @Nested
    @DisplayName("Cart to Response Conversion Tests")
    class CartToResponseTests {

        @Test
        @DisplayName("Should convert cart to response with correct totals")
        void shouldConvertCartToResponseWithCorrectTotals() {
            // Given
            CartItem item1 = new CartItem();
            item1.setId(UUID.randomUUID());
            item1.setSchedule(testSchedule);
            item1.setNumParticipants(2);

            testCart.getItems().add(item1);

            // When
            CartRes result = cartService.toCartRes(testCart);

            // Then
            assertThat(result.getCartId()).isEqualTo(testCart.getId());
            assertThat(result.getItems()).hasSize(1);
            // 2 participants * 50000 = 100000
            assertThat(result.getCartTotal()).isEqualByComparingTo(new BigDecimal("100000"));
        }

        @Test
        @DisplayName("Should handle empty cart")
        void shouldHandleEmptyCart() {
            // When
            CartRes result = cartService.toCartRes(testCart);

            // Then
            assertThat(result.getItems()).isEmpty();
            assertThat(result.getCartTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate totals for multiple items")
        void shouldCalculateTotalsForMultipleItems() {
            // Given
            Tour tour2 = new Tour();
            tour2.setId(UUID.randomUUID());
            tour2.setNameTranslations(Map.of("es", "Otro Tour"));
            tour2.setPrice(new BigDecimal("30000"));

            TourSchedule schedule2 = new TourSchedule();
            schedule2.setId(UUID.randomUUID());
            schedule2.setTour(tour2);

            CartItem item1 = new CartItem();
            item1.setId(UUID.randomUUID());
            item1.setSchedule(testSchedule);
            item1.setNumParticipants(2);

            CartItem item2 = new CartItem();
            item2.setId(UUID.randomUUID());
            item2.setSchedule(schedule2);
            item2.setNumParticipants(3);

            testCart.getItems().add(item1);
            testCart.getItems().add(item2);

            // When
            CartRes result = cartService.toCartRes(testCart);

            // Then
            assertThat(result.getItems()).hasSize(2);
            // (2 * 50000) + (3 * 30000) = 100000 + 90000 = 190000
            assertThat(result.getCartTotal()).isEqualByComparingTo(new BigDecimal("190000"));
        }

        @Test
        @DisplayName("Should include correct item details in response")
        void shouldIncludeCorrectItemDetails() {
            // Given
            CartItem item = new CartItem();
            item.setId(UUID.randomUUID());
            item.setSchedule(testSchedule);
            item.setNumParticipants(4);

            testCart.getItems().add(item);

            // When
            CartRes result = cartService.toCartRes(testCart);

            // Then
            assertThat(result.getItems()).hasSize(1);
            var itemRes = result.getItems().get(0);
            assertThat(itemRes.getScheduleId()).isEqualTo(testSchedule.getId());
            assertThat(itemRes.getTourId()).isEqualTo(testTour.getId());
            assertThat(itemRes.getTourName()).isEqualTo("Tour de Prueba");
            assertThat(itemRes.getNumParticipants()).isEqualTo(4);
            assertThat(itemRes.getPricePerParticipant()).isEqualByComparingTo(new BigDecimal("50000"));
            assertThat(itemRes.getItemTotal()).isEqualByComparingTo(new BigDecimal("200000"));
        }
    }
}
