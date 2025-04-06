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

    // 모달 헤더 (타이틀 + 닫기 버튼)
    const modalHeader = document.createElement("div");
    modalHeader.className = "modal-header";

    const title = document.createElement("h5");
    title.innerText = `📍 ${areaName}의 날씨 현황`;

    const closeButton = document.createElement("span");
    closeButton.className = "close-button";
    closeButton.innerHTML = "&times;";
    closeButton.onclick = function () {
        modal.remove();
    };

    modalHeader.appendChild(title);
    modalHeader.appendChild(closeButton);

    const weatherGrid = document.createElement("div");
    weatherGrid.className = "weather-info-grid";

    const weatherItems = [
        {
            icon: "🌡️",
            title: "온도",
            text: `현재 ${data.data.temperature}℃ <span class="text-small">(최고 ${data.data.maxTemperature}℃ / 최저 ${data.data.minTemperature}℃)</span>`
        },
        {
            icon: "🌤️",
            title: "강수 관련 메시지",
            text: data.data.pcpMsg
        },
        {
            icon: "🌫️",
            title: "미세먼지",
            text: `${data.data.pm10}㎍/㎥ <span class="text-small">(${data.data.pm10Index})</span>`
        },
        {
            icon: "🌫️",
            title: "초미세먼지",
            text: `${data.data.pm25}㎍/㎥ <span class="text-small">(${data.data.pm25Index})</span>`
        }
    ];

    weatherItems.forEach(item => {
        const card = document.createElement("div");
        card.className = "weather-card";

        const icon = document.createElement("div");
        icon.className = "weather-icon";
        icon.textContent = item.icon;

        const details = document.createElement("div");
        details.className = "weather-details";
        details.innerHTML = `<strong>${item.title}</strong><span>${item.text}</span>`;

        card.appendChild(icon);
        card.appendChild(details);
        weatherGrid.appendChild(card);
    });

    // 마지막 업데이트 시간
    const updateTime = document.createElement("p");
    updateTime.style.marginTop = "12px";
    updateTime.style.fontSize = "0.85rem";
    updateTime.style.color = "#666";
    updateTime.innerText = `⏱️ 데이터 업데이트: ${data.data.weatherUpdateTime}`;

    // 모달 구조 구성
    modalContent.appendChild(modalHeader);
    modalContent.appendChild(weatherGrid);
    modalContent.appendChild(updateTime);

    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    // 모달 바깥을 클릭하면 닫히도록 이벤트 추가
    modal.addEventListener("click", function (event) {
        if (event.target === modal) {
            modal.remove();
        }
    });
}
