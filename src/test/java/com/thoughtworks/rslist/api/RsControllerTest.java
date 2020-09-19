package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RsControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired UserRepository userRepository;
  @Autowired RsEventRepository rsEventRepository;
  @Autowired VoteRepository voteRepository;
  @Autowired TradeRepository tradeRepository;
  private UserDto userDto;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    voteRepository.deleteAll();
    tradeRepository.deleteAll();
    rsEventRepository.deleteAll();
    userRepository.deleteAll();
    userDto =
        UserDto.builder()
            .voteNum(10)
            .phone("188888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("idolice")
            .build();
    objectMapper = new ObjectMapper();
  }

  @Test
  public void shouldGetRsEventList() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);

    mockMvc
        .perform(get("/rs/list"))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[0]", not(hasKey("user"))))
        .andExpect(status().isOk());
  }

  @Test
  public void shouldGetOneEvent() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.eventName", is("第一条事件")));
    mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.keyword", is("无分类")));
    mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.eventName", is("第二条事件")));
    mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.keyword", is("无分类")));
  }

  @Test
  public void shouldGetErrorWhenIndexInvalid() throws Exception {
    mockMvc
        .perform(get("/rs/4"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("invalid index")));
  }

  @Test
  public void shouldGetRsListBetween() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    mockMvc
        .perform(get("/rs/list?start=1&end=2"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")));
    mockMvc
        .perform(get("/rs/list?start=2&end=3"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")));
    mockMvc
        .perform(get("/rs/list?start=1&end=3"))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")))
        .andExpect(jsonPath("$[2].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[2].keyword", is("无分类")));
  }

  @Test
  public void shouldAddRsEventWhenUserExist() throws Exception {

    UserDto save = userRepository.save(userDto);

    String jsonValue =
        "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": " + save.getId() + "}";

    mockMvc
        .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
    List<RsEventDto> all = rsEventRepository.findAll();
    assertNotNull(all);
    assertEquals(all.size(), 1);
    assertEquals(all.get(0).getEventName(), "猪肉涨价了");
    assertEquals(all.get(0).getKeyword(), "经济");
    assertEquals(all.get(0).getUser().getUserName(), save.getUserName());
    assertEquals(all.get(0).getUser().getAge(), save.getAge());
  }

  @Test
  public void shouldAddRsEventWhenUserNotExist() throws Exception {
    String jsonValue = "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": 100}";
    mockMvc
        .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldVoteSuccess() throws Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
    rsEventDto = rsEventRepository.save(rsEventDto);

    String jsonValue =
        String.format(
            "{\"userId\":%d,\"time\":\"%s\",\"voteNum\":1}",
            save.getId(), LocalDateTime.now().toString());
    mockMvc
        .perform(
            post("/rs/vote/{id}", rsEventDto.getId())
                .content(jsonValue)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    UserDto userDto = userRepository.findById(save.getId()).get();
    RsEventDto newRsEvent = rsEventRepository.findById(rsEventDto.getId()).get();
    assertEquals(userDto.getVoteNum(), 9);
    assertEquals(newRsEvent.getVoteNum(), 1);
    List<VoteDto> voteDtos =  voteRepository.findAll();
    assertEquals(voteDtos.size(), 1);
    assertEquals(voteDtos.get(0).getNum(), 1);
  }

  @Test
  public void should_not_buy_a_rs_event_rank_when_a_rs_event_id_not_exist() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);

    Trade trade = Trade.builder().amount(50).rank(1).build();
    String json = objectMapper.writeValueAsString(trade);

    mockMvc.perform(post("/rs/buy/3")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void should_not_buy_a_rs_event_when_trade_amount_is_less_than_exists_amount() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);

    TradeDto tradeDto =
            TradeDto.builder().rank(1).amount(120).rsEventDto(rsEventDto).build();
    tradeRepository.save(tradeDto);

    rsEventDto.setTradeDto(tradeDto);
    rsEventRepository.save(rsEventDto);

    Trade trade = Trade.builder().amount(50).rank(1).build();
    String json = objectMapper.writeValueAsString(trade);

    mockMvc.perform(post("/rs/buy/" + rsEventDto.getId())
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void should_buy_a_rank_when_the_rank_not_exist() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);

    Trade trade = Trade.builder().amount(50).rank(1).build();
    String json = objectMapper.writeValueAsString(trade);

    mockMvc.perform(post("/rs/buy/" + rsEventDto.getId())
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    List<TradeDto> tradeDocs = tradeRepository.findAll();
    assertEquals(1, tradeDocs.size());
    assertEquals(rsEventDto.getId(), tradeDocs.get(0).getRsEventDto().getId());
    assertEquals(1, tradeDocs.get(0).getRank());
    assertEquals(50, tradeDocs.get(0).getAmount());
  }

  @Test
  public void should_buy_a_rs_event_rank_when_amount_greater_than_amount_before() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);

    TradeDto tradeDto =
            TradeDto.builder().rank(1).amount(120).rsEventDto(rsEventDto).build();
    tradeRepository.save(tradeDto);

    rsEventDto.setTradeDto(tradeDto);
    rsEventRepository.save(rsEventDto);

    RsEventDto rsEventDtoOther =
            RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDtoOther);

    Trade trade = Trade.builder().amount(150).rank(1).build();
    String json = objectMapper.writeValueAsString(trade);

    mockMvc.perform(post("/rs/buy/" + rsEventDtoOther.getId())
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    List<TradeDto> tradeDocs = tradeRepository.findAll();
    assertEquals(1, tradeDocs.size());
    assertEquals(rsEventDtoOther.getId(), tradeDocs.get(0).getRsEventDto().getId());
    assertEquals(1, tradeDocs.get(0).getRank());
    assertEquals(150, tradeDocs.get(0).getAmount());

    List<RsEventDto> rsEventDocs = rsEventRepository.findAll();
    assertEquals(1, rsEventDocs.size());
    assertEquals(rsEventDtoOther.getId(), rsEventDocs.get(0).getId());
  }

  @Test
  public void should_get_rs_event_list_in_order_without_rank_bought() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto1 =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).voteNum(4).build();

    RsEventDto rsEventDto2 =
            RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).voteNum(8).build();

    RsEventDto rsEventDto3 =
            RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).voteNum(7).build();

    rsEventRepository.save(rsEventDto1);
    rsEventRepository.save(rsEventDto2);
    rsEventRepository.save(rsEventDto3);

    mockMvc.perform(get("/rs/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].eventName", is("第二条事件")))
            .andExpect(jsonPath("$[0].voteNum", is(8)))
            .andExpect(jsonPath("$[1].eventName", is("第三条事件")))
            .andExpect(jsonPath("$[1].voteNum", is(7)))
            .andExpect(jsonPath("$[2].eventName", is("第一条事件")))
            .andExpect(jsonPath("$[2].voteNum", is(4)));
  }

  @Test
  public void should_get_rs_event_list_in_order_with_rank_bought() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto1 =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).voteNum(4).build();

    RsEventDto rsEventDto2 =
            RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).voteNum(8).build();

    RsEventDto rsEventDto3 =
            RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).voteNum(7).build();

    rsEventRepository.save(rsEventDto1);
    rsEventRepository.save(rsEventDto2);
    rsEventRepository.save(rsEventDto3);

    TradeDto tradeDto = TradeDto.builder().rank(1).amount(100).rsEventDto(rsEventDto1).build();
    tradeRepository.save(tradeDto);

    rsEventDto1.setTradeDto(tradeDto);
    rsEventRepository.save(rsEventDto1);

    mockMvc.perform(get("/rs/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
            .andExpect(jsonPath("$[0].voteNum", is(4)))
            .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
            .andExpect(jsonPath("$[1].voteNum", is(8)))
            .andExpect(jsonPath("$[2].eventName", is("第三条事件")))
            .andExpect(jsonPath("$[2].voteNum", is(7)));
  }
}
