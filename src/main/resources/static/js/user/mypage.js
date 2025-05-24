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
                const slice = data.data; // `CommonResponse<Slice<ResGetMyBoardDTO>>`에서 result가 Slice
                slice.content.forEach(item => {
                    const reviewItem = document.createElement("div");
                    reviewItem.className = "mb-3 p-3 border rounded";

                    reviewItem.innerHTML = `
                    <h5>${item.eventName}</h5>
                    <p>${item.content}</p>
                    <small class="text-muted">
                        행사장소: ${item.eventPlace} (${item.areaName})<br/>
                        작성일자: ${new Date(item.createdAt).toLocaleString()}<br/>
                        ${item.updatedAt ? `수정일자: ${new Date(item.updatedAt).toLocaleString()}` : ""}
                    </small>
                    `;

                    // console.log(item.isEnded); // undefined
                    // console.log(item.ended); // true/false
                    if (item.ended === true) {
                        const overlay = document.createElement("div");
                        overlay.className = "event-overlay";
                        overlay.innerText = "(종료된 행사입니다)";
                        reviewItem.appendChild(overlay);
                    }
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
