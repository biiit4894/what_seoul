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
        fetch('/api/user', {
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
});
