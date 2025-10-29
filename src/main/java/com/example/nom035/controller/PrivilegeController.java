package com.example.nom035.controller;

import com.example.nom035.entity.Privilege;
import com.example.nom035.service.PrivilegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/privileges")
public class PrivilegeController {
    @Autowired
    private PrivilegeService privilegeService;

    @GetMapping
    public List<Privilege> getAllPrivileges() {
        return privilegeService.getAllPrivileges();
    }

    @GetMapping("/{id}")
    public Optional<Privilege> getPrivilegeById(@PathVariable Long id) {
        return privilegeService.getPrivilegeById(id);
    }

    @PostMapping
    public Privilege createPrivilege(@RequestBody Privilege privilege) {
        return privilegeService.savePrivilege(privilege);
    }

    @PutMapping("/{id}")
    public Privilege updatePrivilege(@PathVariable Long id, @RequestBody Privilege privilege) {
        privilege.setId(id);
        return privilegeService.savePrivilege(privilege);
    }

    @DeleteMapping("/{id}")
    public void deletePrivilege(@PathVariable Long id) {
        privilegeService.deletePrivilege(id);
    }
}
