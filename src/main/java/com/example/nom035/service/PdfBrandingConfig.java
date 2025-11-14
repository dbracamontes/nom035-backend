package com.example.nom035.service;

public class PdfBrandingConfig {
    private String title;
    private String subtitle;
    private String companyName;
    private String footerText;
    private String primaryHex = "#2196F3"; // default blue
    private String secondaryHex = "#9C27B0"; // default purple
    private String logoClasspath; // e.g. "/branding/logo.png"
    private float logoWidth = 64f; // desired width in points

    public String getTitle() { return title; }
    public PdfBrandingConfig setTitle(String title) { this.title = title; return this; }
    public String getSubtitle() { return subtitle; }
    public PdfBrandingConfig setSubtitle(String subtitle) { this.subtitle = subtitle; return this; }
    public String getCompanyName() { return companyName; }
    public PdfBrandingConfig setCompanyName(String companyName) { this.companyName = companyName; return this; }
    public String getFooterText() { return footerText; }
    public PdfBrandingConfig setFooterText(String footerText) { this.footerText = footerText; return this; }
    public String getPrimaryHex() { return primaryHex; }
    public PdfBrandingConfig setPrimaryHex(String primaryHex) { this.primaryHex = primaryHex; return this; }
    public String getSecondaryHex() { return secondaryHex; }
    public PdfBrandingConfig setSecondaryHex(String secondaryHex) { this.secondaryHex = secondaryHex; return this; }
    public String getLogoClasspath() { return logoClasspath; }
    public PdfBrandingConfig setLogoClasspath(String logoClasspath) { this.logoClasspath = logoClasspath; return this; }
    public float getLogoWidth() { return logoWidth; }
    public PdfBrandingConfig setLogoWidth(float logoWidth) { this.logoWidth = logoWidth; return this; }
}
