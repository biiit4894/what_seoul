document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("updateModal").addEventListener("submit", function (event) {
        event.preventDefault(); // 폼 기본 제출 동작 방지

        // 기존 에러 메시지 초기화
        document.querySelectorAll(".error-message").forEach(el => el.remove());

        const currPassword = document.getElementById("currPassword").value;
        const newPassword = toNullIfEmpty(document.getElementById("newPassword").value);
        const newEmail = toNullIfEmpty(document.getElementById("newEmail").value);
        const newNickName = toNullIfEmpty(document.getElementById("newNickName").value);

        if (!newPassword && !newEmail && !newNickName) {
            alert("수정할 정보를 입력해 주세요.")

        }
        fetch('/api/user/update', {
            method: 'PUT',
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                currPassword: currPassword,
                newPassword: newPassword,
                newEmail: newEmail,
                newNickName: newNickName,
            })
        }).then(response => response.json()
            .then(data => ({status: response.status, body: data})
            ))
            .then(({status, body}) => {
                if (status === 200) {
                    alert("회원정보가 수정되어 로그아웃합니다.");
                    fetch('/api/auth/logout', {
                        method: 'POST',
                        credentials: "include"
                    }).then(response => {
                        if (response.ok) {
                            // 세션스토리지 비우기
                            sessionStorage.removeItem("accessTokenExpiration");
                            sessionStorage.removeItem("loginStartTime");

                            alert("로그아웃이 완료되었습니다.")
                            window.location.href = '/';
                        }
                    }).catch(error => {
                        console.log("Error: ", error);
                    });
                } else if (status === 400) {
                    displayErrors(body.context);
                }
            })
            .catch(error => {
                console.log("Error: ", error);
                alert("회원정보 수정에 실패했습니다.");
            })
    });

    function toNullIfEmpty(str) {
        return str.trim() === "" ? null : str;
    }

    function displayErrors(errors) {
        Object.keys(errors).forEach(field => {
            const inputField = document.getElementById(field);
            if (inputField) {
                errors[field].forEach(errorMsg => {
                    const errorMessage = document.createElement("div");
                    errorMessage.className = "text-danger error-message";
                    errorMessage.style.fontSize = "12px";
                    errorMessage.textContent = errorMsg;
                    inputField.parentNode.appendChild(errorMessage);
                })

            }
        });
    }

    // 내가 작성한 후기 조회 모달 - 무한 스크롤
    let reviewPage = 0;
    let reviewLoading = false;
    let reviewIsLast = false;

    const boardList = document.getElementById("board-list");
    const boardModalBody = document.querySelector("#boardModal .modal-body");
    const loadingIndicator = document.getElementById("loading");

    const startDateInput = document.getElementById("startDate");
    const endDateInput = document.getElementById("endDate");

    let areaNamesCache = null; // 유저가 후기를 작성한 장소명 캐싱용
    const areaCheckboxContainer = document.getElementById("areaCheckboxContainer");

    const getBoardListBtn = document.getElementById("getBoardListBtn");

    // 날짜, 장소, 작성일자 정렬 선택 후 조회하는 버튼 클릭 이벤트
    getBoardListBtn.onclick = () => {
        reviewPage = 0;
        reviewIsLast = false;
        boardList.innerHTML = "";
        loadCultureEventReviews();
    };

    // 모달이 열릴 때 초기화 및 첫 로딩
    $('#boardModal').on('shown.bs.modal', function () {
        reviewPage = 0;
        reviewIsLast = false;
        boardList.innerHTML = "";
        loadAreaNamesReviewed(); // 모달이 열릴때 한번 만 로드
        loadCultureEventReviews();
    });

    // 스크롤 끝에 도달하면 더 불러오기
    boardModalBody.addEventListener("scroll", function () {
        const threshold = 50;
        if (!reviewLoading &&
            boardModalBody.scrollTop + boardModalBody.clientHeight >= boardModalBody.scrollHeight - threshold) {
            loadCultureEventReviews();
        }
    });

    function loadAreaNamesReviewed() {
        // 유저가 후기를 작성한 장소명 리스트 조회 (작성한 후기 조회 모달이 열린 후 한 번)
        fetch('/api/area/reviewed', {
                credentials: "include",
            })
            .then(res => {
                if(!res.ok) {
                    throw new Error("후기를 작성한 장소명 리스트 조회 실패");
                }
                return res.json()
            })
            .then(data => {
                areaNamesCache = data.data || [];

                if (areaNamesCache.length === 0) {
                    areaCheckboxContainer.innerHTML = `
                        <div class="font-weight-bold mr-2" style="white-space: nowrap; display: inline-block;">
                            장소 선택
                        </div>
                        <div class="text-muted" style="display: inline-block;">
                            등록된 장소명이 없습니다.
                        </div>
                    `;
                    return;
                }

                // 초기화
                areaCheckboxContainer.innerHTML = `
                    <div class="font-weight-bold mr-2" style="white-space: nowrap;">장소 선택</div>
                    <div id="area-checkbox-list" class="d-flex flex-wrap gap-2"></div>
                `;

                const checkboxListDiv = document.getElementById("area-checkbox-list");

                areaNamesCache.forEach(area => {
                    const id = `area-checkbox-${area.replace(/\s+/g, '-')}`;
                    const wrapper = document.createElement("div");
                    wrapper.className = "form-check form-check-inline";

                    wrapper.innerHTML = `
                            <input class="form-check-input" type="checkbox" id="${id}" value="${area}">
                            <label class="form-check-label" for="${id}">${area}</label>
                    `;
                    checkboxListDiv.appendChild(wrapper);
                })

            })
            .catch(err => {
                areaCheckboxContainer.innerHTML = `<p class="text-danger">장소명을 불러오는데 실패했습니다.</p>`;
                console.error(err);
            });
    }

    function loadCultureEventReviews() {

        if (reviewIsLast) return;

        reviewLoading = true;
        loadingIndicator.style.display = "block";

        // 정렬 기준 라디오에서 현재 선택된 값 가져오기
        const selectedSortRadio = document.querySelector('input[name="sortOrder"]:checked');
        const sortDirection = selectedSortRadio ? selectedSortRadio.value : 'desc';

        let url = `/api/board/my?page=${reviewPage}&size=10&sort=${sortDirection}`;

        // 날짜 값이 있으면 쿼리 파라미터에 추가
        const startDateVal = startDateInput.value; // yyyy-MM-dd 포맷
        const endDateVal = endDateInput.value;

        if (startDateVal) {
            url += `&startDate=${startDateVal}`;
        }
        if (endDateVal) {
            url += `&endDate=${endDateVal}`;
        }

        // 선택된 장소 체크박스 값 수집
        const checkedAreas = Array.from(areaCheckboxContainer.querySelectorAll("input[type=checkbox]:checked"))
            .map(cb => cb.value);

        // POST용 body 객체 구성
        const requestBody = {
            selectedAreaNames: checkedAreas
        };

        fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(requestBody)
        })
            .then(async response => {
                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.context || "알 수 없는 오류가 발생했습니다.");
                }
                return response.json();
            })
            .then(data => {
                const slice = data.data;

                if (reviewPage === 0 && slice.content.length === 0) {
                    const emptyMessage = document.createElement("div");
                    emptyMessage.className = "text-center text-muted mt-3 mb-3";
                    emptyMessage.innerText = "아직 작성한 후기가 없습니다.";
                    boardList.appendChild(emptyMessage);
                    reviewIsLast = true;
                    return;
                }

                slice.content.forEach(item => {
                    const reviewItem = document.createElement("div");
                    reviewItem.className = "mb-4 p-3 border rounded";

                    const contentId = `review-content-${item.id}`;
                    const formId = `edit-form-${item.id}`;

                    reviewItem.innerHTML = `
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="badge badge-${item.ended ? 'secondary' : 'success'}">
                                ${item.ended ? '종료된 행사' : '진행 중인 행사'}
                            </span>
                            <small class="text-muted">
                                작성일자: ${new Date(item.createdAt).toLocaleString()}<br/>
                                ${item.updatedAt ? `수정일자: ${new Date(item.updatedAt).toLocaleString()}` : ""}
                            </small>
                        </div>

                        <h6 class="mt-2 mb-1 font-weight-bold">
                            <a href="${item.url}" target="_blank" rel="noopener noreferrer" class="text-dark">
                                ${item.eventName}
                            </a>
                        </h6>
                        <small class="text-muted">장소: ${item.eventPlace} (${item.areaName}) ${item.areaDeletedAt ? `<span style="color: #6cb2eb;"> - 삭제처리된 장소입니다.</span>` : ''}</small>
                        <p id="${contentId}" class="mt-2 review-content"
                           style="white-space: pre-line; word-break: break-word; overflow-wrap: break-word;">
                           ${item.content}
                        </p>

                        <form id="${formId}" class="edit-form d-none mt-2">
                            <textarea class="form-control" rows="4" maxlength="300">${item.content}</textarea>
                            <button type="submit" class="btn btn-sm btn-primary mt-2">저장</button>
                            <button type="button" class="btn btn-sm btn-secondary mt-2 cancel-edit">뒤로가기</button>
                        </form>

                        <div class="mt-2 button-group">
                            <button class="btn btn-sm btn-outline-secondary edit-btn">수정</button>
                            <button class="btn btn-sm btn-outline-danger delete-btn">삭제</button>
                        </div>
                    `;

                    const editBtn = reviewItem.querySelector(".edit-btn");
                    const deleteBtn = reviewItem.querySelector(".delete-btn");
                    const buttonGroup = reviewItem.querySelector(".button-group");
                    const reviewContent = reviewItem.querySelector(".review-content");
                    const editForm = reviewItem.querySelector(".edit-form");
                    const cancelBtn = reviewItem.querySelector(".cancel-edit");
                    const textarea = editForm.querySelector("textarea");

                    editBtn.onclick = () => {
                        reviewContent.classList.add("d-none");
                        editForm.classList.remove("d-none");
                        buttonGroup.classList.add("d-none");
                        editForm.querySelectorAll(".error-message").forEach(e => e.remove());
                    };

                    cancelBtn.onclick = () => {
                        editForm.classList.add("d-none");
                        reviewContent.classList.remove("d-none");
                        buttonGroup.classList.remove("d-none");
                        textarea.value = reviewContent.innerText.trim();
                        editForm.querySelectorAll(".error-message").forEach(e => e.remove());
                    };

                    editForm.onsubmit = function (e) {
                        e.preventDefault();
                        const newContent = textarea.value.trim();
                        editForm.querySelectorAll(".error-message").forEach(e => e.remove());

                        if (!newContent) {
                            displayErrorMessages(["내용을 입력해주세요."], textarea);
                            return;
                        }

                        fetch(`/api/board/${item.id}`, {
                            method: "PUT",
                            credentials: "include",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify({ content: newContent })
                        })
                            .then(async res => {
                                const response = await res.json();
                                if (!res.ok) {
                                    if (response.context && response.context.content) {
                                        displayErrorMessages(response.context.content, textarea);
                                        return;
                                    }
                                    throw new Error("수정 실패");
                                }

                                alert("후기가 수정되었습니다.");
                                reviewContent.innerText = newContent; // 후기 내용 갱신해서 화면에 보여주기

                                const updatedAt = new Date(response.data.updatedAt).toLocaleString(); // 작성일자 갱신해서 화면에 보여주기
                                const dateDisplay = reviewItem.querySelector("small.text-muted");
                                dateDisplay.innerHTML = `
                                    작성일자: ${new Date(item.createdAt).toLocaleString()}<br/>
                                    수정일자: ${updatedAt}
                                `;

                                editForm.classList.add("d-none");
                                reviewContent.classList.remove("d-none");
                                buttonGroup.classList.remove("d-none");
                            })
                            .catch(() => alert("수정 중 오류 발생"));
                    };

                    deleteBtn.onclick = () => {
                        if (confirm("정말 삭제하시겠습니까?")) {
                            fetch(`/api/board/${item.id}`, {
                                credentials: "include",
                                method: "DELETE"
                            })
                                .then(res => {
                                    if (!res.ok) throw new Error("삭제 실패");
                                    return res.json();
                                })
                                .then(() => {
                                    alert("삭제되었습니다.");
                                    reviewItem.remove();
                                })
                                .catch(() => alert("삭제 중 오류 발생"));
                        }
                    };

                    boardList.appendChild(reviewItem);
                });

                reviewIsLast = slice.last;
                reviewPage++;
            })
            .catch(error => {
                alert(error.message);
                const emptyMessage = document.createElement("div");
                emptyMessage.className = "text-center text-muted mt-3 mb-3";
                emptyMessage.innerHTML = `<br> 앗! 후기를 불러오는데 문제가 생겼어요. <br> 잠시 후 다시 시도해주세요.`;
                boardList.appendChild(emptyMessage);
                console.error("후기 불러오기 실패:", error);
            })
            .finally(() => {
                reviewLoading = false;
                loadingIndicator.style.display = "none";
            });
    }

    function displayErrorMessages(errors, targetTextarea) {
        errors.forEach(errorMsg => {
            const errorMessage = document.createElement("div");
            errorMessage.className = "text-danger error-message";
            errorMessage.style.textAlign = "left";
            errorMessage.style.fontSize = "0.7rem";
            errorMessage.textContent = errorMsg;
            targetTextarea.parentNode.insertBefore(errorMessage, targetTextarea.nextSibling);
        });
    }


});

function withdrawUser() {
    if (confirm("정말 탈퇴하시겠습니까?") === true) {
        fetch('/api/user/withdraw', {
            method: 'PUT',
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
        }).then(response => response.json()
            .then(data => ({status: response.status, body: data}))
            .then(({status, body}) => {
                if (status === 200) {
                    alert("회원 탈퇴가 완료되어 홈으로 이동합니다.");
                    fetch('/api/auth/logout', {
                        method: 'POST',
                        credentials: "include"
                    }).then(response => {
                        if (response.ok) {
                            // 세션스토리지 비우기
                            sessionStorage.removeItem("accessTokenExpiration");
                            sessionStorage.removeItem("loginStartTime");

                            // alert("로그아웃이 완료되었습니다.")
                            window.location.href = '/';
                        }
                    }).catch(error => {
                        console.log("Error: ", error);
                    });
                } else {
                    alert(body.message || "회원탈퇴에 실패했습니다.");
                }
            })
            .catch(error => {
                console.log("Error: ", error);
                alert("회원탈퇴에 실패했습니다.");
            }));
    }

}
