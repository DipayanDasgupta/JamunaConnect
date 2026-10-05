package edu.iitm.jamunaconnect.web.dto;

import java.util.List;

/** Room lookup result with the residents of that room, roll numbers included. */
public record RoomResponse(String roomNumber, String block, int floor,
                           List<ResidentView> residents) {

    public record ResidentView(String rollNumber, String name) {
    }
}