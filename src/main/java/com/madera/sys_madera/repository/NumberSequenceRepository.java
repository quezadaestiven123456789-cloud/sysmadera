package com.madera.sys_madera.repository;

import com.madera.sys_madera.model.NumberSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NumberSequenceRepository extends JpaRepository<NumberSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ns FROM NumberSequence ns WHERE ns.seqKey = :seqKey")
    Optional<NumberSequence> findBySeqKeyForUpdate(@Param("seqKey") String seqKey);

}
