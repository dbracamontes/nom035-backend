package com.example.nom035.entity;

import jakarta.persistence.*;
import java.util.Set;

@Entity
public class Privilege {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // e.g. VIEW_DASHBOARD, ASSIGN_SURVEY, ANSWER_SURVEY
    @ManyToMany(mappedBy = "privileges")
    private Set<Role> roles;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}
