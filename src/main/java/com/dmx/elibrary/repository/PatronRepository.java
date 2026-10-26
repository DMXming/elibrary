package com.dmx.elibrary.repository;

import com.dmx.elibrary.domain.Patron;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatronRepository extends JpaRepository<Patron, Long> {
}
