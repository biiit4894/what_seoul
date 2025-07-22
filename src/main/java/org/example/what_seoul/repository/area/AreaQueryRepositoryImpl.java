package org.example.what_seoul.repository.area;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.controller.admin.dto.ResGetAreaListDTO;
import org.example.what_seoul.controller.area.dto.AreaWithCongestionLevelDTO;
import org.example.what_seoul.controller.area.dto.AreaWithWeatherDTO;
import org.example.what_seoul.domain.citydata.QArea;
import org.example.what_seoul.domain.citydata.population.QPopulation;
import org.example.what_seoul.domain.citydata.weather.QWeather;
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

    @Override
    public List<AreaWithCongestionLevelDTO> findAllAreasWithCongestionLevel() {
        QPopulation p1 = QPopulation.population;
        QPopulation p2 = new QPopulation("p2");
        QArea a = area;

        return queryFactory
                .select(Projections.constructor(
                        AreaWithCongestionLevelDTO.class,
                        p1.id,
                        a.id,
                        a.areaName,
                        a.polygonWkt,
                        p1.congestionLevel
                ))
                .from(p1)
                .join(p1.area, a)
                .where(
                        a.deletedAt.isNull(),
                        p1.createdAt.eq(
                                JPAExpressions
                                        .select(p2.createdAt.max())
                                        .from(p2)
                                        .where(p2.area.id.
                                                eq(a.id))
                        )
                )
                .fetch();
    }

    @Override
    public List<AreaWithWeatherDTO> findAllAreasWithWeather() {
        QWeather w1 = QWeather.weather;
        QWeather w2 = new QWeather("q2");
        QArea a = area;

        return queryFactory
                .select(Projections.constructor(
                        AreaWithWeatherDTO.class,
                        w1.id,
                        a.id,
                        a.areaName,
                        a.polygonWkt,
                        w1.temperature,
                        w1.pcpMsg
                ))
                .from(w1)
                .join(w1.area, a)
                .where(
                        a.deletedAt.isNull(),
                        w1.createdAt.eq(
                                JPAExpressions
                                        .select(w2.createdAt.max())
                                        .from(w2)
                                        .where(w2.area.id.eq(a.id))
                        )
                )
                .fetch();
    }


}
