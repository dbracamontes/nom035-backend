package com.example.nom035.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    public List<Employee> getEmployees() { return employees; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(length = 20, nullable = false)
    private String taxId;

    @Column(length = 50, nullable = false)
    private String folioMercantil;

    @Column(length = 150)
    private String cliente;

    @Column(length = 150)
    private String razonSocial;

    @Column(length = 150)
    private String representante;

    @Column(length = 255)
    private String domicilio;

    @Column(length = 150)
    private String sindicato;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private List<Employee> employees;

    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private List<CompanySurvey> companySurveys;

    @Column(name = "has_medica_leben_docs", nullable = false)
    private boolean hasMedicaLebenDocs = false;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getTaxId() { return taxId; }
        public void setTaxId(String taxId) { this.taxId = taxId; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getFolioMercantil() { return folioMercantil; }
    public void setFolioMercantil(String folioMercantil) { this.folioMercantil = folioMercantil; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getRepresentante() { return representante; }
    public void setRepresentante(String representante) { this.representante = representante; }
    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }
    public String getSindicato() { return sindicato; }
    public void setSindicato(String sindicato) { this.sindicato = sindicato; }
}