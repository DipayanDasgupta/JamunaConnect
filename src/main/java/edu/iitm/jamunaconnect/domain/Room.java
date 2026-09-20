package edu.iitm.jamunaconnect.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "room")
public class Room {

    @Id
    @Column(name = "room_number", length = 16, nullable = false)
    private String roomNumber;

    @Column(name = "block", length = 32, nullable = false)
    private String block;

    @Column(name = "floor", nullable = false)
    private int floor;

    /** Comma-separated occupant names for shared rooms, denormalised for search. */
    @Column(name = "occupants", length = 512)
    private String occupants;

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getOccupants() {
        return occupants;
    }

    public void setOccupants(String occupants) {
        this.occupants = occupants;
    }
}