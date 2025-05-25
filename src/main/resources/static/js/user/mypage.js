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
                    window.location.href = "/logout";
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

    // 모달이 열릴 때 초기화 및 첫 로딩
    $('#boardModal').on('shown.bs.modal', function () {
        reviewPage = 0;
        reviewIsLast = false;
        boardList.innerHTML = "";
        loadMoreReviews();
    });

    // 스크롤 끝에 도달하면 더 불러오기
    boardModalBody.addEventListener("scroll", function () {
        const threshold = 50;
        if (!reviewLoading &&
            boardModalBody.scrollTop + boardModalBody.clientHeight >= boardModalBody.scrollHeight - threshold) {
            loadMoreReviews();
        }
    });

    function loadMoreReviews() {
        if (reviewIsLast) return;

        reviewLoading = true;
        loadingIndicator.style.display = "block";

        fetch(`/api/board/my?page=${reviewPage}&size=10`)
            .then(response => response.json())
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
                        <small class="text-muted">장소: ${item.eventPlace} (${item.areaName})</small>

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
                                reviewContent.innerText = newContent;
                                editForm.classList.add("d-none");
                                reviewContent.classList.remove("d-none");
                                buttonGroup.classList.remove("d-none");
                            })
                            .catch(() => alert("수정 중 오류 발생"));
                    };

                    deleteBtn.onclick = () => {
                        if (confirm("정말 삭제하시겠습니까?")) {
                            fetch(`/api/board/${item.id}`, {
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
            headers: {
                "Content-Type": "application/json"
            },
        }).then(response => response.json()
            .then(data => ({status: response.status, body: data}))
            .then(({status, body}) => {
                if (status === 200) {
                    alert("회원 탈퇴가 완료되었습니다.");
                    window.location.href = "/";
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
