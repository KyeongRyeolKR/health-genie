package com.example.healthgenie.boundedContext.routine.repository;
import com.example.healthgenie.boundedContext.routine.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutineRepository extends JpaRepository<Routine, Long> {
    List<Routine> findAllByMemberId(Long userId);
}
