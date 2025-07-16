window.addEventListener("DOMContentLoaded", () => {
    const expiration = sessionStorage.getItem("accessTokenExpiration");
    const loginStartTime = sessionStorage.getItem("loginStartTime");

    // loginStartTime이 없다면 로그인 상태가 아님 -> 자동 재발급 로직 실행 X
    if (!expiration || !loginStartTime) {
        return;
    }

    const parsedExpiration = parseInt(expiration);
    if (!isNaN(parsedExpiration)) {
        setupAutoRefresh(parsedExpiration, true);
    }
});

// refresh 토큰 만료 5분 전에 자동 갱신을 시도하는 함수
// - 페이지 로드시 accessToken 만료 시간을 기준으로 자동 재발급 타이머 설정
function setupAutoRefresh(expirationTime, isInitialLoad = false) {

    const now = Date.now();
    // 자동 재발급 허용 시간을 초과하면 재발급 차단
    if(isAutoRefreshExpired()) {
        fetch("/api/auth/logout", {
            method: "POST",
            credentials: "include"
        }).then(res => {
            if (res.ok) {
                sessionStorage.removeItem("accessTokenExpiration");
                sessionStorage.removeItem("loginStartTime");

                alert("-- 로그인 유효 시간이 만료되었습니다. 다시 로그인해주세요.");
                window.location.href = "/login";
            } else {
                console.log("로그아웃 실패 (응답 오류)");
            }
        }).catch(err => {
            console.log("로그아웃 요청 실패:", err);
        });
        return;
    }

    const timeUntilExpiry = expirationTime - now;
    // const refreshTime = timeUntilExpiry - 30000; // 테스트용(30초 전)
    // const refreshTime = timeUntilExpiry - 60000; // 테스트용(1분 전)
    const refreshTime = timeUntilExpiry - 300000; // 실서비스용(5분 전)

    if (refreshTime > 0) {
        // 여유가 있는 경우: 만료 1분 전에 재발급 예약
        console.log(`재발급 예약됨: ${Math.floor(refreshTime / 1000)}초 후`);
        setTimeout(() => {
            tryReissueToken();
        }, refreshTime);
    } else if (timeUntilExpiry > 0) {
        // 이미 만료까지 1분 미만이지만 아직 만료되지 않음
        // 바로 재발급은 하지 않지만, 즉시 재발급 타이머를 걸어줌
        if (isInitialLoad) {
            console.log("accessToken 만료 1분 이내: 곧바로 재발급 예약");
            setTimeout(() => {
                tryReissueToken();
            }, 1000); // 1초 뒤에 재발급 시도
        } else {
            // 일반 실행 상황에서는 즉시 재발급 예약
            console.log("accessToken 만료 1분 이내: 재발급 예약");
            setTimeout(() => {
                tryReissueToken();
            }, 1000);
        }

    } else {
        // 이미 만료된 상태
        console.log("accessToken이 이미 만료됨");
        tryReissueToken(); // 즉시 재발급 시도
    }

    // accessToken 재발급 요청 함수
    function tryReissueToken() {

        // 재발급 전에 한 번 더 자동 갱신 허용 시간 체크
        // 자동 재발급 허용 시간을 초과하면 재발급 차단
        if(isAutoRefreshExpired()) {
            fetch("/api/auth/logout", {
                method: "POST",
                credentials: "include"
            }).then(res => {
                if (res.ok) {
                    sessionStorage.removeItem("accessTokenExpiration");
                    sessionStorage.removeItem("loginStartTime");

                    alert(">> 로그인 유효 시간이 만료되었습니다. 다시 로그인해주세요.");
                    window.location.href = "/login";
                } else {
                    console.log("로그아웃 실패 (응답 오류)");
                }
            }).catch(err => {
                console.log("로그아웃 요청 실패:", err);
            });
            return;
        }

        fetch("/api/auth/access/reissue", {
            method: "POST",
            credentials: "include"
        }).then(res => res.json()
            .then(data => {
                console.log("재발급 응답 성공 여부:", data.success);

                if (!res.ok || !data.success) {
                    alert("세션이 만료되었습니다. 다시 로그인해주세요.");
                    window.location.href = "/login";
                    return;
                }

                // accessToken 재발급 성공 시, 새 만료 시간 저장
                const newExpiration = data.data.accessTokenExpiration;
                console.log("새 accessTokenExpiration:", newExpiration);

                if (!newExpiration || isNaN(newExpiration)) {
                    console.log("!! 유효하지 않은 만료시간, 자동 재발급 중단됨");
                    return;
                }

                sessionStorage.setItem("accessTokenExpiration", newExpiration);

                // 타이머 다시 설정
                setupAutoRefresh(parseInt(newExpiration));
            }))
            .catch(err => {
                console.log("재발급 요청 실패:", err);
                alert("세션이 만료되었습니다. 다시 로그인해주세요.");
                window.location.href = "/login";
            });
    }
}

// 로그인 이후 일정 시간이 지나면 자동 재발급을 차단하기 위해 사용되는 함수
function isAutoRefreshExpired() {
    const loginStartTime = parseInt(sessionStorage.getItem("loginStartTime"));
    // const maxAutoRefreshDuration = 3 * 60 * 1000; // 테스트용(3분)
    const maxAutoRefreshDuration = 3 * 60 * 60 * 1000; // 실서비스용
    return Date.now() - loginStartTime > maxAutoRefreshDuration;
}


// 인증이 필요한 모든 API 요청에 사용할 수 있는 공통 함수 (미완)
async function fetchWithAuth(url, options = {}) {
    // 항상 credentials 포함
    const opts = {
        ...options,
        credentials: "include"
    };

    let response = await fetch(url, opts);

    if (response.status === 401) {
        // 액세스토큰 만료 -> 리프레시 토큰으로 갱신 시도
        const reissueRes = await fetch("/api/auth/access/reissue", {
            method: "POST",
            credentials: "include"
        });

        console.log(reissueRes);
        if (reissueRes.ok) {
            // 재발급 성공 -> 원래 요청 다시 시도
            return fetch(url, opts);
        } else {
            // 재발급 실패 -> 로그인 페이지로 이동
            alert("세션이 만료되었습니다. 다시 로그인해주세요.");
            window.location.href = "/login";
            return Promise.reject("세션 만료");
        }
    }

    return response;
}

function logout() {
    if (confirm("로그아웃 하시겠습니까?")) {

        fetch('/api/auth/logout', {
            method: 'POST',
            credentials: "include"
        }).then(response => {
            console.log(response);
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
    }

}