package com.danielnavia.melodygenerator.service;

import com.danielnavia.melodygenerator.dto.user.UserRequest;
import com.danielnavia.melodygenerator.dto.user.UserResponse;
import com.danielnavia.melodygenerator.entity.UserEntity;
import com.danielnavia.melodygenerator.repository.UserRepository;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de gestionar las operaciones relacionadas con los usuarios.
 */
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea y guarda un nuevo usuario.
     *
     * @param request datos necesarios para registrar el usuario
     * @return usuario creado sin exponer su contraseña
     */
    public UserResponse save(UserRequest request) {
        UserEntity user = new UserEntity(
                request.getUsername().trim(),
                request.getEmail().trim(),
                passwordEncoder.encode(request.getPassword()),
                LocalDateTime.now()
        );
        UserEntity savedUser = userRepository.save(user);
        return createUserResponse(savedUser);
    }

    /**
     * Obtiene todos los usuarios registrados.
     *
     * @return lista de usuarios sin exponer sus contraseñas
     */
    public List<UserResponse> findAll() {
        List<UserEntity> users = userRepository.findAll();
        List<UserResponse> userResponses = new ArrayList<>();
        for (UserEntity user : users) {
            UserResponse response = createUserResponse(user);
            userResponses.add(response);
        }
        return userResponses;
    }

    public UserResponse findById(Integer id) {
        UserEntity user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }
        return createUserResponse(user);
    }

    /**
     * Actualiza parcialmente un usuario existente.
     *
     * @param id identificador del usuario que se quiere actualizar
     * @param request datos que se quieren modificar
     * @return usuario actualizado sin exponer su contraseña
     * @throws RuntimeException si el usuario no existe o algún campo proporcionado está vacío
     */
    public UserResponse partialUpdate(Integer id, UserRequest request) {

        UserEntity existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            return null;
        }

        if (request.getUsername() != null) {
            if (request.getUsername().isBlank()) {
                throw new IllegalArgumentException("El username no puede estar vacío");
            }

            existingUser.setUsername(request.getUsername().trim());
        }

        if (request.getEmail() != null) {
            if (request.getEmail().isBlank()) {
                throw new IllegalArgumentException("El email no puede estar vacío");
            }

            existingUser.setEmail(request.getEmail().trim());
        }

        if (request.getPassword() != null) {
            if (request.getPassword().isBlank()) {
                throw new IllegalArgumentException("La contraseña no puede estar vacía");
            }

            existingUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        UserEntity updatedUser = userRepository.save(existingUser);

        return createUserResponse(updatedUser);
    }

    public void deleteById(Integer id) {
        userRepository.deleteById(id);
    }

    private UserResponse createUserResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}