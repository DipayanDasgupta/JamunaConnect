package edu.iitm.jamunaconnect.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "resident")
public class Resident {

    @Id
    @Column(name = "roll_number", length = 16, nullable = false)
    private String rollNumber;

    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @Column(name = "room_number", length = 16)
    private String roomNumber;

    @Column(name = "contact", length = 64)
    private String contact;

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}