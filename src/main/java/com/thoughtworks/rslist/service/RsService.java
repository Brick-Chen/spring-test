package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.exception.InvalidBuyException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final TradeRepository tradeRepository;

  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository,
                   VoteRepository voteRepository, TradeRepository tradeRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRepository = tradeRepository;
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
        || !userDto.isPresent()
        || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
        VoteDto.builder()
            .localDateTime(vote.getTime())
            .num(vote.getVoteNum())
            .rsEvent(rsEventDto.get())
            .user(userDto.get())
            .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }

  public void buy(Trade trade, int id) throws InvalidBuyException {
    Optional<RsEventDto> eventDto = rsEventRepository.findById(id);
    if (!eventDto.isPresent()) {
      throw new InvalidBuyException();
    }

    int rank = trade.getRank();
    Optional<TradeDto> history = tradeRepository.findById(rank);
    if (!history.isPresent()) {
      RsEventDto rsEventDto = eventDto.get();
      TradeDto tradeDto = TradeDto.builder()
              .amount(trade.getAmount())
              .rank(trade.getRank())
              .rsEventDto(rsEventDto)
              .build();
      tradeRepository.save(tradeDto);

      rsEventDto.setTradeDto(tradeDto);
      rsEventRepository.save(rsEventDto);
    }
    else {
      TradeDto tradeDto = history.get();
      if (trade.getAmount() < tradeDto.getAmount()) {
        throw new InvalidBuyException();
      }
      RsEventDto originRsEventDto = tradeDto.getRsEventDto();
      RsEventDto newRsEventDto = eventDto.get();

      tradeDto.setRsEventDto(newRsEventDto);
      newRsEventDto.setTradeDto(tradeDto);

      originRsEventDto.setTradeDto(null);
      rsEventRepository.deleteById(originRsEventDto.getId());

      tradeDto.setAmount(trade.getAmount());
      tradeRepository.save(tradeDto);
    }

  }
}
