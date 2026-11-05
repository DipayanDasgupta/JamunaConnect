package edu.iitm.jamunaconnect.service;

import edu.iitm.jamunaconnect.domain.Notice;
import edu.iitm.jamunaconnect.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Office-managed notice board; public read, staff write. */
@Service
public class NoticeService {

    private final NoticeRepository notices;

    public NoticeService(NoticeRepository notices) {
        this.notices = notices;
    }

    @Transactional(readOnly = true)
    public List<Notice> list() {
        return notices.findAllByOrderByEventDateDescCreatedAtDesc();
    }

    @Transactional
    public Notice create(String title, String description, LocalDate eventDate,
                         String linkUrl, String imageUrl, String postedBy) {
        String cleanTitle = required(title, "Title", 160);
        String cleanDescription = required(description, "Description", 4000);
        if (eventDate == null) {
            throw new IllegalArgumentException("Event date is required");
        }
        Notice notice = new Notice();
        notice.setTitle(cleanTitle);
        notice.setDescription(cleanDescription);
        notice.setEventDate(eventDate);
        notice.setLinkUrl(httpUrl(linkUrl, "Link"));
        notice.setImageUrl(httpUrl(imageUrl, "Image"));
        notice.setPostedBy(required(postedBy, "Posted by", 64));
        notice.setCreatedAt(Instant.now());
        return notices.save(notice);
    }

    @Transactional
    public void delete(Long id) {
        if (!notices.existsById(id)) {
            throw new IllegalArgumentException("No notice " + id);
        }
        notices.deleteById(id);
    }

    private static String required(String value, String field, int max) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        if (cleaned.length() > max) {
            throw new IllegalArgumentException(field + " is too long");
        }
        return cleaned;
    }

    /** Rendered as an href, so only http(s) is accepted. */
    private static String httpUrl(String value, String field) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            URI uri = URI.create(cleaned);
            if ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {
                return cleaned;
            }
        } catch (IllegalArgumentException ignored) {
            // fall through to the friendly error below
        }
        throw new IllegalArgumentException(field + " must start with http:// or https://");
    }
}
