package co.unillanos.secct.usecases.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login request DTO with Bean Validation rules (PL-2.05, PL-2.06, DC-HU2).
 */
public class LoginRequestDTO {

    @NotBlank(message = "El correo es obligatorio")
    @TrimmedLength(min = 6, max = 100, message = "El correo debe tener entre 6 y 100 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * toString() strictly masks the password for security (RNF-SEC-04).
     */
    @Override
    public String toString() {
        return "LoginRequestDTO{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
