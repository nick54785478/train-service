package com.example.demo.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.booking.aggregate.TicketBooking;

@Repository
public interface TicketBookingRepository extends JpaRepository<TicketBooking, String> {

}
