/*
 * package com.flentas.controller;
 * 
 * import org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.web.bind.annotation.PostMapping; import
 * org.springframework.web.bind.annotation.RequestBody; import
 * org.springframework.web.bind.annotation.RequestMapping; import
 * org.springframework.web.bind.annotation.RestController;
 * 
 * import com.flentas.model.AuditLogEntity; import
 * com.flentas.service.AuditLogService;
 * 
 * @RestController
 * 
 * @RequestMapping("/api")
 * 
 * 
 * public class AuditLogController {
 * 
 * @Autowired AuditLogService auditLogService;
 * 
 * @PostMapping(value="/create") public AuditLogEntity createLog(@RequestBody
 * AuditLogEntity auditLogEntity) { AuditLogEntity
 * auditLog=auditLogService.createLog(auditLogEntity);
 * 
 * return auditLog;
 * 
 * } }
 */