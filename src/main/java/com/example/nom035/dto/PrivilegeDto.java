package com.example.nom035.dto;

public class PrivilegeDto {
    private Long id;
    private String name;

    public PrivilegeDto() {
    }

    public PrivilegeDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
