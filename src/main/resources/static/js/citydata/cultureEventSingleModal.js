function cultureEventSingleModal(areaName, events) {
    saveToLocal("cachedCultureEvents", {areaName, events});

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
    title.innerText = `🎭 ${areaName}의 문화행사 정보`;

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
        eventCard.style.position = "relative"; // 오버레이 배치를 위한 설정

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

        const reviewButton = document.createElement("button");
        reviewButton.className = "btn btn-outline-secondary btn-sm mr-2";
        reviewButton.innerText = "📝 후기 보기";
        reviewButton.style.width = "fit-content";
        reviewButton.style.marginTop = "10px";
        reviewButton.style.fontSize = "0.8rem";
        reviewButton.onclick = function () {
            showReviewModal(event.cultureEventId, event.eventName);
        };

        eventInfo.appendChild(eventName);
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

function showReviewModal(cultureEventId, eventName) {
    const modal = document.getElementById("culture-event-modal");
    modal.innerHTML = ""; // 기존 내용 제거

    const modalContent = document.createElement("div");
    modalContent.className = "culture-event-modal-content";

    const modalHeader = document.createElement("div");
    modalHeader.className = "modal-header";

    const backButton = document.createElement("button");
    backButton.className = "btn btn-outline-secondary btn-sm mr-2";
    backButton.innerText = "<";
    backButton.style.fontSize = "0.8rem";
    backButton.onclick = () => {
        const cached = loadFromLocal("cachedCultureEvents");
        if (cached && cached.events) {
            cultureEventSingleModal(cached.areaName, cached.events);
        }
    };

    const title = document.createElement("h5");
    title.innerText = `📝 '${eventName}' 후기 목록`;

    const writeButton = document.createElement("button");
    writeButton.className = "btn btn-outline-secondary btn-sm mr-2";
    writeButton.innerText = "후기 작성하기";
    writeButton.style.fontSize = "0.8rem";
    writeButton.onclick = () => {
        showCreateReviewForm(cultureEventId, eventName);
    };

    modalHeader.appendChild(backButton);
    modalHeader.appendChild(title);
    modalHeader.appendChild(writeButton);  // 제목 아래에 버튼


    const reviewList = document.createElement("div");
    reviewList.className = "review-list";
    reviewList.style.height = "300px";
    reviewList.style.overflowY = "auto";

    let page = 0;
    let isLast = false;

    const loadReviews = () => {
        if (isLast) return;

        fetch(`/api/board?cultureEventId=${cultureEventId}&page=${page}`)
            .then(res => res.json())
            .then(data => {
                const reviews = data.data.content;

                // 후기 없을 경우 메시지
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

                    const updatedInfo = updatedAt ? ` (수정일자: ${updatedAt})` : "";

                    const header = document.createElement("p");
                    header.innerHTML = `<strong>${review.author}</strong> 작성일자 ${createdAt}${updatedInfo}`;

                    const content = document.createElement("p");
                    content.innerText = review.content;
                    content.style.overflowWrap = "break-word"; // 긴 단어 자동 줄바꿈
                    content.style.whiteSpace = "pre-wrap"; // 개행(\n)은 그대로 줄바꿈 적용하되 긴 단어는 자동 줄바꿈

                    item.appendChild(header);
                    item.appendChild(content);

                    // 수정/삭제 버튼
                    if (review.editable) {
                        const editBtn = document.createElement("button");
                        editBtn.className = "btn btn-secondary btn-sm mr-2";
                        editBtn.innerText = "수정";
                        editBtn.style.marginRight = "8px";
                        editBtn.onclick = () => {
                            showEditReviewForm(cultureEventId, review, eventName);
                        };

                        const deleteBtn = document.createElement("button");
                        deleteBtn.className = "btn btn-danger btn-sm";
                        deleteBtn.innerText = "삭제";
                        deleteBtn.onclick = () => {
                            if (confirm("정말 삭제하시겠습니까?")) {
                                fetch(`/api/board/${review.id}`, {
                                    method: "DELETE"
                                })
                                    .then(res => {
                                        if (!res.ok) throw new Error("삭제 실패");
                                        return res.json();
                                    })
                                    .then(() => {
                                        alert("삭제되었습니다.");
                                        reviewList.innerHTML = ""; // 초기화 후 다시 불러오기
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

                    const divider = document.createElement("hr");
                    item.appendChild(divider);
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

function showCreateReviewForm(cultureEventId, eventName) {
    const modal = document.getElementById("culture-event-modal");
    modal.innerHTML = ""; // 기존 내용 제거

    const modalContent = document.createElement("div");
    modalContent.className = "culture-event-modal-content";

    const modalHeader = document.createElement("div");
    modalHeader.className = "modal-header";

    const backButton = document.createElement("button");
    backButton.className = "btn btn-outline-secondary btn-sm mr-2";
    backButton.innerText = "<";
    backButton.style.fontSize = "0.8rem";
    backButton.onclick = () => showReviewModal(cultureEventId, eventName);

    const title = document.createElement("h5");
    title.innerText = `✍️ '${eventName}' 후기 작성`;

    modalHeader.appendChild(backButton);
    modalHeader.appendChild(title);

    const form = document.createElement("form");
    form.className = "review-form";

    const textarea = document.createElement("textarea");
    textarea.className = "form-control";
    textarea.id = "content"
    textarea.placeholder = "후기를 입력하세요 (최대 300자)";
    textarea.maxLength = 300;
    textarea.rows = 6;
    textarea.required = true;
    textarea.style.width = "100%";
    textarea.style.height = "100px";

    const submitButton = document.createElement("button");
    submitButton.className = "btn btn-primary mt-2";
    submitButton.type = "submit";
    submitButton.innerText = "등록";
    submitButton.style.fontSize = "0.8rem";

    form.appendChild(textarea);
    form.appendChild(submitButton);

    form.onsubmit = function (e) {
        e.preventDefault();

        document.querySelectorAll(".error-message").forEach(el => el.remove());

        const content = textarea.value.trim();
        if (!content) {
            alert("후기를 입력해주세요.");
            return;
        }

        fetch("/api/board", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                content: content,
                cultureEventId: cultureEventId
            })
        })
            .then(res => {
                if (!res.ok) throw new Error("후기 작성 실패");
                return res.json();
            })
            .then(() => {
                alert("후기가 등록되었습니다.");
                showReviewModal(cultureEventId, eventName); // 다시 후기 목록 보기로 이동
            })
            .catch(() => {
                alert("후기 작성 중 오류가 발생했습니다.");
            });
    };

    modalContent.appendChild(modalHeader);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);
}

function showEditReviewForm(cultureEventId, review, eventName) {
    const modal = document.getElementById("culture-event-modal");
    modal.innerHTML = ""; // 기존 내용 제거

    const modalContent = document.createElement("div");
    modalContent.className = "culture-event-modal-content";

    const modalHeader = document.createElement("div");
    modalHeader.className = "modal-header";

    const backButton = document.createElement("button");
    backButton.className = "btn btn-outline-secondary btn-sm mr-2";
    backButton.innerText = "<";
    backButton.style.fontSize = "0.8rem";
    backButton.onclick = () => {
        showReviewModal(cultureEventId, eventName);
    };

    const title = document.createElement("h5");
    title.innerText = `✏️ '${eventName}' 후기 수정`;

    const createdAt = formatDateTime(review.createdAt);
    const updatedAt = review.updatedAt ? formatDateTime(review.updatedAt) : null;

    const updatedInfo = updatedAt ? ` (수정일자: ${updatedAt})` : "";

    const reviewInfo = document.createElement("p");
    reviewInfo.innerHTML = `<strong>${review.author}</strong> 작성일자 ${createdAt}${updatedInfo}`;
    reviewInfo.style.fontSize = "0.85rem";
    reviewInfo.style.textAlign = "left";

    modalHeader.appendChild(backButton);
    modalHeader.appendChild(title);

    const form = document.createElement("form");
    form.onsubmit = function (e) {
        e.preventDefault();
        document.querySelectorAll(".error-message").forEach(el => el.remove());

        const newContent = textarea.value.trim();
        if (!newContent) {
            alert("후기 내용을 입력해주세요.");
            return;
        }

        fetch(`/api/board/${review.id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ content: newContent })
        })
            .then(async res => {
                const response = await res.json();
                if (!res.ok) {
                    // 서버에서 유효성 오류를 context에 담아 보낸 경우
                    if (response.context && response.context.content) {
                        displayErrorMessages(response.context.content);
                        return; // 오류 메시지 보여주고 종료
                    }
                    throw new Error("수정 실패"); // 그 외 예외
                }

                // 성공 시
                alert("후기가 수정되었습니다.");
                showReviewModal(cultureEventId, eventName);
            })
            .catch(() => alert("수정 중 오류 발생"));
    };

    const textarea = document.createElement("textarea");
    textarea.className = "form-control";
    textarea.id = "content"
    textarea.maxLength = 300;
    textarea.value = review.content;
    textarea.rows = 6;
    textarea.style.width = "100%";
    textarea.style.marginTop = "10px";

    const submitBtn = document.createElement("button");
    submitBtn.className = "btn btn-primary mt-2";
    submitBtn.type = "submit";
    submitBtn.innerText = "저장";
    submitBtn.style.fontSize = "0.8rem";
    submitBtn.style.marginTop = "10px";

    form.appendChild(textarea);
    form.appendChild(submitBtn);

    modalContent.appendChild(modalHeader);
    modalContent.appendChild(reviewInfo);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);


}

// DateTime -> YYYY-MM-DD HH:mm 변환 함수
function formatDateTime(dateTimeStr) {
    const [date, time] = dateTimeStr.split("T");
    const hhmm = time.slice(0, 5);
    return `${date} ${hhmm}`;
}

// 에러 메시지 표시 함수
function displayErrorMessages(errors) {
    errors.forEach(errorMsg => {
        const textarea = document.querySelector('#content');
        const errorMessage = document.createElement("div");
        errorMessage.className = "text-danger error-message";
        errorMessage.style.textAlign = "left";
        errorMessage.style.fontSize = "0.7rem";
        errorMessage.textContent = errorMsg;
        // textarea 아래에 에러 메시지 추가
        textarea.parentNode.insertBefore(errorMessage, textarea.nextSibling);
    });
}


