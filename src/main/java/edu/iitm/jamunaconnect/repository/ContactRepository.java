package edu.iitm.jamunaconnect.repository;

import edu.iitm.jamunaconnect.domain.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findAllByOrderByDisplayOrderAsc();
}