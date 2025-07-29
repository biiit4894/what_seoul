function cultureEventModal(data, areaName) {
    const existingModal = document.getElementById("culture-event-modal");
    if (existingModal) existingModal.remove();

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

            const reviewButton = document.createElement("button");
            reviewButton.className = "btn btn-outline-secondary btn-sm mt-2";
            reviewButton.innerText = "📝 후기 보기";
            reviewButton.style.width = "fit-content";
            reviewButton.style.marginTop = "10px";
            reviewButton.style.fontSize = "0.8rem";
            reviewButton.onclick = () => showReviewModalForArea(event.id, event.eventName, data.data, areaName);

            eventInfo.appendChild(eventTitle);
            eventInfo.appendChild(eventPeriod);
            eventInfo.appendChild(eventPlace);
            eventInfo.appendChild(eventLink);
            eventInfo.appendChild(reviewButton);

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

// 후기 조회 모달 생성
function showReviewModalForArea(cultureEventId, eventName, allEventData, areaName) {
    const modal = document.getElementById("culture-event-modal");
    modal.innerHTML = "";

    const modalContent = document.createElement("div");
    modalContent.className = "culture-event-modal-content";

    const modalHeader = document.createElement("div");
    modalHeader.className = "modal-header";

    const backButton = document.createElement("button");
    backButton.className = "btn btn-outline-secondary btn-sm mr-2";
    backButton.innerText = "<";
    backButton.style.fontSize = "0.8rem";
    backButton.onclick = () => cultureEventModal({ data: allEventData }, areaName);

    const title = document.createElement("h5");
    title.innerText = `📝 '${eventName}' 후기 목록`;

    const writeButton = document.createElement("button");
    writeButton.className = "btn btn-outline-secondary btn-sm mr-2";
    writeButton.innerText = "후기 작성하기";
    writeButton.style.fontSize = "0.8rem";
    writeButton.onclick = () => showCreateReviewFormForArea(cultureEventId, eventName, allEventData, areaName);

    modalHeader.appendChild(backButton);
    modalHeader.appendChild(title);
    modalHeader.appendChild(writeButton);

    const reviewList = document.createElement("div");
    reviewList.className = "review-list";
    reviewList.style.height = "300px";
    reviewList.style.overflowY = "auto";

    let page = 0;
    let isLast = false;

    const loadReviews = () => {
        if (isLast) return;

        fetch(`/api/board?cultureEventId=${cultureEventId}&page=${page}`, {
            credentials: 'include',
        })
            .then(res => res.json())
            .then(data => {
                const reviews = data.data.content;

                if (page === 0 && reviews.length === 0) {
                    const noReviewMsg = document.createElement("p");
                    noReviewMsg.innerText = "등록된 후기가 없습니다.";
                    noReviewMsg.style.textAlign = "center";
                    noReviewMsg.style.marginTop = "20px";
                    reviewList.appendChild(noReviewMsg);
                    isLast = true;
                    return;
                }

                reviews.forEach(review => {
                    const createdAt = formatDateTime(review.createdAt);
                    const updatedAt = review.updatedAt ? formatDateTime(review.updatedAt) : null;
                    const item = document.createElement("div");
                    item.className = "review-item";
                    item.style.textAlign = "left";
                    item.style.fontSize = "0.9em";

                    const header = document.createElement("p");
                    header.innerHTML = `<strong>${review.author}</strong> 작성일자 ${createdAt}${updatedAt ? ` (수정일자: ${updatedAt})` : ""}`;

                    const content = document.createElement("p");
                    content.innerText = review.content;
                    content.style.overflowWrap = "break-word"; // 긴 단어 자동 줄바꿈
                    content.style.whiteSpace = "pre-wrap"; // 개행(\n)은 그대로 줄바꿈 적용하되 긴 단어는 자동 줄바꿈

                    item.appendChild(header);
                    item.appendChild(content);

                    if (review.editable) {
                        const editBtn = document.createElement("button");
                        editBtn.className = "btn btn-secondary btn-sm mr-2";
                        editBtn.innerText = "수정";
                        editBtn.onclick = () => showEditReviewFormForArea(cultureEventId, review, eventName, allEventData, areaName);

                        const deleteBtn = document.createElement("button");
                        deleteBtn.className = "btn btn-danger btn-sm";
                        deleteBtn.innerText = "삭제";
                        deleteBtn.onclick = () => {
                            if (confirm("정말 삭제하시겠습니까?")) {
                                fetch(`/api/board/${review.id}`, {
                                    method: "DELETE",
                                    credentials: 'include',
                                })
                                    .then(res => {
                                        if (!res.ok) throw new Error("삭제 실패");
                                        return res.json();
                                    })
                                    .then(() => {
                                        alert("삭제되었습니다.");
                                        reviewList.innerHTML = "";
                                        page = 0;
                                        isLast = false;
                                        loadReviews();
                                    })
                                    .catch(() => alert("삭제 중 오류 발생"));
                            }
                        };

                        const buttonWrap = document.createElement("div");
                        buttonWrap.appendChild(editBtn);
                        buttonWrap.appendChild(deleteBtn);
                        item.appendChild(buttonWrap);
                    }

                    item.appendChild(document.createElement("hr"));
                    reviewList.appendChild(item);
                });

                isLast = data.data.last;
                page++;
            });
    };

    reviewList.addEventListener("scroll", () => {
        if (reviewList.scrollTop + reviewList.clientHeight >= reviewList.scrollHeight - 10) {
            loadReviews();
        }
    });

    loadReviews();

    modalContent.appendChild(modalHeader);
    modalContent.appendChild(reviewList);
    modal.appendChild(modalContent);
}

// 후기 작성 모달 생성
function showCreateReviewFormForArea(cultureEventId, eventName, allEventData, areaName) {
    const modal = document.getElementById("culture-event-modal");
    modal.innerHTML = "";

    const modalContent = document.createElement("div");
    modalContent.className = "culture-event-modal-content";

    const modalHeader = document.createElement("div");
    modalHeader.className = "modal-header";

    const backButton = document.createElement("button");
    backButton.className = "btn btn-outline-secondary btn-sm mr-2";
    backButton.innerText = "<";
    backButton.onclick = () => showReviewModalForArea(cultureEventId, eventName, allEventData, areaName);

    const title = document.createElement("h5");
    title.innerText = `✍️ '${eventName}' 후기 작성`;

    modalHeader.appendChild(backButton);
    modalHeader.appendChild(title);

    const form = document.createElement("form");
    const textarea = document.createElement("textarea");
    textarea.className = "form-control";
    textarea.id = "content";
    textarea.placeholder = "후기를 입력하세요 (최대 300자)";
    textarea.maxLength = 300;
    textarea.rows = 6;

    const submitButton = document.createElement("button");
    submitButton.className = "btn btn-primary mt-2";
    submitButton.type = "submit";
    submitButton.innerText = "등록";

    form.appendChild(textarea);
    form.appendChild(submitButton);

    form.onsubmit = function (e) {
        e.preventDefault();
        const content = textarea.value.trim();
        if (!content) return alert("후기를 입력해주세요.");

        fetch("/api/board", {
            method: "POST",
            credentials: 'include',
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ content, cultureEventId })
        })
            .then(res => {
                if (!res.ok) throw new Error("작성 실패");
                return res.json();
            })
            .then(() => {
                alert("후기가 등록되었습니다.");
                showReviewModalForArea(cultureEventId, eventName, allEventData, areaName);
            })
            .catch(() => alert("작성 중 오류 발생"));
    };

    modalContent.appendChild(modalHeader);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);
}

