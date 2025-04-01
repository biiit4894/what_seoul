document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("signupForm").addEventListener("submit", function (event) {
        event.preventDefault(); // 폼 기본 제출 동작 방지

        fetch('/api/user/signup', {
            method: 'POST',
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
            .then(response => {
                console.log(response);
                if (response.ok) {
                    alert('회원가입이 완료되었습니다.')
                    window.location.href = "/view/user/login"; // 로그인 페이지로 이동
                }
            })
            .catch(error => {
                console.error("회원가입 실패: ", error.response.data);
                alert("회원가입 실패: " + (error.response.data.message || "알 수 없는 오류"));
            });
    });
});