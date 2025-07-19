document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("findIdForm");
    if (form) {
        form.addEventListener("submit", function (event) {
            event.preventDefault();

            const email = document.getElementById("email").value;
            const successMsg = document.getElementById("successMessage");
            const errorMsg = document.getElementById("errorMessage");
            const loadingSpinner = document.getElementById("loadingSpinner");

            successMsg.classList.add("d-none");
            errorMsg.classList.add("d-none");

            let loadingTimer = null;

            // 1.5초 후 로딩 스피너 표시
            loadingTimer = setTimeout(() => {
                loadingSpinner.classList.remove("d-none");
            }, 1500);

            fetch("/api/user/find/id", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email: email })
            })
                .then(response => {
                    return response.json().then(data => ({
                        ok: response.ok,
                        data: data
                    }));
                })
                .then(result => {
                    clearTimeout(loadingTimer); // 로딩 예약 취소
                    loadingSpinner.classList.add("d-none");

                    if (result.ok) {
                        successMsg.classList.remove("d-none");
                        errorMsg.classList.add("d-none");
                    } else {
                        successMsg.classList.add("d-none");
                        errorMsg.classList.remove("d-none");
                    }
                })
                .catch(error => {
                    clearTimeout(loadingTimer);
                    loadingSpinner.classList.add("d-none");
                    console.error("요청 실패:", error);
                    successMsg.classList.add("d-none");
                    errorMsg.classList.remove("d-none");
                });
        });
    }
});
