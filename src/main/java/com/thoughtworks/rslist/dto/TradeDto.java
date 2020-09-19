package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "trade")
public class TradeDto {
    @Id
    @Column(name = "id")
    private int rank;

    private int amount;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "rsEvent_id")
    private RsEventDto rsEventDto;
}
