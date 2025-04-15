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
        const polygon = drawPolygonWithCongestionLevel(area.polygonCoords, area.areaName, area.areaId, area.congestionLevel);
        console.log(area.areaName + "의 혼잡도: " + area.congestionLevel);

        // 지도 경계 조정
        adjustMapBounds(polygon);
    })
}

function drawPolygonWithCongestionLevel(coords, areaname, areaId, congestionLevel) {
    console.log("cooords: " + coords);
    console.log("areaName: " + areaname); // areaName 전역변수에 값을 재할당해야 하기 때문에 변수명 areaname으로 변경
    console.log("areaId: " + areaId);
    console.log("congestionLevel: " + congestionLevel);

    // 혼잡도별 색상 매핑
    const colorMap = {
        '붐빔': '#FF0000',
        '약간 붐빔': '#ff8000',
        '보통': 'rgba(17,110,1,0.66)',
        '여유': '#0059b0'
    };

    const color = colorMap[congestionLevel] || '#CCCCCC'; // 기본 회색
    console.log(areaname + "의 색상: " + color)

    const polygon = new google.maps.Polygon({
        paths: coords.map(coord => ({ lat: coord.lat, lng: coord.lon })),
        strokeColor: color,
        strokeOpacity: 0.6,
        strokeWeight: 1,
        fillColor: color,
        fillOpacity: 0.3
    });

    const defaultStyle = {
        strokeColor: color,
        strokeOpacity: 0.6,
        strokeWeight: 1,
        fillColor: color,
        fillOpacity: 0.3
    };

    const hoverStyle = {
        strokeColor: color,
        strokeOpacity: 0.8,
        strokeWeight: 3,
        fillColor: color,
        fillOpacity: 0.6
    };

    const center = getPolygonCenter(polygon);

    // 수정된: 라벨도 색상 적용해서 표시
    createCustomLabelWithCongestionLevel(map, center, areaname, color);

    polygon.setMap(map);

    polygon.addListener('mouseover', () => {
        polygon.setOptions(hoverStyle);
        const labelDiv = document.getElementById(`custom-label-${areaname}`);
        if (labelDiv) {
            labelDiv.style.opacity = "1";
            labelDiv.style.zIndex = "1000";
        }
    });

    polygon.addListener('mouseout', () => {
        polygon.setOptions(defaultStyle);
        const labelDiv = document.getElementById(`custom-label-${areaname}`);
        if (labelDiv) {
            labelDiv.style.opacity = "0.7";
            labelDiv.style.zIndex = "999";
        }
    });

    polygon.addListener('click', () => {
        areaName = areaname;
        createAreaNameControl(map, areaname);
        addInfoIcons(areaId);
    });

    polygons.push(polygon);
    return polygon;
}

// 폴리곤 중앙에 표현할 커스텀 라벨을 생성하고 지도에 붙이기
function createCustomLabelWithCongestionLevel(map, position, text, color) {
    const labelDiv = document.createElement("div");
    labelDiv.className = "custom-label";
    labelDiv.id = `custom-label-${text}`;
    labelDiv.textContent = text;

    labelDiv.style.position = "absolute";
    labelDiv.style.padding = "2px 6px";
    labelDiv.style.fontSize = "12px";
    labelDiv.style.color = color;
    labelDiv.style.backgroundColor = "white";
    labelDiv.style.border = `1px solid ${color}`;
    labelDiv.style.borderRadius = "4px";
    labelDiv.style.whiteSpace = "nowrap";
    labelDiv.style.pointerEvents = "auto";
    labelDiv.style.transform = "translate(-50%, -100%)";
    labelDiv.style.boxShadow = "0 1px 4px rgba(0,0,0,0.3)";
    labelDiv.style.zIndex = "999";
    labelDiv.style.opacity = "0.7";

    const overlay = new google.maps.OverlayView();
    overlay.onAdd = function () {
        this.getPanes().overlayMouseTarget.appendChild(labelDiv);
    };
    overlay.draw = function () {
        const projection = this.getProjection();
        if (!projection) return;

        const point = projection.fromLatLngToDivPixel(position);
        if (point) {
            labelDiv.style.left = `${point.x}px`;
            labelDiv.style.top = `${point.y}px`;
        }

        const zoom = map.getZoom();
        labelDiv.style.display = zoom >= LABEL_ZOOM_THRESHOLD ? "block" : "none";
    };
    overlay.onRemove = function () {
        if (labelDiv.parentNode) {
            labelDiv.parentNode.removeChild(labelDiv);
        }
    };

    overlay.setMap(map);
    customLabels.push(overlay);

    map.addListener("zoom_changed", () => overlay.draw());
}

