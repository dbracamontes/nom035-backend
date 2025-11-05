package com.example.nom035.dto;

public class CompanyParticipationSummaryDto {
    public Long companyId;
    public String companyName;
    public int totalEmployees;
    public int responded;
    public int pending;
    public int participationPercent;
    public String status;

    public CompanyParticipationSummaryDto(Long companyId, String companyName, int totalEmployees, int responded, int pending, int participationPercent, String status) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.totalEmployees = totalEmployees;
        this.responded = responded;
        this.pending = pending;
        this.participationPercent = participationPercent;
        this.status = status;
    }
}
