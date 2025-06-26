let congestionLegendOverlay = null;
let selectedPolygon = null;
let cultureEventMarkers = [];

function removeAreaNameControl() {
    const existingControl = document.querySelector('.area-name-control');
    if (existingControl && existingControl.parentNode) {
        existingControl.parentNode.removeChild(existingControl);
    }
    areaNameControl = null;
}

function removeInfoIcons() {
    document.querySelectorAll('.info-icon').forEach(el => {
        if (el.parentNode) {
            el.parentNode.removeChild(el);
        }
    });
}

function removeCultureEventMarkers() {
    console.log("removeCultureEventMarkers");
    cultureEventMarkers.forEach(marker => marker.setMap(null));
    cultureEventMarkers = [];
}

// 전체 장소 정보 및 혼잡도 조회
function getAllAreasWithCongestionLevel() {
    selectedPolygon = null;
    removeAreaNameControl(); // 장소명 컨트롤 제거
    removeInfoIcons(); // info-icon 제거
    removeCultureEventMarkers(); // 문화행사 마커 제거

    document.getElementById('citydata').innerHTML = '';

    fetch('/api/area/all/ppltn', {
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
            showAllPolygons(areas, { useCongestionLevel: true });
            createLegendOverlay(map); // 지도에 혼잡도 범례 표시

        })
        .catch(error => console.error("Error:", error));
}

