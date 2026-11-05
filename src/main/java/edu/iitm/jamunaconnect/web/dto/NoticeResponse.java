package edu.iitm.jamunaconnect.web.dto;

import edu.iitm.jamunaconnect.domain.Notice;

import java.time.Instant;
import java.time.LocalDate;

public record NoticeResponse(Long id, String title, String description, LocalDate eventDate,
                             String linkUrl, String imageUrl, String postedBy, Instant createdAt) {

    public static NoticeResponse from(Notice notice) {
        return new NoticeResponse(notice.getId(), notice.getTitle(), notice.getDescription(),
                notice.getEventDate(), notice.getLinkUrl(), notice.getImageUrl(),
                notice.getPostedBy(), notice.getCreatedAt());
    }
}
