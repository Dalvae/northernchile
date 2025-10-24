package com.northernchile.api.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users") // Endpoint protegido para admins
public class UserController {

    // TODO: Inyectar un UserService

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> userPayload) {
        // TODO: Crear un DTO para la creación de usuarios (email, fullName, password, role)
        // TODO: El UserService debe verificar que quien hace la llamada es SUPER_ADMIN
        // TODO: La lógica del servicio creará el nuevo usuario con el rol especificado

        String email = userPayload.get("email");
        String role = userPayload.get("role");

        System.out.println("Admin request to create user: " + email + " with role: " + role);

        return ResponseEntity.ok("User creation endpoint hit (implement logic).");
    }
}
