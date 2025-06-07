package org.example.what_seoul.repository.area;

import java.util.List;

public interface AreaQueryRepository {
    List<String> findAreaNamesByUserId(Long userId);
}
