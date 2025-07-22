let map, marker;
let latitude, longitude;
let polygons = [];
let areaId, areaName;
let customLabels = []; // 폴리곤의 중심에 표시된 모든 라벨을 추적
const LABEL_ZOOM_THRESHOLD = 12; // 이 줌 이상에서만 라벨 보이게 (구 단위를 벗어나면 라벨 사라짐)
// 지도 초기화 시 컨트롤 추가
let areaNameControl;
let areas = []; // 전체 장소 정보 관리

document.addEventListener("DOMContentLoaded", () => {
    const collapseBtn = document.getElementById("collapse-btn");
    const expandBtn = document.getElementById("expand-btn");
    const wrapperContent = document.querySelector(".wrapper-content");

    collapseBtn.addEventListener("click", () => {
        $(wrapperContent).slideUp(300, () => {
            collapseBtn.style.display = "none";
            expandBtn.style.display = "inline-block";
        });
    });

    expandBtn.addEventListener("click", () => {
        $(wrapperContent).slideDown(300, () => {
            expandBtn.style.display = "none";
            collapseBtn.style.display = "inline-block";
        });
    });
});


// navbar, buttonWrapper, map 3요소의 위치 정렬
function adjustLayout() {
    const navbar = document.querySelector(".navbar");
    const buttonWrapper = document.querySelector(".wrapper-1");
    const map = document.querySelector("#map");

    if (navbar && buttonWrapper) {
        const navbarHeight = navbar.offsetHeight; // 네비게이션 바의 실제 높이 가져오기
        buttonWrapper.style.top = `${navbarHeight}px`; // .button-text-wrapper를 navbar 아래에 배치
        map.style.top = `${navbarHeight}px`;
        map.style.height = `calc(100vh - ${navbarHeight}px)`; // 전체 높이에서 navbar 제외
    }

    const locationBtn = document.getElementById("current-location-btn");
    if (locationBtn) {
        locationBtn.addEventListener("click", () => {
            getGeoLocation(); // 이미 구현된 현위치 이동 함수 재사용쳑
        });
    }
}

window.onload = () => {
    fetch('/api/area/all',{
        method: 'GET',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            const areas = data.data.map(area => ({
                ...area,
                areaId: area.id
            }));
            clearCustomLabels();
            clearPolygons();
            showAllPolygons(areas); // 첫 로딩 시에는 폴리곤을 회색으로 표현
        });
}
window.addEventListener("load", adjustLayout);
window.addEventListener("resize", adjustLayout); // 브라우저 크기가 변경될 때도 적ㅇㅇ

// 키워드로 서울시 장소 검색
function handleKeywordEnter(event) {
    if (event.key === 'Enter') {
        event.preventDefault(); // 폼 제출 방지
        getAreaListByKeyword();
    }
}

function getAreaListByKeyword() {
    let keyword = document.getElementById("keyword").value;
    if (!keyword.trim()) {
        alert("검색어를 입력해주세요.");
        return;
    }

    fetch(`/api/area?query=${encodeURIComponent(keyword)}`, {
        method: 'GET',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            const searchResultsElement = document.getElementById('search-results');
            searchResultsElement.innerHTML = '';
            searchResultsElement.style.display = "block";

            if (!data.data || data.data.areaList.length === 0) {
                searchResultsElement.innerHTML = "검색 결과가 없습니다.";
            } else {
                // searchResultsElement.style.display = "block"; // 검색 결과 보이기

                data.data.areaList.forEach((area, index) => {
                    const areaElement = document.createElement('div');
                    areaElement.classList.add('search-result-item'); // 스타일 적용을 위한 클래스 추가
                    const areaLink = document.createElement('a');
                    areaLink.href = '#';
                    areaLink.innerText = `${index + 1}. ${area.areaName}`;
                    areaLink.onclick = function () {
                        removeCultureEventMarkers();

                        // 클릭한 장소의 id, 이름을 전역변수에 저장
                        areaId = area.id;
                        areaName = area.areaName;
                        showPolygon(area.polygonCoords, area.areaName, areaId);
                        searchResultsElement.style.display = "none"; // 선택 후 검색 결과 숨기기

                    };
                    areaElement.appendChild(areaLink);
                    searchResultsElement.appendChild(areaElement);
                });
            }
        })
        .catch(error => console.error("Error:", error));
}

// 검색어 및 검색 결과창 이외의 다른 곳 클릭 시 검색 결과 닫기
document.addEventListener("click", function(event) {
    const searchBox = document.getElementById("keyword");
    const searchResultsElement = document.getElementById("search-results");

    if (!searchResultsElement) return;

    if (event.target !== searchBox && !searchResultsElement.contains(event.target)) {
        searchResultsElement.style.display = "none";
    }
});


