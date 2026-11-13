package com.Transporter.FreightFox.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transporters")
@Getter
@Setter
@NoArgsConstructor
public class Transporter {

    @Id
    private Long id;

    private String name;

    @OneToMany(mappedBy = "transporter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LaneQuote> laneQuotes = new ArrayList<>();

    public Transporter(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addLaneQuote(LaneQuote laneQuote) {
        laneQuote.setTransporter(this);
        laneQuotes.add(laneQuote);
    }
}
