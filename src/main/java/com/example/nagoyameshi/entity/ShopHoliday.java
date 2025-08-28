package com.example.nagoyameshi.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "shop_holidays")
@Data
public class ShopHoliday {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@OneToOne
	@JoinColumn(name = "shop_id", unique = true)
	private Shop shop;
	
	@Column(name = "monday")
	private boolean monday;
	
	@Column(name = "tuesday")
	private boolean tuesday;
	
	@Column(name = "wednesday")
	private boolean wednesday;
	
	@Column(name = "thursday")
	private boolean thursday;
	
	@Column(name = "friday")
	private boolean friday;
	
	@Column(name = "saturday")
	private boolean saturday;
	
	@Column(name = "sunday")
	private boolean sunday;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;
	
	@Column(name = "updated_at", insertable = false, updatable = false)
	private Timestamp updatedAt;
}
