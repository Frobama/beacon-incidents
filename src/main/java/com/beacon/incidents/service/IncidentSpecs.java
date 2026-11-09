package com.beacon.incidents.service;

import com.beacon.incidents.domain.Incident;
import com.beacon.incidents.domain.IncidentStatus;
import com.beacon.incidents.domain.Severity;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class IncidentSpecs {

    private IncidentSpecs() {
    }

    /** Todos los filtros son opcionales: null o vacío significa "sin filtro". */
    public static Specification<Incident> filter(IncidentStatus status, Severity severity, String service) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (severity != null) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }
            if (service != null && !service.isBlank()) {
                predicates.add(cb.equal(root.get("service"), service));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
