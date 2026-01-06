package com.northernchile.api.tour;

import com.northernchile.api.audit.AuditLogService;
import com.northernchile.api.media.repository.MediaRepository;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.dto.TourCreateReq;
import com.northernchile.api.tour.dto.TourRes;
import com.northernchile.api.tour.dto.TourUpdateReq;
import com.northernchile.api.util.SlugGenerator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TourService Tests")
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private SlugGenerator slugGenerator;

    @Mock
    private TourMapper tourMapper;

    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private TourService tourService;

    private User adminUser;
    private User partnerAdmin;
    private Tour testTour;
    private TourRes testTourRes;

    @BeforeEach
    void setUp() {
        // Set up admin user using constructor (id, email, passwordHash, fullName, nationality, phoneNumber, dateOfBirth, role, authProvider, providerId)
        adminUser = new User(
            UUID.randomUUID(),
            "admin@northernchile.com",
            null,
            "Admin User",
            null,
            null,
            null,
            "ROLE_SUPER_ADMIN",
            "LOCAL",
            null
        );

        // Set up partner admin
        partnerAdmin = new User(
            UUID.randomUUID(),
            "partner@example.com",
            null,
            "Partner Admin",
            null,
            null,
            null,
            "ROLE_PARTNER_ADMIN",
            "LOCAL",
            null
        );

        // Set up test tour
        testTour = new Tour();
        testTour.setId(UUID.randomUUID());
        testTour.setOwner(partnerAdmin);
        testTour.setNameTranslations(Map.of("es", "Tour Astronómico", "en", "Astronomy Tour"));
        testTour.setCategory("astronomy");
        testTour.setPrice(new BigDecimal("75000"));
        testTour.setDefaultMaxParticipants(12);
        testTour.setDurationHours(3);
        testTour.setStatus("PUBLISHED");
        testTour.setSlug("tour-astronomico");

        // Set up test tour response using record constructor
        testTourRes = createMockTourRes(testTour.getId(), testTour.getSlug(), testTour.getNameTranslations());
    }

    @Nested
    @DisplayName("Get Published Tours Tests")
    class GetPublishedToursTests {

        @Test
        @DisplayName("Should return only published tours")
        void shouldReturnOnlyPublishedTours() {
            // Given
            when(tourRepository.findByStatusNotDeletedWithImages("PUBLISHED"))
                    .thenReturn(List.of(testTour));
            when(tourMapper.toTourRes(testTour)).thenReturn(testTourRes);
            when(mediaRepository.findByTourIdOrderByDisplayOrderAsc(any()))
                    .thenReturn(List.of());

            // When
            List<TourRes> result = tourService.getPublishedTours();

            // Then
            assertThat(result).hasSize(1);
            verify(tourRepository).findByStatusNotDeletedWithImages("PUBLISHED");
        }

        @Test
        @DisplayName("Should return empty list when no published tours")
        void shouldReturnEmptyListWhenNoPublishedTours() {
            // Given
            when(tourRepository.findByStatusNotDeletedWithImages("PUBLISHED"))
                    .thenReturn(List.of());

            // When
            List<TourRes> result = tourService.getPublishedTours();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Tour by Slug Tests")
    class GetTourBySlugTests {

        @Test
        @DisplayName("Should return tour by slug")
        void shouldReturnTourBySlug() {
            // Given
            String slug = "tour-astronomico";
            when(tourRepository.findBySlugPublished(slug))
                    .thenReturn(Optional.of(testTour));
            when(tourMapper.toTourRes(testTour)).thenReturn(testTourRes);
            when(mediaRepository.findByTourIdOrderByDisplayOrderAsc(any()))
                    .thenReturn(List.of());

            // When
            TourRes result = tourService.getTourBySlug(slug);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.slug()).isEqualTo(slug);
        }

        @Test
        @DisplayName("Should throw exception when slug not found")
        void shouldThrowExceptionWhenSlugNotFound() {
            // Given
            String slug = "non-existent-slug";
            when(tourRepository.findBySlugPublished(slug))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> tourService.getTourBySlug(slug))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Tour not found with slug");
        }
    }

    @Nested
    @DisplayName("Create Tour Tests")
    class CreateTourTests {

        @Test
        @DisplayName("Should create tour successfully")
        void shouldCreateTourSuccessfully() {
            // Given - using record constructor
            TourCreateReq createReq = new TourCreateReq(
                Map.of("es", "Nuevo Tour", "en", "New Tour"), // nameTranslations
                Map.of(),  // descriptionBlocksTranslations
                false,     // isWindSensitive
                false,     // isMoonSensitive
                false,     // isCloudSensitive
                "adventure", // category
                new BigDecimal("60000"), // price
                8,         // defaultMaxParticipants
                4,         // durationHours
                null,      // defaultStartTime
                "DRAFT",   // status
                "content-key", // contentKey
                null,      // guideName
                null,      // itineraryTranslations
                null,      // equipmentTranslations
                null       // additionalInfoTranslations
            );

            when(slugGenerator.generateSlug("Nuevo Tour")).thenReturn("nuevo-tour");
            when(tourRepository.findBySlug("nuevo-tour")).thenReturn(Optional.empty());
            when(tourRepository.save(any(Tour.class))).thenAnswer(invocation -> {
                Tour tour = invocation.getArgument(0);
                tour.setId(UUID.randomUUID());
                return tour;
            });
            when(tourMapper.toTourRes(any(Tour.class))).thenReturn(createMockTourRes(UUID.randomUUID(), "nuevo-tour", createReq.nameTranslations()));

            // When
            TourRes result = tourService.createTour(createReq, adminUser);

            // Then
            assertThat(result).isNotNull();
            verify(tourRepository).save(any(Tour.class));
            verify(auditLogService).logCreate(eq(adminUser), eq("TOUR"), any(), any(), any());
        }

        @Test
        @DisplayName("Should generate unique slug when duplicate exists")
        void shouldGenerateUniqueSlugWhenDuplicateExists() {
            // Given - using record constructor
            TourCreateReq createReq = new TourCreateReq(
                Map.of("es", "Tour Astronómico"), // nameTranslations
                Map.of(),  // descriptionBlocksTranslations
                false, false, false, // sensitivity flags
                "astronomy", new BigDecimal("60000"), 8, 4, null, "DRAFT", "content-key", null, null, null, null
            );

            when(slugGenerator.generateSlug("Tour Astronómico")).thenReturn("tour-astronomico");
            // First slug exists
            when(tourRepository.findBySlug("tour-astronomico")).thenReturn(Optional.of(testTour));
            // Second slug is available
            when(tourRepository.findBySlug("tour-astronomico-1")).thenReturn(Optional.empty());

            ArgumentCaptor<Tour> tourCaptor = ArgumentCaptor.forClass(Tour.class);
            when(tourRepository.save(tourCaptor.capture())).thenAnswer(invocation -> {
                Tour tour = invocation.getArgument(0);
                tour.setId(UUID.randomUUID());
                return tour;
            });
            when(tourMapper.toTourRes(any(Tour.class))).thenReturn(createMockTourRes(UUID.randomUUID(), "tour-astronomico-1", createReq.nameTranslations()));

            // When
            tourService.createTour(createReq, adminUser);

            // Then
            Tour savedTour = tourCaptor.getValue();
            assertThat(savedTour.getSlug()).isEqualTo("tour-astronomico-1");
        }

        @Test
        @DisplayName("Should set owner to current user")
        void shouldSetOwnerToCurrentUser() {
            // Given - using record constructor
            TourCreateReq createReq = new TourCreateReq(
                Map.of("es", "Mi Tour"), Map.of(), false, false, false,
                "nature", new BigDecimal("50000"), 10, 2, null, "DRAFT", "content-key", null, null, null, null
            );

            when(slugGenerator.generateSlug("Mi Tour")).thenReturn("mi-tour");
            when(tourRepository.findBySlug("mi-tour")).thenReturn(Optional.empty());

            ArgumentCaptor<Tour> tourCaptor = ArgumentCaptor.forClass(Tour.class);
            when(tourRepository.save(tourCaptor.capture())).thenAnswer(invocation -> {
                Tour tour = invocation.getArgument(0);
                tour.setId(UUID.randomUUID());
                return tour;
            });
            when(tourMapper.toTourRes(any(Tour.class))).thenReturn(createMockTourRes(UUID.randomUUID(), "mi-tour", createReq.nameTranslations()));

            // When
            tourService.createTour(createReq, partnerAdmin);

            // Then
            Tour savedTour = tourCaptor.getValue();
            assertThat(savedTour.getOwner()).isEqualTo(partnerAdmin);
        }
    }

    @Nested
    @DisplayName("Update Tour Tests")
    class UpdateTourTests {

        @Test
        @DisplayName("Should update tour successfully")
        void shouldUpdateTourSuccessfully() {
            // Given - using record constructor
            TourUpdateReq updateReq = new TourUpdateReq(
                Map.of("es", "Tour Astronómico Premium", "en", "Premium Astronomy Tour"),
                Map.of(), false, false, false, "astronomy",
                new BigDecimal("90000"), 10, 4, null, "PUBLISHED", null, null, null, null, null
            );

            when(tourRepository.findByIdNotDeleted(testTour.getId()))
                    .thenReturn(Optional.of(testTour));
            when(slugGenerator.generateSlug("Tour Astronómico Premium")).thenReturn("tour-astronomico-premium");
            when(tourRepository.findBySlug("tour-astronomico-premium")).thenReturn(Optional.empty());
            when(tourRepository.save(any(Tour.class))).thenReturn(testTour);
            when(tourMapper.toTourRes(any(Tour.class))).thenReturn(testTourRes);

            // When
            TourRes result = tourService.updateTour(testTour.getId(), updateReq, adminUser);

            // Then
            assertThat(result).isNotNull();
            verify(auditLogService).logUpdate(eq(adminUser), eq("TOUR"), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should throw exception when tour not found")
        void shouldThrowExceptionWhenTourNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            TourUpdateReq updateReq = new TourUpdateReq(
                Map.of("es", "Test"), null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null
            );

            when(tourRepository.findByIdNotDeleted(nonExistentId))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> tourService.updateTour(nonExistentId, updateReq, adminUser))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Tour not found with id");
        }
    }

    @Nested
    @DisplayName("Delete Tour Tests")
    class DeleteTourTests {

        @Test
        @DisplayName("Should soft delete tour")
        void shouldSoftDeleteTour() {
            // Given
            when(tourRepository.findByIdNotDeleted(testTour.getId()))
                    .thenReturn(Optional.of(testTour));
            when(tourRepository.save(any(Tour.class))).thenReturn(testTour);

            // When
            tourService.deleteTour(testTour.getId(), adminUser);

            // Then
            verify(tourRepository).save(argThat(tour -> tour.getDeletedAt() != null));
            verify(auditLogService).logDelete(eq(adminUser), eq("TOUR"), any(), any(), any());
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent tour")
        void shouldThrowExceptionWhenDeletingNonExistentTour() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(tourRepository.findByIdNotDeleted(nonExistentId))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> tourService.deleteTour(nonExistentId, adminUser))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Admin Access Tests")
    class AdminAccessTests {

        @Test
        @DisplayName("SUPER_ADMIN should get all tours")
        void superAdminShouldGetAllTours() {
            // Given
            when(tourRepository.findAllNotDeletedWithImages())
                    .thenReturn(List.of(testTour));
            when(tourMapper.toTourRes(any())).thenReturn(testTourRes);
            when(mediaRepository.findByTourIdOrderByDisplayOrderAsc(any()))
                    .thenReturn(List.of());

            // When
            List<TourRes> result = tourService.getAllToursForAdmin();

            // Then
            assertThat(result).hasSize(1);
            verify(tourRepository).findAllNotDeletedWithImages();
        }

        @Test
        @DisplayName("PARTNER_ADMIN should only get their own tours")
        void partnerAdminShouldGetOwnTours() {
            // Given
            when(tourRepository.findByOwnerIdNotDeletedWithImages(partnerAdmin.getId()))
                    .thenReturn(List.of(testTour));
            when(tourMapper.toTourRes(any())).thenReturn(testTourRes);
            when(mediaRepository.findByTourIdOrderByDisplayOrderAsc(any()))
                    .thenReturn(List.of());

            // When
            List<TourRes> result = tourService.getToursByOwner(partnerAdmin);

            // Then
            assertThat(result).hasSize(1);
            verify(tourRepository).findByOwnerIdNotDeletedWithImages(partnerAdmin.getId());
        }
    }

    @Nested
    @DisplayName("Get Tour by ID Tests")
    class GetTourByIdTests {

        @Test
        @DisplayName("Should return tour by ID")
        void shouldReturnTourById() {
            // Given
            when(tourRepository.findByIdNotDeleted(testTour.getId()))
                    .thenReturn(Optional.of(testTour));
            when(tourMapper.toTourRes(testTour)).thenReturn(testTourRes);
            when(mediaRepository.findByTourIdOrderByDisplayOrderAsc(any()))
                    .thenReturn(List.of());

            // When
            TourRes result = tourService.getTourById(testTour.getId(), adminUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(testTour.getId());
        }

        @Test
        @DisplayName("Should throw exception when tour ID not found")
        void shouldThrowExceptionWhenTourIdNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(tourRepository.findByIdNotDeleted(nonExistentId))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> tourService.getTourById(nonExistentId, adminUser))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // Helper method to create mock TourRes for tests
    private TourRes createMockTourRes(UUID id, String slug, Map<String, String> nameTranslations) {
        return new TourRes(
            id,
            slug,
            nameTranslations,
            "astronomy",        // category
            new BigDecimal("75000"), // price
            12,                 // defaultMaxParticipants
            3,                  // durationHours
            null,               // defaultStartTime
            "PUBLISHED",        // status
            List.of(),          // images
            false,              // isMoonSensitive
            false,              // isWindSensitive
            false,              // isCloudSensitive
            java.time.Instant.now(), // createdAt
            java.time.Instant.now(), // updatedAt
            "content-key",      // contentKey
            null,               // guideName
            List.of(),          // itinerary
            List.of(),          // equipment
            List.of(),          // additionalInfo
            Map.of(),           // itineraryTranslations
            Map.of(),           // equipmentTranslations
            Map.of(),           // additionalInfoTranslations
            Map.of(),           // descriptionBlocksTranslations
            null,               // ownerId
            null,               // ownerName
            null                // ownerEmail
        );
    }
}
