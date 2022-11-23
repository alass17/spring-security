package Secucom.secucom.Controllers;

import Secucom.secucom.Models.Collaborateurs;
import Secucom.secucom.Models.Role;
import Secucom.secucom.Payload.LoginDto;
import Secucom.secucom.Payload.SignUpDto;
import Secucom.secucom.Repository.CollaborateurRepo;
import Secucom.secucom.Repository.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@RestController
//@RequestMapping("/api/auth")
public class AuthController {

    OAuth2AuthorizedClientService authorizedClientService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CollaborateurRepo collaborateurRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signin")
    public ResponseEntity<String> authenticateUser(@RequestBody LoginDto loginDto){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getNomutilisateurOrEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new ResponseEntity<>("Utilisateur connecté avec succès !.", HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDto signUpDto){

        // verifier si le nom d'utilisateur existe deja dans la bd
        if(collaborateurRepo.existsByNomutilisateur(signUpDto.getNomutilisateur())){
            return new ResponseEntity<>("Nom d'utilisateur déjà pris!", HttpStatus.BAD_REQUEST);
        }

        // verifier si l' email existe deja dans la bd
        if(collaborateurRepo.existsByEmail(signUpDto.getEmail())){
            return new ResponseEntity<>("Cet email est déjà pris!", HttpStatus.BAD_REQUEST);
        }

        // create user object
        Collaborateurs collaborateurs = new Collaborateurs();
        collaborateurs.setNom(signUpDto.getNom());
        collaborateurs.setNomutilisateur(signUpDto.getNomutilisateur());
        collaborateurs.setEmail(signUpDto.getEmail());
        collaborateurs.setPassword(passwordEncoder.encode(signUpDto.getPassword()));

        Role roles = roleRepo.findByName("ADMIN").get();
        collaborateurs.setRoles(Collections.singleton(roles));

        collaborateurRepo.save(collaborateurs);

        return new ResponseEntity<>("Utilisateur enregistré avec succès", HttpStatus.OK);
    }



    @RolesAllowed("USER")
    @RequestMapping("/**")
    public String getUser()
    {
        return "Welcome User";
    }

    @RolesAllowed({"USER","ADMIN"})
    @RequestMapping("/admin")
    public String getAdmin()
    {
        return "Welcome Admin";
    }

    @RequestMapping("/*")
    public String getGithub(Principal user)
    {
        return "Welcome, " + collaborateurRepo.findByEmail(user.getName()).get().getNomutilisateur() ;
    }
    public String getUserInfo(Principal user) {
        StringBuffer userInfo= new StringBuffer();
        if(user instanceof UsernamePasswordAuthenticationToken){
            userInfo.append(getUsernamePasswordLoginInfo(user));
        }
        else if(user instanceof OAuth2AuthenticationToken){
            userInfo.append(getOauth2LoginInfo(user));
        }
        return userInfo.toString();
    }
    private StringBuffer getUsernamePasswordLoginInfo(Principal user)
    {
        StringBuffer usernameInfo = new StringBuffer();

        UsernamePasswordAuthenticationToken token = ((UsernamePasswordAuthenticationToken) user);
        if(token.isAuthenticated()){
            User u = (User) token.getPrincipal();
            usernameInfo.append("Welcome, " + u.getUsername());
        }
        else{
            usernameInfo.append("NA");
        }
        return usernameInfo;
    }
    private StringBuffer getOauth2LoginInfo(Principal user){

        StringBuffer protectedInfo = new StringBuffer();

        OAuth2AuthenticationToken authToken = ((OAuth2AuthenticationToken) user);
        OAuth2AuthorizedClient authClient = this.authorizedClientService.loadAuthorizedClient(authToken.getAuthorizedClientRegistrationId(), authToken.getName());
        if(authToken.isAuthenticated()){

            Map<String,Object> userAttributes = ((DefaultOAuth2User) authToken.getPrincipal()).getAttributes();

            String userToken = authClient.getAccessToken().getTokenValue();
            protectedInfo.append("Welcome, " + userAttributes.get("name")+"<br><br>");
            protectedInfo.append("e-mail: " + userAttributes.get("email")+"<br><br>");
            protectedInfo.append("Access Token: " + userToken+"<br><br>");
        }
        else{
            protectedInfo.append("NA");
        }
        return protectedInfo;
    }


}