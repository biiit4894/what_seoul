package org.example.what_seoul.domain.board;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.user.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 문화행사 후기
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 후기 내용
     */
    @Column(nullable = false)
    private String content;

    /**
     * 후기 작성일시
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 후기 수정일시
     */
    @Column
    private LocalDateTime updatedAt;

    /**
     * 후기 작성자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 후기를 작성한 문화행사
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "culture_event_id", nullable = false)
    private CultureEvent cultureEvent;

    public Board(String content, User user, CultureEvent cultureEvent) {
        this.content = content;
        this.user = user;
        this.cultureEvent = cultureEvent;
    }

    public void updateBoard(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
}
