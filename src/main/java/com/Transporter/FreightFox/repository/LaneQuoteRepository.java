package com.Transporter.FreightFox.repository;

import com.Transporter.FreightFox.entity.LaneQuote;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LaneQuoteRepository extends JpaRepository<LaneQuote, Long> {

    @EntityGraph(attributePaths = {"transporter", "lane"})
    List<LaneQuote> findAllBy();
}
