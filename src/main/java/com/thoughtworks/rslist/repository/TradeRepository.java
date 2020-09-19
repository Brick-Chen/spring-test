package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.TradeDto;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface TradeRepository extends PagingAndSortingRepository<TradeDto, Integer> {
    List<TradeDto> findAll();
//    Optional<TradeDto> findByRank(int rank);
}