// 후기 수정 모달 생성
function showEditReviewFormForArea(cultureEventId, review, eventName, allEventData, areaName) {
    const modal = document.getElementById("culture-event-modal");
    modal.innerHTML = "";

    const modalContent = document.createElement("div");
    modalContent.className = "culture-event-modal-content";

    const modalHeader = document.createElement("div");
    modalHeader.className = "modal-header";

    const backButton = document.createElement("button");
    backButton.className = "btn btn-outline-secondary btn-sm mr-2";
    backButton.innerText = "<";
    backButton.onclick = () => showReviewModalForArea(cultureEventId, eventName, allEventData, areaName);

    const title = document.createElement("h5");
    title.innerText = `✏️ '${eventName}' 후기 수정`;

    const form = document.createElement("form");
    const textarea = document.createElement("textarea");
    textarea.className = "form-control";
    textarea.id = "content";
    textarea.value = review.content;
    textarea.maxLength = 300;
    textarea.rows = 6;

    const submitBtn = document.createElement("button");
    submitBtn.className = "btn btn-primary mt-2";
    submitBtn.type = "submit";
    submitBtn.innerText = "저장";

    form.appendChild(textarea);
    form.appendChild(submitBtn);

    form.onsubmit = function (e) {
        e.preventDefault();
        const newContent = textarea.value.trim();
        if (!newContent) return alert("후기 내용을 입력해주세요.");

        fetch(`/api/board/${review.id}`, {
            method: "PUT",
            credentials: 'include',
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ content: newContent })
        })
            .then(res => res.json())
            .then(() => {
                alert("후기가 수정되었습니다.");
                showReviewModalForArea(cultureEventId, eventName, allEventData, areaName);
            })
            .catch(() => alert("수정 중 오류 발생"));
    };

    modalHeader.appendChild(backButton);
    modalHeader.appendChild(title);
    modalContent.appendChild(modalHeader);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);
}

function formatDateTime(dateTimeStr) {
    const [date, time] = dateTimeStr.split("T");
    return `${date} ${time.slice(0, 5)}`;
}
