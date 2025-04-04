function weatherModal(data) {
    // 기존에 모달이 있으면 제거
    const existingModal = document.getElementById("weather-modal");
    if (existingModal) {
        existingModal.remove();
    }

    // 모달 HTML 동적 생성
    const modal = document.createElement("div");
    modal.id = "weather-modal";
    modal.className = "weather-modal";

    const modalContent = document.createElement("div");
    modalContent.className = "weather-modal-content";

    const closeButton = document.createElement("span");
    closeButton.className = "close-button";
    closeButton.innerHTML = "&times;";
    closeButton.onclick = function () {
        modal.remove();
    };

    const title = document.createElement("h5");
    title.innerText = `📍 ${areaName}의 날씨 현황`;

    const weatherText = `
        🌡️ 현재 온도: ${data.data.temperature}℃ (최고: ${data.data.maxTemperature}℃, 최저: ${data.data.minTemperature}℃)
        🌤️ 강수 관련 메시지 : ${data.data.pcpMsg}
        🌫️ 미세먼지: ${data.data.pm10}㎍/㎥ (${data.data.pm10Index})
        🌫️ 초미세먼지: ${data.data.pm25}㎍/㎥ (${data.data.pm25Index})
        - 날씨 데이터 업데이트 시간: ${data.data.weatherUpdateTime}
        
    `;

    const weatherElement = document.createElement("p");
    weatherElement.innerText = weatherText;

    // 모달 구조 구성
    modalContent.appendChild(closeButton);
    modalContent.appendChild(title);
    modalContent.appendChild(weatherElement);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    // 모달 바깥을 클릭하면 닫히도록 이벤트 추가
    modal.addEventListener("click", function (event) {
        if (event.target === modal) {
            modal.remove();
        }
    });
}
