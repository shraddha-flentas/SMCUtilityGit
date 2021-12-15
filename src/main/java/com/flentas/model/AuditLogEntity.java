package com.flentas.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name="AuditLog")
public class AuditLogEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="log_Id")
	private int logId;
	
    @CreationTimestamp
	@Column(name="timestamp")
	private Timestamp timestamp;

	@Column(name="type")
	private String type;
	
	@Column(name="token")
	private String token;
	
	@Column(name="appl_id")
	private String applId;
	
	@Column(name="param1")
	private String param1;
	
	@Column(name="param2")
	private String param2;
	
	
	@Column(name="param3")
	private String param3;
	
	public AuditLogEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AuditLogEntity(int logId, Timestamp timestamp, String type, String token, String applId, String param1,
			String param2, String param3) {
		super();
		this.logId = logId;
		this.timestamp = timestamp;
		this.type = type;
		this.token = token;
		this.applId = applId;
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
	}

	public int getLogId() {
		return logId;
	}

	public void setLogId(int logId) {
		this.logId = logId;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getApplId() {
		return applId;
	}

	public void setApplId(String applId) {
		this.applId = applId;
	}

	public String getParam1() {
		return param1;
	}

	public void setParam1(String param1) {
		this.param1 = param1;
	}

	public String getParam2() {
		return param2;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}

	public String getParam3() {
		return param3;
	}

	public void setParam3(String param3) {
		this.param3 = param3;
	}

}