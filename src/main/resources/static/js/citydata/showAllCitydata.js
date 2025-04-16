let congestionLegendOverlay = null;
let selectedPolygon = null;

function removeAreaNameControl() {
    const existingControl = document.querySelector('.area-name-control');
    if (existingControl && existingControl.parentNode) {
        existingControl.parentNode.removeChild(existingControl);
    }
}

function removeInfoIcons() {
    document.querySelectorAll('.info-icon').forEach(el => {
        if (el.parentNode) {
            el.parentNode.removeChild(el);
        }
    });
}


// 전체 장소 정보 및 혼잡도 조회
function getAllAreasWithCongestionLevel() {
    selectedPolygon = null;
    removeAreaNameControl(); // 장소명 컨트롤 제거
    removeInfoIcons(); // info-icon 제거

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
        useTemperature = false
    } = options;

    console.log("---")
    console.log("useCongestionLevel: ", useCongestionLevel);
    console.log("useTemperature: ", useTemperature);
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
        console.log(area.areaName + "의 areaId: " + area.areaId);

        // 지도 경계 조정
        adjustMapBounds(polygon);
    })
}

function getColorByCongestionLevel(level) {
    console.log("혼잡도 값(level):", level);

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
        showLabel(areaId); // ✅

    });

    polygon.addListener('mouseout', () => {
        // 클릭된 상태가 아니면 다시 연하게 변경
        if (selectedPolygon !== polygon) {
            polygon.setOptions(defaultStyle);
            hideLabel(areaId); // ✅
        }
    });

    polygon.addListener('click', () => {
        // 기존 선택된 폴리곤이 있다면 다시 연하게 변경
        if (selectedPolygon && selectedPolygon !== polygon) {
            selectedPolygon.setOptions(defaultStyle);
            const prevLabel = document.getElementById(`custom-label-${selectedPolygon.areaName}`);
            if (prevLabel) {
                prevLabel.style.opacity = "0.7";
                prevLabel.style.zIndex = "999";
            }
        }

        // 현재 클릭된 폴리곤을 선택한 상태(selectedPolygon)으로 저장
        selectedPolygon = polygon;
        polygon.areaName = areaname; // 폴리곤에 이름 저장해두기

        polygon.setOptions(hoverStyle);
        const labelDiv = document.getElementById(`custom-label-${areaId}`);
        if (labelDiv) {
            labelDiv.style.opacity = "1";
            labelDiv.style.zIndex = "1000";
        }

        areaName = areaname;
        createAreaNameControl(map, areaname);
        addInfoIcons(areaId);
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
            console.log("Showing label: ", label);

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
            console.log("Hiding label: ", label);

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

    const yOffset = isTemperatureLabel ? -15 : 5; // ✅ 겹치지 않도록 오프셋 조정

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

