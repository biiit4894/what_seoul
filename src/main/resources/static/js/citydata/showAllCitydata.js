// 전체 장소 정보 조회
function getAllAreas() {
    fetch('/api/area/all', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            const areas = data.data;

            clearCustomLabels();
            clearPolygons();
            // TODO: 전체 장소 폴리곤 또는 마커 표기 + 혼잡도 마커로 표기
            showAllPolygons(areas);

        })
        .catch(error => console.error("Error:", error));
}

function showAllPolygons(areas) {
    // 기존 라벨 제거
    // clearCustomLabels();

    // 기존 폴리곤 제거
    // clearPolygons();

    areas.forEach((area, index) => {
        // 새 폴리곤 그리기
        const polygon = drawPolygon(area.polygonCoords, area.areaName);

        // 지도 경계 조정
        adjustMapBounds(polygon);
    })
}