package com.ruralhealth.resident.model;

// One row of the "residents" table
public class Resident {

    private Integer id;
    private String name;
    private Integer age;
    private String gender;
    private String village;
    private String phone;
    private String bloodGroup;
    private String medicalNotes;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getMedicalNotes() { return medicalNotes; }
    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }
}
