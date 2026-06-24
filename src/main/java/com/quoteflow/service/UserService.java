package com.quoteflow.service;

import com.quoteflow.dto.admin.EmployeRequest;
import com.quoteflow.dto.admin.EmployeResponse;
import com.quoteflow.entity.User;
import com.quoteflow.entity.enums.Role;
import com.quoteflow.exception.BusinessException;
import com.quoteflow.exception.ResourceNotFoundException;
import com.quoteflow.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

// Gestion des utilisateurs : creation/activation des comptes employes (admin). 

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public EmployeResponse creerEmploye(EmployeRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un compte existe deja avec cet email");
        }
        User employe = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.EMPLOYE)
                .actif(true)
                .build();
        return toResponse(userRepository.save(employe));
    }

    public List<EmployeResponse> listerEmployes() {
        return userRepository.findByRole(Role.EMPLOYE).stream().map(this::toResponse).toList();
    }

    // Bascule l'etat actif/inactif d'un employe. 
    
    public EmployeResponse changerActivation(Long employeId) {
        User employe = userRepository.findById(employeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employe introuvable (id=" + employeId + ")"));
        if (employe.getRole() != Role.EMPLOYE) {
            throw new BusinessException("Seuls les comptes employes peuvent etre actives/desactives");
        }
        employe.setActif(!employe.isActif());
        return toResponse(userRepository.save(employe));
    }

    public User getEmployeById(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe introuvable (id=" + id + ")"));
        if (u.getRole() != Role.EMPLOYE) {
            throw new BusinessException("L'utilisateur cible n'est pas un employe");
        }
        return u;
    }

    private EmployeResponse toResponse(User u) {
        return EmployeResponse.builder()
                .id(u.getId()).nom(u.getNom()).prenom(u.getPrenom())
                .email(u.getEmail()).actif(u.isActif()).dateCreation(u.getDateCreation())
                .build();
    }
}
