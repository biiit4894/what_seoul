package org.example.what_seoul.domain.citydata.weather;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 강수량 메시지
 */
@Entity
@Table(name = "pcp_msg_history")
@Getter
@NoArgsConstructor
public class PcpMsgHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pcp_msg", unique = true, nullable = false)
    private String pcpMsg;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PcpMsgHistory(String pcpMsg) {
        this.pcpMsg = pcpMsg;
        this.createdAt = LocalDateTime.now();
    }
}
