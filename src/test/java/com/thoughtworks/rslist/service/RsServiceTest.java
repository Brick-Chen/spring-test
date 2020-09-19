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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class RsServiceTest {
  RsService rsService;

  @Mock RsEventRepository rsEventRepository;
  @Mock UserRepository userRepository;
  @Mock VoteRepository voteRepository;
  @Mock TradeRepository tradeRepository;
  LocalDateTime localDateTime;
  Vote vote;
  Trade trade;

  @BeforeEach
  void setUp() {
    initMocks(this);
    rsService = new RsService(rsEventRepository, userRepository, voteRepository, tradeRepository);
    localDateTime = LocalDateTime.now();
    vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
    trade = Trade.builder().amount(100).rank(1).build();
  }

  @Test
  void shouldVoteSuccess() {
    // given

    UserDto userDto =
        UserDto.builder()
            .voteNum(5)
            .phone("18888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("xiaoli")
            .id(2)
            .build();
    RsEventDto rsEventDto =
        RsEventDto.builder()
            .eventName("event name")
            .id(1)
            .keyword("keyword")
            .voteNum(2)
            .user(userDto)
            .build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
    // when
    rsService.vote(vote, 1);
    // then
    verify(voteRepository)
        .save(
            VoteDto.builder()
                .num(2)
                .localDateTime(localDateTime)
                .user(userDto)
                .rsEvent(rsEventDto)
                .build());
    verify(userRepository).save(userDto);
    verify(rsEventRepository).save(rsEventDto);
  }

  @Test
  void shouldThrowExceptionWhenUserNotExist() {
    // given
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
    //when&then
    assertThrows(
        RuntimeException.class,
        () -> {
          rsService.vote(vote, 1);
        });
  }

  @Test
  void should_get_exception_when_buy_rsEvent_not_exist() {
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
    assertThrows(InvalidBuyException.class, () -> {
      rsService.buy(trade, 1);
    });
  }

  @Test
  void should_get_exception_when_amount_not_enough() {
    RsEventDto rsEventDto = RsEventDto.builder()
            .eventName("event")
            .keyword("key")
            .voteNum(2)
            .user(null)
            .tradeDto(null)
            .build();

    TradeDto tradeDto = TradeDto.builder()
            .rank(1)
            .amount(200)
            .rsEventDto(rsEventDto)
            .build();

    rsEventDto.setTradeDto(tradeDto);

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(tradeRepository.findById(1)).thenReturn(Optional.of(tradeDto));

    assertThrows(InvalidBuyException.class, () -> {
      rsService.buy(trade, 1);
    });
  }

  @Test
  void should_buy_a_rs_event_when_the_rank_not_exist() throws InvalidBuyException {
    RsEventDto rsEventDto = RsEventDto.builder()
            .eventName("event")
            .keyword("key")
            .voteNum(2)
            .user(null)
            .tradeDto(null)
            .build();

    TradeDto tradeDto = TradeDto.builder()
            .rank(1)
            .amount(100)
            .rsEventDto(rsEventDto)
            .build();


    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(tradeRepository.findById(1)).thenReturn(Optional.empty());

    rsService.buy(trade, 1);

    verify(tradeRepository).save(tradeDto);

    verify(rsEventRepository).save(RsEventDto.builder()
            .eventName("event")
            .keyword("key")
            .voteNum(2)
            .user(null)
            .tradeDto(tradeDto)
            .build());
  }

  @Test
  public void should_delete_original_rs_event_when_a_rank_is_bought_before() throws InvalidBuyException {
    RsEventDto rsEventDto = RsEventDto.builder()
            .id(1)
            .eventName("event")
            .keyword("key")
            .voteNum(2)
            .user(null)
            .tradeDto(null)
            .build();

    TradeDto tradeDto = TradeDto.builder()
            .amount(50)
            .rank(1)
            .rsEventDto(rsEventDto)
            .build();

    rsEventDto.setTradeDto(tradeDto);

    RsEventDto rsEventDtoOther = RsEventDto.builder()
            .id(2)
            .eventName("eventOther")
            .keyword("keyOther")
            .voteNum(3)
            .user(null)
            .tradeDto(null)
            .build();

    when(rsEventRepository.findById(2)).thenReturn(Optional.of(rsEventDtoOther));
    when(tradeRepository.findById(1)).thenReturn(Optional.of(tradeDto));

    rsService.buy(trade, rsEventDtoOther.getId());

    verify(rsEventRepository).deleteById(rsEventDto.getId());

    verify(tradeRepository).save(
            TradeDto.builder()
                    .amount(100)
                    .rank(1)
                    .rsEventDto(rsEventDtoOther)
                    .build()
    );
  }
}
