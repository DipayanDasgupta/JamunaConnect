package edu.iitm.jamunaconnect.repository;

import edu.iitm.jamunaconnect.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, String> {

    /**
     * pg_trgm fuzzy search. Ranks by similarity of both the room number and the
     * occupant names, so a misspelt name or wrong block prefix still resolves.
     * Only valid on PostgreSQL (the production and integration-test engine).
     */
    @Query(value = """
            SELECT r.* FROM room r
            WHERE r.room_number ILIKE '%' || :query || '%'
               OR r.occupants   ILIKE '%' || :query || '%'
               OR similarity(r.room_number, :query) > 0.25
               OR similarity(r.occupants,  :query) > 0.25
            ORDER BY greatest(
                       similarity(r.room_number, :query),
                       similarity(r.occupants,   :query)
                     ) DESC, r.room_number
            LIMIT 25
            """, nativeQuery = true)
    List<Room> searchFuzzy(@Param("query") String query);

    /** Portable fallback for H2/local runs; plain substring match. */
    @Query("""
            SELECT r FROM Room r
            WHERE lower(r.roomNumber) LIKE lower(concat('%', :query, '%'))
               OR lower(r.occupants)   LIKE lower(concat('%', :query, '%'))
            ORDER BY r.roomNumber
            """)
    List<Room> searchLike(@Param("query") String query);
}