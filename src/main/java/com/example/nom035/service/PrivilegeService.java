package com.example.nom035.service;

import com.example.nom035.entity.Privilege;
import com.example.nom035.repository.PrivilegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PrivilegeService {
    @Autowired
    private PrivilegeRepository privilegeRepository;

    public List<Privilege> getAllPrivileges() {
        return privilegeRepository.findAll();
    }

    public Optional<Privilege> getPrivilegeById(Long id) {
        return privilegeRepository.findById(id);
    }

    public Privilege savePrivilege(Privilege privilege) {
        return privilegeRepository.save(privilege);
    }

    public void deletePrivilege(Long id) {
        privilegeRepository.deleteById(id);
    }
}
