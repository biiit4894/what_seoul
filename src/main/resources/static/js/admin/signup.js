document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("signupForm").addEventListener("submit", function (event) {
        event.preventDefault(); // 폼 기본 제출 동작 방지

        // 기존 에러 메시지 초기화
        document.querySelectorAll(".error-message").forEach(el => el.remove());

        fetch('/api/admin/signup', {
            method: 'POST',
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                userId: document.getElementById("userId").value,
                password: document.getElementById("password").value,
                email: document.getElementById("email").value,
                nickName: document.getElementById("nickName").value
            })
        })
            .then(response =>
                response.json()
                    .then(
                        data => ({ status: response.status, body: data })
                    ))
            .then(({ status, body }) => {
                if (status === 201) {
                    alert('신규 관리자 계정 생성이 완료되었습니다.');
                    // window.location.href = "/login"; // 로그인 페이지 이동
                } else if (status === 400) {
                    displayErrors(body.context); // 서버에서 받은 에러 메시지 표시
                } else {
                    alert("신규 관리자 계정 생성 실패: " + (body.message || "알 수 없는 오류"));
                }
            })
            .catch(error => {
                console.error("신규 관리자 계정 생성 요청 중 오류 발생:", error);
                alert("신규 관리자 계정 생성 요청에 실패했습니다.");

            });
    });

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
});