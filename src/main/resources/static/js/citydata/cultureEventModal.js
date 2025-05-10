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

    // 모달 헤더 (제목 + 닫기 버튼)
    const modalHeader = document.createElement("div");
    modalHeader.className = "modal-header";

    const title = document.createElement("h5");
    title.innerText = `🎭 ${areaName}의 문화행사 목록`;

    const closeButton = document.createElement("span");
    closeButton.className = "close-button";
    closeButton.innerHTML = "&times;";
    closeButton.onclick = function () {
        modal.remove();
    };

    modalHeader.appendChild(title);
    modalHeader.appendChild(closeButton);

    // 문화행사 정보 컨텐츠 영역
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
            eventCard.style.position = "relative"; // 오버레이 배치를 위한 설정

            const eventImg = document.createElement("img");
            eventImg.src = event.thumbnail;
            eventImg.alt = event.eventName;
            eventImg.className = "event-thumbnail";

            const eventInfo = document.createElement("div");
            eventInfo.className = "event-info";

            const eventTitle = document.createElement("h6");
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

            // 종료된 행사는 overlay 추가 및 종료된 행사 문구 표기
            if (event.isEnded === true) {
                const overlay = document.createElement("div");
                overlay.className = "event-overlay";
                overlay.innerText = "종료된 행사";
                eventCard.appendChild(overlay);
            }

            eventContainer.appendChild(eventCard);
        });
    }

    // 모달 구성
    modalContent.appendChild(modalHeader);
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
