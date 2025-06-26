function logout() {
    if (confirm("로그아웃 하시겠습니까?")) {
        fetch('/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
        }).then(response => {
            console.log(response);
            if (response.ok) {
                alert("로그아웃이 완료되었습니다.")
                window.location.href = '/';
            }
        }).catch(error => {
            console.log("Error: ", error);
        });
    }

}