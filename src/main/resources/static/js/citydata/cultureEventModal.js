function cultureEventModal(data) {
    // 기존에 모달이 있으면 제거
    const existingModal = document.getElementById("culture-event-modal");
    if (existingModal) {
        existingModal.remove();
    }

    // 모달 HTML 동적 생성
    const modal = document.createElement("div");
    modal.id = "culture-event-modal";
    modal.className = "culture-event-modal";

    const modalContent = document.createElement("div");
    modalContent.className = "culture-event-modal-content";

    const closeButton = document.createElement("span");
    closeButton.className = "close-button";
    closeButton.innerHTML = "&times;";
    closeButton.onclick = function () {
        modal.remove();
    };

    // 제목 (선택된 지역)
    const title = document.createElement("h5");
    title.innerText = `🎭 ${areaName}의 문화행사 목록`;

    const eventContainer = document.createElement("div");
    eventContainer.className = "event-container";

    if (!data.data || data.data.length === 0) {
        const noEventText = document.createElement("p");
        noEventText.innerText = "해당 지역의 문화행사 정보가 없습니다.";
        eventContainer.appendChild(noEventText);
    } else {
        data.data.forEach(event => {
            const eventCard = document.createElement("div");
            eventCard.className = "event-card";

            const eventImg = document.createElement("img");
            eventImg.src = event.thumbnail;
            eventImg.alt = event.eventName;
            eventImg.className = "event-thumbnail";
            eventImg.style.maxWidth = "100px";  // 최대 너비 100px
            eventImg.style.height = "auto";  // 높이는 비율 유지
            eventImg.style.objectFit = "contain"; // 이미지 찌그러짐 방지

            const eventInfo = document.createElement("div");
            eventInfo.className = "event-info";

            const eventTitle = document.createElement("h7");
            eventTitle.innerText = event.eventName;

            const eventPeriod = document.createElement("p");
            eventPeriod.innerText = `📅 일정: ${event.eventPeriod}`;

            const eventPlace = document.createElement("p");
            eventPlace.innerText = `📍 장소: ${event.eventPlace}`;

            const eventLink = document.createElement("a");
            eventLink.href = event.url;
            eventLink.target = "_blank";
            eventLink.innerText = "🔗 상세 정보 보기";

            eventInfo.appendChild(eventTitle);
            eventInfo.appendChild(eventPeriod);
            eventInfo.appendChild(eventPlace);
            eventInfo.appendChild(eventLink);

            eventCard.appendChild(eventImg);
            eventCard.appendChild(eventInfo);
            eventContainer.appendChild(eventCard);
        });
    }

    // 모달 구성
    modalContent.appendChild(closeButton);
    modalContent.appendChild(title);
    modalContent.appendChild(eventContainer);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    // 모달 바깥 클릭하면 닫기
    modal.addEventListener("click", function (event) {
        if (event.target === modal) {
            modal.remove();
        }
    });
}
