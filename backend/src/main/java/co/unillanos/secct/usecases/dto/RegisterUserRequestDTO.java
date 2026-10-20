package co.unillanos.secct.usecases.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registration request DTO with Bean Validation rules (PL-1.01 to PL-1.05, PL-1.17).
 */
public class RegisterUserRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^(?!\\s+$)[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]{2,50}$",
            message = "El nombre debe tener entre 2 y 50 letras y espacios, y no solo espacios")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Pattern(regexp = "^(?!\\s+$)[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]{2,50}$",
            message = "El apellido debe tener entre 2 y 50 letras y espacios, y no solo espacios")
    private String lastName;

    @NotBlank(message = "El correo es obligatorio")
    @Pattern(regexp = "^\\s*[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\s*$",
            message = "El formato de correo no es válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^3[0-9]{9}$", message = "El teléfono debe tener 10 dígitos e iniciar en 3")
    private String phone;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 20, message = "La contraseña debe tener entre 8 y 20 caracteres")
    @Pattern(regexp = "^(?!\\s+$).{8,20}$", message = "La contraseña no puede consistir solo de espacios")
    private String password;

    public RegisterUserRequestDTO() {
    }

    public RegisterUserRequestDTO(String firstName, String lastName, String email, String phone, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * toString() strictly omits password for security reasons (RNF-SEC-04).
     */
    @Override
    public String toString() {
        return "RegisterUserRequestDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
