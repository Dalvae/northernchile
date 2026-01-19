package com.northernchile.api.user;

import com.northernchile.api.config.security.annotation.CurrentUser;
import com.northernchile.api.model.User;
import com.northernchile.api.user.dto.SavedParticipantReq;
import com.northernchile.api.user.dto.SavedParticipantRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile/participants")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Saved Participants", description = "Manage saved participants for future bookings")
public class SavedParticipantController {

    private final SavedParticipantService savedParticipantService;

    public SavedParticipantController(SavedParticipantService savedParticipantService) {
        this.savedParticipantService = savedParticipantService;
    }

    @GetMapping
    @Operation(
        summary = "Get saved participants",
        description = "Get all saved participants for the current user"
    )
    @ApiResponse(responseCode = "200", description = "List of saved participants")
    public ResponseEntity<List<SavedParticipantRes>> getMyParticipants(@CurrentUser User user) {
        List<SavedParticipantRes> participants = savedParticipantService.getParticipantsForUser(user);
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/self")
    @Operation(
        summary = "Get self participant",
        description = "Get the participant marked as 'self' (the current user's own data)"
    )
    @ApiResponse(responseCode = "200", description = "Self participant found")
    @ApiResponse(responseCode = "404", description = "No self participant exists")
    public ResponseEntity<SavedParticipantRes> getSelfParticipant(@CurrentUser User user) {
        return savedParticipantService.getSelfParticipant(user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(
        summary = "Create saved participant",
        description = "Create a new saved participant for future bookings"
    )
    @ApiResponse(responseCode = "201", description = "Participant created")
    public ResponseEntity<SavedParticipantRes> createParticipant(
            @CurrentUser User user,
            @Valid @RequestBody SavedParticipantReq req) {
        SavedParticipantRes created = savedParticipantService.createParticipant(user, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/self")
    @Operation(
        summary = "Create or update self participant",
        description = "Create or update the participant representing the current user. " +
                "This also syncs the data to the user's profile."
    )
    @ApiResponse(responseCode = "200", description = "Self participant created/updated")
    public ResponseEntity<SavedParticipantRes> createOrUpdateSelf(
            @CurrentUser User user,
            @Valid @RequestBody SavedParticipantReq req) {
        SavedParticipantRes result = savedParticipantService.createOrUpdateSelf(user, req);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update saved participant",
        description = "Update an existing saved participant"
    )
    @ApiResponse(responseCode = "200", description = "Participant updated")
    @ApiResponse(responseCode = "404", description = "Participant not found")
    public ResponseEntity<SavedParticipantRes> updateParticipant(
            @PathVariable UUID id,
            @CurrentUser User user,
            @Valid @RequestBody SavedParticipantReq req) {
        SavedParticipantRes updated = savedParticipantService.updateParticipant(id, user, req);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete saved participant",
        description = "Delete a saved participant"
    )
    @ApiResponse(responseCode = "204", description = "Participant deleted")
    @ApiResponse(responseCode = "404", description = "Participant not found")
    public ResponseEntity<Void> deleteParticipant(
            @PathVariable UUID id,
            @CurrentUser User user) {
        savedParticipantService.deleteParticipant(id, user);
        return ResponseEntity.noContent().build();
    }
}
