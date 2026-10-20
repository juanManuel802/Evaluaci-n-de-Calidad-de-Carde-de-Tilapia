package co.unillanos.secct.adapters.controllers;

import co.unillanos.secct.usecases.dto.RegisterUserRequestDTO;
import co.unillanos.secct.usecases.dto.RegisterUserResponseDTO;
import co.unillanos.secct.usecases.exceptions.BusinessException;
import co.unillanos.secct.usecases.services.UserService;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private RegistrationController registrationController;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(registrationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/users responde 201 Created con el DTO expuesto")
    void testRegisterUser_Success() throws Exception {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "Carlos",
                "Gómez",
                "carlos@unillanos.edu.co",
                "3101234567",
                "ClaveSegura2026"
        );

        RegisterUserResponseDTO response = new RegisterUserResponseDTO(1, "carlos@unillanos.edu.co");
        when(userService.register(any(RegisterUserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("carlos@unillanos.edu.co"));

        verify(userService).register(any(RegisterUserRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/registro (alias de compatibilidad) responde 201 Created")
    void testRegisterUserAlias_Success() throws Exception {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "María",
                "Fernández",
                "maria@unillanos.edu.co",
                "3209876543",
                "ClaveSegura2026"
        );

        RegisterUserResponseDTO response = new RegisterUserResponseDTO(2, "maria@unillanos.edu.co");
        when(userService.register(any(RegisterUserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("maria@unillanos.edu.co"));
    }

    @Test
    @DisplayName("POST /api/users con payload inválido responde 400 Bad Request")
    void testRegisterUser_ValidationFailure() throws Exception {
        RegisterUserRequestDTO invalidRequest = new RegisterUserRequestDTO(
                "",
                "Gómez",
                "correo-invalido",
                "123",
                "corta"
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/users con correo duplicado responde 409 Conflict")
    void testRegisterUser_DuplicateEmailConflict() throws Exception {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "Carlos",
                "Gómez",
                "duplicado@correo.com",
                "3101234567",
                "ClaveSegura2026"
        );

        when(userService.register(any(RegisterUserRequestDTO.class)))
                .thenThrow(new BusinessException("correo ya registrado", 409));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("correo ya registrado"));
    }
}
