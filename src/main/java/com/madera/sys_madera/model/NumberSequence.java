package com.madera.sys_madera.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "number_sequences")
public class NumberSequence {

    @Id
    @Column(name = "seq_key", length = 30)
    private String seqKey;

    @Column(name = "next_val", nullable = false)
    private Long nextVal;

}
