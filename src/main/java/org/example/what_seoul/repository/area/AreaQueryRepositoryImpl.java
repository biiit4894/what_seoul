package org.example.what_seoul.repository.area;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static org.example.what_seoul.domain.board.QBoard.board;
import static org.example.what_seoul.domain.citydata.QArea.area;
import static org.example.what_seoul.domain.citydata.event.QCultureEvent.cultureEvent;

@RequiredArgsConstructor
@Slf4j
public class AreaQueryRepositoryImpl implements AreaQueryRepository {
    private final JPAQueryFactory queryFactory;


    @Override
    public List<String> findAreaNamesByUserId(Long userId) {
        return queryFactory
                .select(area.areaName)
                .distinct()
                .from(board)
                .join(board.cultureEvent, cultureEvent)
                .join(cultureEvent.area, area)
                .where(board.user.id.eq(userId))
                .fetch();
    }
}