async function initMap(callback) {
    const { Map } = await google.maps.importLibrary("maps");
    const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");

    const defaultPosition = { lat: 37.5665, lng: 126.9780 };

    map = new Map(document.getElementById("map"), {
        zoom: 4,
        center: defaultPosition,
        mapId: "DEMO_MAP_ID",
    })

    marker = new AdvancedMarkerElement({
        map: map,
        position: defaultPosition,
        title: "Default Position (Seoul)",
    });

    // 페이지 로드 후 현위치 가져오기
    getGeoLocation();
}

function getGeoLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(updatePosition, showError, {
            enableHighAccuracy: true, // 높은 정확도 요청
            timeout: 5000, // 5초 이내 응답 없으면 실패
            maximumAge: 0 // 항상 최신 위치 가져오기
        });
    } else {
        document.getElementById("location").innerText = "이 브라우저에서는 위치 정보가 지원되지 않습니다.";
    }
}

function updatePosition(position) {
    latitude = position.coords.latitude;
    longitude = position.coords.longitude;
    const location = document.getElementById("location");

    getAddressFromCoords(latitude, longitude)
        .then(address => {
            location.innerHTML = `현위치: ${address}`;
        });
    const userPosition = new google.maps.LatLng(latitude, longitude);

    // 기존 마커 삭제 후 새로운 마커 추가
    if (marker) marker.setMap(null);
    marker = new google.maps.Marker({
        position: userPosition,
        map: map,
        title: "현재 위치",
    });

    // // 현위치 주소 가져오기 -> 장소명 컨트롤에 설정
    // getAddressFromCoords(latitude, longitude)
    //     .then(address => {
    //         updateAreaName(address); // 기본 주소로 설정
    //     });

    // 폴리곤과 현위치 모두 포함하는 bounds 계산
    const bounds = new google.maps.LatLngBounds();
    bounds.extend(userPosition);

    // 현재 표시된 폴리곤이 있다면 그 경계도 포함
    if (polygons.length > 0) {
        polygons.forEach(polygon => {
            polygon.getPath().forEach(coord => bounds.extend(coord));
        });
    }

    map.fitBounds(bounds);
}

// 좌표 -> 도로명 주소 변환 함수
function getAddressFromCoords(lat, lng) {
    return new Promise((resolve, reject) => {
        const geocoder = new google.maps.Geocoder();
        const latlng = { lat: parseFloat(lat), lng: parseFloat(lng) };

        geocoder.geocode({ location: latlng }, (results, status) => {
            if (status === "OK") {
                if (results[0]) {
                    resolve(results[0].formatted_address);
                } else {
                    resolve("주소를 찾을 수 없음");
                }
            } else {
                console.error("Geocoder failed due to: " + status);
                resolve("주소 검색 실패");
            }
        });
    });
}

