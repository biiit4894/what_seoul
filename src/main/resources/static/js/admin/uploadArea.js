window.addEventListener("DOMContentLoaded", () => {
    document.getElementById('uploadForm').addEventListener('submit', async function (e) {
        e.preventDefault();

        const fileInput = document.getElementById('shpFile');
        const formData = new FormData();
        formData.append("file", fileInput.files[0]);

        const response = await fetch('/api/admin/area', {
            method: 'POST',
            body: formData
        });

        const resultDiv = document.getElementById('result');

        if (response.ok) {
            const res = await response.json();
            console.log(res);
            resultDiv.innerText = `업로드 성공\n\n총 항목: ${res.data.totalCount}\n저장됨: ${res.data.savedCount}\n중복 스킵: ${res.data.skippedCount}`;
        } else {
            resultDiv.innerText = `업로드 실패\n\nstatus: ${response.status}`;
            console.log(response.json());
        }
    });
});