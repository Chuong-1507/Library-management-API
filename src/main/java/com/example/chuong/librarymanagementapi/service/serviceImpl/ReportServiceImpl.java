package com.example.chuong.librarymanagementapi.service.serviceImpl;

import com.example.chuong.librarymanagementapi.dto.response.ActiveUserResponse;
import com.example.chuong.librarymanagementapi.dto.response.BorrowReportResponse;
import com.example.chuong.librarymanagementapi.dto.response.FineReportResponse;
import com.example.chuong.librarymanagementapi.dto.response.PopularBookResponse;
import com.example.chuong.librarymanagementapi.entity.Enum.Status;
import com.example.chuong.librarymanagementapi.repository.BorrowRepository;
import com.example.chuong.librarymanagementapi.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final BorrowRepository borrowRepository;

    @Override
    public BorrowReportResponse getBorrowReport(LocalDate from, LocalDate to) {
        return BorrowReportResponse.builder()
                .totalBorrows(borrowRepository.countTotalBorrows(from,to))
                .currentlyBorrowed(borrowRepository.countByStatusInRange(Status.BORROWING, from, to))
                .overdue(borrowRepository.countByStatusInRange(Status.OVERDUE,from,to))
                .returned(borrowRepository.countByStatusInRange(Status.RETURNED,from,to))
                .build();
    }

    @Override
    public FineReportResponse getFineReport(LocalDate from, LocalDate to) {
        BigDecimal total = borrowRepository.sumFines(from,to);
        long count = borrowRepository.countBorrowsWithFine(from,to);
        BigDecimal average = count > 0
                ? total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return FineReportResponse.builder()
                .totalFines(total)
                .borrowsWithFine(count)
                .averageFine(average)
                .build();
    }

    @Override
    public List<PopularBookResponse> getPopularBooks(LocalDate from, LocalDate to, int limit) {
        return borrowRepository.findPopularBooks(from,to, PageRequest.of(0,limit) );
    }

    @Override
    public List<ActiveUserResponse> getActiveUsers(LocalDate from, LocalDate to, int limit) {

        return borrowRepository.findActiveUsers(from,to,PageRequest.of(0,limit));
    }
}