function getAllAreasWithWeather() {
    selectedPolygon = null;
    removeAreaNameControl(); // 장소명 컨트롤 제거
    removeInfoIcons(); // info-icon 제거
    removeLegendOverlay(); // 혼잡도 범례 제거
    removeCultureEventMarkers(); // 문화행사 마커 제거

    document.getElementById('citydata').innerHTML = '';

    fetch(`/api/area/all/weather`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(response => response.json())
        .then(data => {
            const areas = data.data;
            console.log(areas);
            clearCustomLabels();
            clearPolygons();
            showAllPolygons(areas, { useTemperature : true });

        })
        .catch(error => console.error("Error:", error));

}

function getAllAreasWithCultureEvent() {
    selectedPolygon = null;
    removeAreaNameControl(); // 장소명 컨트롤 제거
    removeInfoIcons(); // info-icon 제거
    removeLegendOverlay(); // 혼잡도 범례 제거

    document.getElementById('citydata').innerHTML = '';

    fetch(`/api/area/all/event`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(response => response.json())
        .then(data => {
            const areas = data.data;
            console.log(areas);
            clearCustomLabels();
            clearPolygons();
            showAllPolygons(areas, { useCultureEvent : true});

        })
        .catch(error => console.error("Error:", error));
}
// 혼잡도 범례 제거
function removeLegendOverlay() {
    const existingLegend = document.querySelector('.legend-control');
    if (existingLegend && existingLegend.parentNode) {
        existingLegend.parentNode.removeChild(existingLegend);
    }
}

// 혼잡도 범례 생성
function createLegendOverlay(map) {
    // 기존 컨트롤 제거 (중복 방지)
    const existingLegend = document.querySelector('.legend-control');
    if (existingLegend) {
        map.controls[google.maps.ControlPosition.TOP_RIGHT].forEach((el, idx) => {
            if (el.className === 'legend-control') {
                map.controls[google.maps.ControlPosition.TOP_RIGHT].removeAt(idx);
            }
        });
    }

    const legendDiv = document.createElement("div");
    legendDiv.className = "legend-control";
    legendDiv.innerHTML = `  
        <strong>혼잡도 지표</strong><br>
        <span style="color: #FF0000;">■ 붐빔</span><br>
        <span style="color: #ff8000;">■ 약간 붐빔</span><br>
        <span style="color: rgba(17,110,1,0.66);">■ 보통</span><br>
        <span style="color: #0059b0;">■ 여유</span>
    `;

    Object.assign(legendDiv.style, {
        backgroundColor: "white",
        border: "1px solid #ccc",
        borderRadius: "5px",
        padding: "10px",
        fontSize: "13px",
        margin: "10px",
        boxShadow: "0px 2px 6px rgba(0,0,0,0.3)",
    });

    // 👉 지도 오른쪽 상단에 추가 (전체 화면 보기 버튼 바로 아래)
    map.controls[google.maps.ControlPosition.TOP_RIGHT].push(legendDiv);
}

function showAllPolygons(areas, options = {}) {
    const {
        useCongestionLevel = false,
        useTemperature = false,
        useCultureEvent = false
    } = options;

    console.log("---")
    console.log("useCongestionLevel: ", useCongestionLevel);
    console.log("useTemperature: ", useTemperature);
    console.log("useCultureEvent: ", useCultureEvent);
    console.log("---")

    areas.forEach((area) => {
        const color = useCongestionLevel
            ? getColorByCongestionLevel(area.congestionLevel)
            : '#FF0000';

        // 새 폴리곤 그리기
        const polygon = drawPolygonWithOptions(
            area.polygonCoords,
            area.areaName,
            area.areaId,
            area.congestionLevel,
            area.temperature,
            color,
            useTemperature
        );

        // 지도 경계 조정
        adjustMapBounds(polygon);

        // 문화행사 데이터가 있을 경우 문화행사 정보 표시
        if (useCultureEvent && area.cultureEventList && area.cultureEventList.length > 0) {
            area.cultureEventList.forEach(event => {
                // 문화행사의 X, Y 좌표를 사용하여 점을 찍음
                const eventLocation = new google.maps.LatLng(event.eventY, event.eventX);
                const eventMarker = new google.maps.Marker({
                    position: eventLocation,
                    map: map,
                    title: event.eventName,
                    icon: {
                        path: google.maps.SymbolPath.CIRCLE,
                        scale: 6,
                        fillColor: '#FFD700',
                        fillOpacity: 1,
                        strokeColor: '#FFA500',
                        strokeWeight: 1
                    }
                });

                cultureEventMarkers.push(eventMarker);

                // 점에 대한 클릭 이벤트 처리
                eventMarker.addListener("click", () => {
                    const sameLocationEvents = area.cultureEventList.filter(e =>
                        e.eventX === event.eventX && e.eventY === event.eventY
                    );
                    cultureEventSingleModal(area.areaName, sameLocationEvents);
                });

            });
        }
    })
}

function getColorByCongestionLevel(level) {
    // 혼잡도별 색상 매핑
    const colorMap = {
        '붐빔': '#FF0000',
        '약간 붐빔': '#ff8000',
        '보통': 'rgba(17,110,1,0.66)',
        '여유': '#0059b0'
    };
    return colorMap[level] || '#FF0000';
}

function drawPolygonWithOptions(
    coords,
    areaname,
    areaId,
    congestionLevel,
    temperature,
    color,
    useTemperature
) {
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

    // ✅ 스타일 복원을 위한 개별 저장
    polygon.__defaultStyle = { ...defaultStyle };
    polygon.__hoverStyle = { ...hoverStyle };

    const center = getPolygonCenter(polygon);
    if (useTemperature && temperature !== undefined) {
        // 온도 라벨 (검은 글씨, 테두리+배경)
        createCustomLabelWithOptions(map, center, `${temperature}℃`, 'black', areaId, true);

        // 장소명 라벨 (검은 글씨, 투명 배경)
        createCustomLabelWithOptions(map, center, areaname, 'black', `${areaId}-name`, false);
    } else {
        // 장소명 라벨만
        createCustomLabelWithOptions(map, center, areaname, color, areaId, false);
    }

    polygon.setMap(map);

    polygon.addListener('mouseover', () => {
        polygon.setOptions(hoverStyle);
        showLabel(areaId);

    });

    polygon.addListener('mouseout', () => {
        // 클릭된 상태가 아니면 다시 연하게 변경
        if (selectedPolygon !== polygon) {
            polygon.setOptions(polygon.__defaultStyle);
            hideLabel(areaId);
        }
    });

    polygon.addListener('click', () => {
        // 기존 선택된 폴리곤이 있다면 다시 연하게 변경
        if (selectedPolygon && selectedPolygon !== polygon) {
            selectedPolygon.setOptions(selectedPolygon.__defaultStyle); // 개별 스타일 복원
            const prevLabel = document.getElementById(`custom-label-${selectedPolygon.areaName}`);
            if (prevLabel) {
                prevLabel.style.opacity = "0.7";
                prevLabel.style.zIndex = "999";
            }
        }

        // 현재 클릭된 폴리곤을 선택한 상태(selectedPolygon)으로 저장
        selectedPolygon = polygon;
        polygon.areaName = areaname; // 폴리곤에 이름 저장해두기

        polygon.setOptions(polygon.__hoverStyle);
        const labelDiv = document.getElementById(`custom-label-${areaId}`);
        if (labelDiv) {
            labelDiv.style.opacity = "1";
            labelDiv.style.zIndex = "1000";
        }

        areaName = areaname;
        console.log("# areaname from drawPolygonWithOptions : ", areaname);
        createAreaNameControl(map, areaname);
        addInfoIcons(areaId);

        // 💡 현위치 + 폴리곤 함께 보기
        if (latitude && longitude) {
            const bounds = new google.maps.LatLngBounds();
            polygon.getPath().forEach(coord => bounds.extend(coord));
            bounds.extend(new google.maps.LatLng(latitude, longitude));
            map.fitBounds(bounds);
        }
    });

    polygons.push(polygon);
    return polygon;
}

function showLabel(areaId) {
    const labels = [
        document.getElementById(`custom-label-${areaId}`),
        document.getElementById(`custom-label-${areaId}-name`)
    ];
    labels.forEach(label => {
        if (label) {
            label.style.opacity = "1";
            label.style.zIndex = "1000";
        }
    });
}

function hideLabel(areaId) {
    const labels = [
        document.getElementById(`custom-label-${areaId}`),
        document.getElementById(`custom-label-${areaId}-name`)
    ];
    labels.forEach(label => {
        if (label) {
            label.style.opacity = "0.7";
            label.style.zIndex = "999";
        }
    });
}


// 폴리곤 중앙에 표현할 커스텀 라벨을 생성하고 지도에 붙이기
function createCustomLabelWithOptions(map, position, text, color, labelId, isTemperatureLabel) {
    const labelDiv = document.createElement("div");
    labelDiv.className = "custom-label";
    labelDiv.id = `custom-label-${labelId}`;
    labelDiv.textContent = text;

    const yOffset = isTemperatureLabel ? -15 : 5; // 겹치지 않도록 오프셋 조정

    labelDiv.style.position = "absolute";
    labelDiv.style.padding = "2px 6px";
    labelDiv.style.fontSize = "12px";
    labelDiv.style.color = color;
    labelDiv.style.borderRadius = "4px";
    labelDiv.style.whiteSpace = "nowrap";
    labelDiv.style.pointerEvents = "auto";
    labelDiv.style.transform = "translate(-50%, -100%)";
    labelDiv.style.boxShadow = "0 1px 4px rgba(0,0,0,0.3)";
    labelDiv.style.zIndex = "2000"; // 라벨의 zIndex를 높게 설정
    labelDiv.style.opacity = "0.7";

    if (isTemperatureLabel) {
        // 온도 라벨: 배경/테두리 O
        labelDiv.style.backgroundColor = "white";
        labelDiv.style.border = `1px solid ${color}`;
    } else {
        // 장소명 라벨: 테두리 X
        labelDiv.style.backgroundColor = "white";
        labelDiv.style.border = "none";
    }

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
            labelDiv.style.top = `${point.y + yOffset}px`;
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