// 현위치에서 가장 가까운 장소 리스트 조회
function getAreaListByCurrentLocation() {
    const requestData = {
        latitude: latitude,
        longitude: longitude
    };

    fetch('/api/area/location', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
        .then(response => response.json())
        .then(data => {
            const places = data.data.nearestPlaces;
            const cityDataElement = document.getElementById('citydata');

            cityDataElement.innerHTML = '';

            if (places.length === 0) {
                cityDataElement.innerHTML = "인근 장소를 찾을 수 없습니다.";
            } else {
                cityDataElement.innerHTML = "<strong>내 위치에서 가장 가까운 서울시 주요 장소들</strong><br><br>";

                places.forEach((place, index) => {
                    const placeElement = document.createElement('div');
                    const placeLink = document.createElement('a');
                    placeLink.href = '#';
                    placeLink.innerText = `${index + 1}. ${place.areaName}`;
                    placeLink.onclick = function () {
                        removeCultureEventMarkers();

                        // 클릭한 장소의 id, 이름을 전역변수에 저장
                        areaId = place.id;
                        areaName = place.areaName;
                        showPolygon(place.polygonCoords, place.areaName, areaId); // 클릭 시 폴리곤 표시
                    };

                    placeElement.appendChild(placeLink);
                    cityDataElement.appendChild(placeElement);
                });
            }
        })
        .catch(error => console.error("Error: ", error));

}

// 지도에 추가할 커스텀 컨트롤 요소 생성
function createAreaNameControl(map, areaName) {

    // 변수의 존재와 DOM에 실제로 요소가 포함되어있는지 여부를 함께 체크
    if (!areaNameControl || !document.body.contains(areaNameControl)) {
        areaNameControl = document.createElement("div");
        areaNameControl.className = 'area-name-control';
        Object.assign(areaNameControl.style, {
            backgroundColor: "#fff",
            border: "2px solid #ccc",
            borderRadius: "5px",
            padding: "10px 15px",
            margin: "10px",
            fontSize: "16px",
            fontWeight: "bold",
            boxShadow: "0px 2px 6px rgba(0,0,0,0.3)",
            textAlign: "center"
        });
        map.controls[google.maps.ControlPosition.TOP_RIGHT].push(areaNameControl);
    }
    areaNameControl.innerHTML = areaName;
}

// 폴리곤의 중심 찾기
function getPolygonCenter(polygon) {
    const paths = polygon.getPath().getArray();
    let lat = 0, lng = 0;

    paths.forEach(point => {
        lat += point.lat();
        lng += point.lng();
    });

    return new google.maps.LatLng(lat / paths.length, lng / paths.length);
}


// 폴리곤 중앙에 표현할 커스텀 라벨을 생성하고 지도에 붙이기
function createCustomLabel(map, position, text) {
    const labelDiv = document.createElement("div");
    labelDiv.className = "custom-label";
    labelDiv.id = `custom-label-${text}`;
    labelDiv.textContent = text;

    // 스타일은 자유롭게 조절 가능
    labelDiv.style.position = "absolute";
    labelDiv.style.padding = "2px 6px";
    labelDiv.style.fontSize = "12px";
    labelDiv.style.color = "#FF0000";
    labelDiv.style.backgroundColor = "white";
    labelDiv.style.border = "1px solid #FF0000";
    labelDiv.style.borderRadius = "4px";
    labelDiv.style.whiteSpace = "nowrap";
    labelDiv.style.pointerEvents = "auto"; // 포인터 이벤트 활성화
    labelDiv.style.transform = "translate(-50%, -100%)";
    labelDiv.style.boxShadow = "0 1px 4px rgba(0,0,0,0.3)";
    labelDiv.style.zIndex = "999";
    labelDiv.style.opacity = "0.7"; // 기본 불투명



    const overlay = new google.maps.OverlayView();
    overlay.onAdd = function () {
        this.getPanes().overlayMouseTarget.appendChild(labelDiv);
    };
    overlay.draw = function () {
        const projection = this.getProjection();
        if(!projection) return;

        const point = projection.fromLatLngToDivPixel(position);
        if (point) {
            labelDiv.style.left = `${point.x}px`;
            labelDiv.style.top = `${point.y}px`;
        }

        // 줌 레벨에 따라 보이거나 숨김
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

    // 줌 변경 시 다시 그리기 트리거
    map.addListener("zoom_changed", () => overlay.draw());
}

// 폴리곤에서 기존 라벨 제거
function clearCustomLabels() {
    customLabels.forEach(label => label.setMap(null));
    customLabels = [];
}

function showPolygon(polygonCoords, areaName, areaId) {
    // 기존 라벨 제거
    clearCustomLabels();

    // 기존 폴리곤 제거
    clearPolygons();

    // 새 폴리곤 그리기
    const polygon = drawPolygon(polygonCoords, areaName, areaId);

    // 지도 경계 조정
    adjustMapBounds(polygon);

    // 오른쪽 위에 장소명 표시할 컨트롤 생성
    createAreaNameControl(map, areaName);

    // 날씨, 혼잡도, 문화행사 아이콘 추가
    addInfoIcons(areaId);
}

// 1) 기존 폴리곤 제거
function clearPolygons() {
    polygons.forEach(polygon => polygon.setMap(null));
    polygons = [];  // 배열 초기화
}


// 2) 폴리곤 그리기
function drawPolygon(coords, areaName, areaId) {

    const polygon = new google.maps.Polygon({
        paths: coords.map(coord => ({ lat: coord.lat, lng: coord.lon })),
        strokeColor: "#FF0000",
        strokeOpacity: 0.6,
        strokeWeight: 1,
        fillColor: "#FF0000",
        fillOpacity: 0.3
    });

    // 폴리곤 기본 스타일
    const defaultStyle = {
        strokeOpacity: 0.6,
        strokeWeight: 1,
        fillOpacity: 0.3
    };

    // 폴리곤 마우스오버 시의 스타일
    const hoverStyle = {
        strokeOpacity: 0.8,
        strokeWeight: 3,
        fillOpacity: 0.6
    };

    // 폴리곤의 중심 좌표
    const center = getPolygonCenter(polygon);

    // 폴리곤의 중심에 장소명 라벨 표기하기
    createCustomLabel(map, center, areaName);

    polygon.setMap(map);

    // 폴리곤 마우스 오버할 때 더 진하게 표현
    polygon.addListener('mouseover', () => {
        polygon.setOptions(hoverStyle); // 폴리곤 더 진하게
        const labelDiv = document.getElementById(`custom-label-${areaName}`);
        if(labelDiv) {
            labelDiv.style.opacity = "1";
            labelDiv.style.zIndex = "1000";
        } // 라벨 더 진하게
    });

    // 폴리곤 마우스 아웃할 때 더 연하게 표현
    polygon.addListener('mouseout', () => {
        polygon.setOptions(defaultStyle); // 폴리곤 더 연하게
        const labelDiv = document.getElementById(`custom-label-${areaName}`);
        if(labelDiv) {
            labelDiv.style.opacity = "0.7";
            labelDiv.style.zIndex = "999";
        } // 라벨 더 연하게
    });

    // 폴리곤 클릭 시 도시데이터 아이콘 및 장소명 표기
    polygon.addListener('click', () => {
        createAreaNameControl(map, areaName);
        addInfoIcons(areaId);
    })

    // 폴리곤 마우스오버시 라벨 진하게
    // 폴리곤 마우스아웃시 라벨 연하게

    polygons.push(polygon);
    return polygon;
}

// 3) 지도 경계 조정

function adjustMapBounds(polygon) {
    const bounds = new google.maps.LatLngBounds();
    polygon.getPath().forEach(coord => bounds.extend(coord));
    map.fitBounds(bounds);
    map.setZoom(map.getZoom() - 1);  // 약간 축소
}

// 4) 아이콘 추가
function addInfoIcons(areaId) {
    // 이미 존재하는 경우 제거
    const existingIcons = document.querySelector(".info-icons");
    if (existingIcons) existingIcons.remove();

    const iconsContainer = document.createElement("div");
    iconsContainer.className = "info-icons";

    const icons = [
        { name: "날씨", engName: "weather", icon: "🌤️"},
        { name: "혼잡도", engName: "population", icon: "🚦"},
        { name: "문화행사", engName: "culture-event", icon: "🎭"}
    ];

    icons.forEach(({ name, engName, icon }) => {
        const iconElement = document.createElement("div");
        iconElement.className = "info-icon";
        iconElement.id = `${engName}-icon`
        iconElement.innerHTML = `${icon} <span>${name}</span>`;
        iconsContainer.appendChild(iconElement);
    });

    // areaNameControl.after(iconsContainer);
    // areaNameControl이 이미 DOM에 존재하면 그것을 그대로 사용
    if (areaNameControl) {
        // areaNameControl이 있을 때만 info-icons 컨테이너 추가
        areaNameControl.after(iconsContainer);
    }

    map.controls[google.maps.ControlPosition.TOP_RIGHT].push(iconsContainer);

    const weatherIcon = document.querySelector('#weather-icon');
    const populationIcon = document.querySelector('#population-icon');
    const cultureEventIcon = document.querySelector('#culture-event-icon')
    weatherIcon.addEventListener('click', (e) => fetchWeatherData(areaId));
    populationIcon.addEventListener('click', (e) => fetchPopulationData(areaId));
    cultureEventIcon.addEventListener('click', (e) => fetchCultureEventData(areaId));


}

function fetchWeatherData(id) {
    console.log('click');
    const areaId = Number(id);
    console.log(areaId);
    console.log(typeof areaId);
    fetch(`/api/citydata/weather/${areaId}`, {
        method: 'GET',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
    })
        .then(response => response.json())
        .then(data => {
            weatherModal(data);
        })
        .catch(error => console.error("Error: ", error));
}

function fetchPopulationData(id) {
    const areaId = Number(id);
    fetch(`/api/citydata/population/${areaId}`, {
        method: 'GET',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
    })
        .then(response => response.json())
        .then(data => {
            console.log("Response: ", data);
            populationModal(data);
        })
        .catch(error => console.error("Error: ", error));

}

function fetchCultureEventData(id) {
    const areaId = Number(id);
    fetch(`/api/citydata/event/${areaId}`, {
        method: 'GET',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
    })
        .then(response => response.json())
        .then(data => {
            cultureEventModal(data, areaName);
        })
        .catch(error => console.error("Error: ", error));

}


function showError(error) {
    let errorMessage = "";
    switch(error.code) {
        case error.PERMISSION_DENIED:
            errorMessage = "사용자가 위치 정보 제공을 거부했습니다.";
            break;
        case error.POSITION_UNAVAILABLE:
            errorMessage = "위치 정보를 사용할 수 없습니다.";
            break;
        case error.TIMEOUT:
            errorMessage = "위치 정보를 가져오는 데 시간이 초과되었습니다.";
            break;
        case error.UNKNOWN_ERROR:
            errorMessage = "알 수 없는 오류가 발생했습니다.";
            break;
    }
    document.getElementById("location").innerText = errorMessage;
}

window.initMap = initMap;