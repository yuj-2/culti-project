package com.culti.booking.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.culti.booking.dto.PerformanceSeatResponseDTO;
import com.culti.booking.dto.SeatInfoDTO;
import com.culti.booking.entity.ScheduleSeat;
import com.culti.booking.entity.Seat;
import com.culti.booking.repository.ScheduleSeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PerformanceSeatService {

    private final ScheduleSeatRepository scheduleSeatRepository;

    public List<PerformanceSeatResponseDTO> getSeats(Long scheduleId){

        List<ScheduleSeat> scheduleSeats =
                scheduleSeatRepository.findAllWithSeatByScheduleId(scheduleId);

        return scheduleSeats.stream()
                .map(ss -> {

                    Seat seat = ss.getSeat();

                    SeatInfoDTO seatInfo = new SeatInfoDTO(
                            seat.getSeatId(),
                            seat.getSeatRow(),
                            seat.getSeatCol(),
                            seat.getGrade(),
                            seat.getFloor()
                    );

                    return new PerformanceSeatResponseDTO(
                            seatInfo,
                            ss.getStatus()
                    );

                }).toList();
    }
}