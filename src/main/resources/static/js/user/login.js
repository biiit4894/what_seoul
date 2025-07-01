let loginMode = "user"; // 기본은 일반 유저 로그인

function switchToUserLogin() {
    loginMode = "user";
    document.getElementById("login-title").textContent = "일반 회원 로그인";

    // 버튼 스타일 업데이트
    const userBtn = document.getElementById("userLoginBtn");
    const adminBtn = document.getElementById("adminLoginBtn");

    userBtn.classList.remove("btn-outline-primary");
    userBtn.classList.add("btn-primary");

    adminBtn.classList.remove("btn-secondary");
    adminBtn.classList.add("btn-outline-secondary");
}

function switchToAdminLogin() {
    loginMode = "admin";
    document.getElementById("login-title").textContent = "관리자 로그인";

    // 버튼 스타일 업데이트
    const userBtn = document.getElementById("userLoginBtn");
    const adminBtn = document.getElementById("adminLoginBtn");

    adminBtn.classList.remove("btn-outline-secondary");
    adminBtn.classList.add("btn-secondary");

    userBtn.classList.remove("btn-primary");
    userBtn.classList.add("btn-outline-primary");
}

async function login(event) {
    event.preventDefault();

    const userId = document.getElementById("userId").value;
    const password = document.getElementById("password").value;

    const url = loginMode === "admin" ? "/api/admin/login" : "/api/user/login";
    const payload = {
        userId: userId,
        password: password
    };

    try {
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload),
            credentials: "include" // 쿠키 포함
        });

        const data = await response.json();

        if (!response.ok || !data.success) {
            throw new Error(data.context || data.message || "로그인 실패");
        }

        // console.log("토큰 만료 ms: ", data.data.accessTokenExpiration);
        sessionStorage.setItem("accessTokenExpiration", data.data.accessTokenExpiration);
        sessionStorage.setItem("loginStartTime", Date.now().toString());

        // 더 이상 accessToken을 저장하지 않음. 쿠키에 저장됨.
        alert("로그인 되었습니다.");
        window.location.href = "/";
    } catch (err) {
        // console.log(err);
        // console.log(err.name)
        // console.log(err.message);
        const errorBox = document.getElementById("errorMessage");
        errorBox.textContent = err.message;
        errorBox.classList.remove("d-none");
    }
}
