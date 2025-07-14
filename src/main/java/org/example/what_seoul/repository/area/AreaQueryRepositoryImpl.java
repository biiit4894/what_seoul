package org.example.what_seoul.repository.area;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.controller.admin.dto.ResGetAreaListDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

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

    @Override
    public Slice<ResGetAreaListDTO> findAreasSlice(
            String areaName,
            Pageable pageable
    ) {

        BooleanExpression areaNameCondition = null;

        if (areaName != null) {
            areaNameCondition = area.areaName.containsIgnoreCase(areaName);
        }

        List<ResGetAreaListDTO> results = queryFactory
                .select(Projections.constructor(ResGetAreaListDTO.class,
                        area.id,
                        area.category,
                        area.areaCode,
                        area.areaName,
                        area.createdAt,
                        area.updatedAt,
                        area.deletedAt))
                .from(area)
                .where(areaNameCondition != null ? areaNameCondition : Expressions.asBoolean(true).isTrue())
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
