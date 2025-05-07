function cultureEventSingleModal(events) {
    const existingModal = document.getElementById("culture-event-modal");
    if (existingModal) {
        existingModal.remove();
    }

    const modal = document.createElement("div");
    modal.id = "culture-event-modal";
    modal.className = "culture-event-modal";

    const modalContent = document.createElement("div");
    modalContent.className = "culture-event-modal-content";

    const modalHeader = document.createElement("div");
    modalHeader.className = "modal-header";

    const title = document.createElement("h5");
    title.innerText = `🎭 문화행사 상세 정보`

    const closeButton = document.createElement("span");
    closeButton.className = "close-button";
    closeButton.innerHTML = "&times;";
    closeButton.onclick = function () {
        modal.remove();
    };

    modalHeader.appendChild(title);
    modalHeader.appendChild(closeButton);

    const eventContainer = document.createElement("div");
    eventContainer.className = "event-container";

    // 주소 요소를 미리 생성해 놓음 (비어있는 상태)
    const addressElem = document.createElement("p");
    addressElem.innerText = "📌 위치: 주소를 불러오는 중...";
    addressElem.style.color = "#777";
    addressElem.style.fontSize = "0.85em";
    addressElem.style.marginBottom = "8px";

    // 주소 요소를 eventContainer에 먼저 추가해 둠
    eventContainer.appendChild(addressElem);

    getAddressFromCoords(events[0].eventY, events[0].eventX) // 좌표값을 도로명주소로 변환
        .then(address => {
            addressElem.innerText = `📌 위치: ${address}`;
        })
        .catch(() => {
            addressElem.innerText = "📌 위치: 주소를 불러올 수 없습니다.";

        });

    events.forEach(event => {
        const eventCard = document.createElement("div");
        eventCard.className = "event-card";

        const eventImg = document.createElement("img");
        eventImg.src = event.thumbnail;
        eventImg.alt = event.eventName;
        eventImg.className = "event-thumbnail";

        const eventInfo = document.createElement("div");
        eventInfo.className = "event-info";

        const eventName = document.createElement("h6");
        eventName.innerText = `🎪 ${event.eventName}`;

        const eventPeriod = document.createElement("p");
        eventPeriod.innerText = `📅 일정: ${event.eventPeriod}`;

        const eventPlace = document.createElement("p");
        eventPlace.innerText = `📍 장소: ${event.eventPlace}`;

        const eventLink = document.createElement("a");
        eventLink.href = event.url;
        eventLink.target = "_blank";
        eventLink.innerText = "🔗 상세 정보 보기";

        eventInfo.appendChild(eventName);
        eventInfo.appendChild(eventPeriod);
        eventInfo.appendChild(eventPlace);
        eventInfo.appendChild(eventLink);

        eventCard.appendChild(eventImg);
        eventCard.appendChild(eventInfo);
        eventContainer.appendChild(eventCard);
    });

    modalContent.appendChild(modalHeader);
    modalContent.appendChild(eventContainer);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    modal.addEventListener("click", function (event) {
        if (event.target === modal) {
            modal.remove();
        }
    });
}
