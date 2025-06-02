package org.example.what_seoul.repository.board;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.controller.board.dto.ResGetMyBoardDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static org.example.what_seoul.domain.board.QBoard.board;
import static org.example.what_seoul.domain.citydata.QArea.area;
import static org.example.what_seoul.domain.citydata.event.QCultureEvent.cultureEvent;

@RequiredArgsConstructor
public class BoardQueryRepositoryImpl implements BoardQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<ResGetMyBoardDTO> findSliceByUserId(Long userId, Pageable pageable) {

        List<ResGetMyBoardDTO> results = queryFactory
                .select(Projections.constructor(ResGetMyBoardDTO.class,
                        board.id,
                        board.content,
                        board.createdAt,
                        board.updatedAt,
                        cultureEvent.eventName,
                        cultureEvent.eventPlace,
                        cultureEvent.url,
                        area.areaName,
                        cultureEvent.isEnded))
                .from(board)
                .join(board.cultureEvent, cultureEvent)
                .join(cultureEvent.area, area)
                .where(board.user.id.eq(userId))
                .orderBy(board.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = results.size() > pageable.getPageSize();

        if (hasNext) {
            results.remove(results.size() - 1);
        }

        return new SliceImpl<>(results, pageable, hasNext);
    }
}
