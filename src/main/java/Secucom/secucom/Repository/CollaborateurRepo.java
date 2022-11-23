package Secucom.secucom.Repository;

import Secucom.secucom.Models.Collaborateurs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollaborateurRepo extends JpaRepository<Collaborateurs,Long> {

    Optional<Collaborateurs> findByEmail(String email);
    Optional<Collaborateurs> findByNomutilisateurOrEmail(String nomutilisateur, String email);
    Optional<Collaborateurs> findByNomutilisateur(String nomutilisateur);
    Boolean existsByNomutilisateur(String nomutilisateur);
    Boolean existsByEmail(String email);
}
