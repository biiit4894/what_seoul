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
    function createTableRow(area) {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${area.id}</td>
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

            areas.forEach(area => {
                const row = createTableRow(area);
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
});
