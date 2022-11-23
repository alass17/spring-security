package Secucom.secucom.Payload;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SignUpDto {
    private String nom;
    private String nomutilisateur;
    private String email;
    private String password;
}
