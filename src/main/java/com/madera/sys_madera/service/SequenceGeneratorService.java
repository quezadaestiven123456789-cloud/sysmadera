package com.madera.sys_madera.service;

import com.madera.sys_madera.model.NumberSequence;
import com.madera.sys_madera.repository.NumberSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SequenceGeneratorService {

    private final NumberSequenceRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long nextValue(String seqKey) {
        NumberSequence seq = repository.findBySeqKeyForUpdate(seqKey)
                .orElseThrow(() -> new RuntimeException("Secuencia no encontrada: " + seqKey));
        long next = seq.getNextVal();
        seq.setNextVal(next + 1);
        repository.save(seq);
        return next;
    }

}
