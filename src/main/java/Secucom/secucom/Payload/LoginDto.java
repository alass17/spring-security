package Secucom.secucom.Payload;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginDto {
    private String nomutilisateurOrEmail;
    private String password;
}
