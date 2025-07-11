window.addEventListener("DOMContentLoaded", () => {
    const tableBody = document.getElementById("area-table-body");
    const loadingSpinner = document.getElementById("loading-spinner");
    const scrollAnchor = document.getElementById("scroll-anchor");
    const searchInput = document.getElementById("areaName-input");
    const searchButton = document.getElementById("search-button");

    let currentPage = 0;
    // const pageSize = 10;
    let isLoading = false;
    let isAreaLast = false; // false로 시작해야 처음부터 로딩 가능
    let currentKeyword = "";

    // 화면 높이에 따라 변화하는 pageSize 계산
    const estimatedRowHeight = 60; // 예상 테이블 행 높이(px)
    const pageSize = Math.ceil(window.innerHeight / estimatedRowHeight) + 5; // 한 화면에 표시되는 행 개수 + 약간의 여유분

    // 날짜 포맷 함수
    function formatDate(dateStr) {
        if (!dateStr) {
            return "-";
        }
        const date = new Date(dateStr);
        return date.toLocaleString("ko-KR");
    }

    // 행 추가
    function createTableRow(area, index) {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td><input type="checkbox" class="row-checkbox" value="${area.id}"></td> 
            <td>${index}</td>
            <td>${area.category}</td>
            <td>${area.areaCode}</td>
            <td>${area.areaName}</td>
            <td>${formatDate(area.createdAt)}</td>
            <td>${formatDate(area.updatedAt)}</td>
            <td>${formatDate(area.deletedAt)}</td>
        `;
        return row;
    }

    // 데이터 로딩
    async function loadAreas() {
        if (isLoading || isAreaLast) return;

        isLoading = true;
        loadingSpinner.style.display = "block";

        try {
            const response = await fetch(`/api/admin/area/list?page=${currentPage}&size=${pageSize}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    areaName: currentKeyword || null
                })
            });

            if (!response.ok) {
                throw new Error("서울시 주요 장소 목록 조회에 실패했습니다.");
            }

            const data = await response.json();
            const slice = data.data;
            const areas = slice.content;

            areas.forEach((area, i) => {
                const rowNumber = currentPage * pageSize + i + 1; // 행별 인덱스(1부터 시작)
                const row = createTableRow(area, rowNumber);
                tableBody.appendChild(row);
            });

            isAreaLast = slice.last;
            currentPage++;
        } catch (error) {
            console.error("Error loading areas:", error);
        } finally {
            isLoading = false;
            loadingSpinner.style.display = "none";
        }
    }

    function isScrollAnchorVisible() {
        const rect = scrollAnchor.getBoundingClientRect();
        return rect.top < window.innerHeight;
    }

    // 뷰포트에 찰 때까지 재귀적으로 데이터 로드
    async function loadAreasRecursively() {
        await loadAreas();
        setTimeout(() => {
            if (!isAreaLast && !isScrollAnchorVisible()) {
                loadAreasRecursively();
            }
        }, 300);
    }

    // IntersectionObserver로 무한 스크롤 감지
    const observer = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting) {
            loadAreas();
        }
    }, {
        rootMargin: "200px",
    });

    observer.observe(scrollAnchor);

    // 검색 버튼 클릭 시
    searchButton.addEventListener("click", () => {
        currentKeyword = searchInput.value.trim();
        currentPage = 0;
        isAreaLast = false;
        tableBody.innerHTML = ""; // 기존 목록 초기화
        loadAreas();
    });

    searchInput.addEventListener("keyup", (event) => {
        if (event.key === "Enter") {
            searchButton.click();
        }
    });

    // 최초 1회 로딩
    loadAreas();

    // 서울시 주요 장소 .shp 파일 업로드 처리 이벤트 리스너
    const uploadForm = document.getElementById("uploadForm");
    if (uploadForm) {
        uploadForm.addEventListener("submit", async function (e) {
            e.preventDefault();

            const fileInput = document.getElementById("shpFile");
            if (!fileInput.files.length) {
                alert("파일을 선택해주세요.");
                return;
            }

            const confirmed = confirm("선택한 .shp 파일을 업로드하시겠습니까?");
            if (!confirmed) {
                return; // 업로드 중단
            }

            const formData = new FormData();
            formData.append("file", fileInput.files[0]);

            try {
                const response = await fetch("/api/admin/area", {
                    method: "POST",
                    body: formData,
                });

                if (response.ok) {
                    const res = await response.json();
                    alert(`업로드 완료\n\n총 항목: ${res.data.totalCount}\n저장됨: ${res.data.savedCount}\n중복 스킵: ${res.data.skippedCount}`);

                    // 모달 닫기
                    $('#uploadModal').modal('hide');

                    // 테이블 리셋 및 재로딩
                    currentPage = 0;
                    isAreaLast = false;
                    tableBody.innerHTML = "";
                    loadAreas();
                } else {
                    alert(`업로드 실패\nstatus: ${response.status}`);
                }
            } catch (err) {
                console.error("업로드 중 에러 발생:", err);
                alert("파일 업로드 중 문제가 발생했습니다.");
            }
        });
    }

    // 전체 선택 체크박스
    const selectAllCheckbox = document.getElementById("select-all-checkbox");
    selectAllCheckbox.addEventListener("change", () => {
        const rowCheckboxes = document.querySelectorAll(".row-checkbox");
        rowCheckboxes.forEach(cb => cb.checked = selectAllCheckbox.checked);
    });

    // 선택 삭제 버튼
    const deleteSelectedButton = document.getElementById("delete-selected-button");
    deleteSelectedButton.addEventListener("click", async () => {
        const checkedBoxes = Array.from(document.querySelectorAll(".row-checkbox:checked"));
        if (checkedBoxes.length === 0) {
            alert("삭제할 장소를 선택해주세요.");
            return;
        }

        const ids = checkedBoxes.map(cb => parseInt(cb.value));
        if (!confirm(`정말 ${ids.length}개의 장소를 삭제하시겠습니까?`)) return;

        try {
            const response = await fetch("/api/admin/area", {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(ids)
            });

            if (!response.ok) throw new Error("삭제 실패");

            alert("삭제가 완료되었습니다.");
            // 테이블 갱신
            currentPage = 0;
            isAreaLast = false;
            tableBody.innerHTML = "";
            loadAreas();
        } catch (e) {
            console.error("삭제 에러:", e);
            alert("삭제 중 오류가 발생했습니다.");
        }
    });
});
