package com.example.pillulkin.data.remote.model;

public class CustomMedicineRequest {
    private String name;
    private String dosage;
    private String form;
    private String activeSubstance;
    private String indications;
    private String contraindications;
    private String expirationDate;
    private String quantity;

    public CustomMedicineRequest(String name, String dosage, String form,
                                  String activeSubstance, String indications,
                                  String contraindications, String expirationDate,
                                  String quantity) {
        this.name = name;
        this.dosage = dosage;
        this.form = form;
        this.activeSubstance = activeSubstance;
        this.indications = indications;
        this.contraindications = contraindications;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getForm() { return form; }
    public String getActiveSubstance() { return activeSubstance; }
    public String getIndications() { return indications; }
    public String getContraindications() { return contraindications; }
    public String getExpirationDate() { return expirationDate; }
    public String getQuantity() { return quantity; }
}