package com.flentas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.flentas.model.AuditLogEntity;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity,Integer> {

}
