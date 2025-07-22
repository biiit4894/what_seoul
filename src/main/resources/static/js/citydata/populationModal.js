function populationModal(data) {
    // 기존 모달 제거
    const existingModal = document.getElementById("forecast-modal");
    if (existingModal) existingModal.remove();

    // 모달 요소 생성
    const modal = document.createElement("div");
    modal.id = "population-modal";
    modal.className = "population-modal";

    const modalContent = document.createElement("div");
    modalContent.className = "population-modal-content";

    const modalHeader = document.createElement("div");
    modalHeader.className = "modal-header";

    const title = document.createElement("h5");
    title.innerText = `👥 ${areaName}의 인구 현황`;

    const closeButton = document.createElement("span");
    closeButton.className = "close-button";
    closeButton.innerHTML = "&times;";
    closeButton.onclick = () => modal.remove();

    modalHeader.appendChild(title);
    modalHeader.appendChild(closeButton);

    // 실시간 인구 현황 데이터
    const populationInfoSection = document.createElement("div");
    populationInfoSection.className = "modal-population-info-section";
    populationInfoSection.innerHTML = `
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; text-align: left; font-size: 0.95rem;">
            <div><strong>📊 혼잡도</strong><br/><span class=text-small-ppltn>${data.data.congestionLevel}</span></div>
            <div><strong>👤 현재 인구</strong><br/><span class="text-small-ppltn">${Number(data.data.populationMin).toLocaleString()}명 ~ ${Number(data.data.populationMax).toLocaleString()}명</span></div>
            <div style="grid-column: span 2;"><strong>📝 메시지</strong><br/><span class="text-small-ppltn">${data.data.congestionMessage}</span></div>
            <div style="grid-column: span 2; text-align: right; font-size: 0.8rem; color: #777;">업데이트: ${data.data.populationUpdateTime}</div>
        </div>
        <hr style="margin-top: 12px;"/>
    `;

    // 그래프 제목
    const chartTitle = document.createElement("h6");
    chartTitle.innerText = "📈 향후 12시간 동안의 예측 인구";
    chartTitle.style.margin = "10px 0 6px";
    chartTitle.style.fontWeight = "bold";
    chartTitle.style.textAlign = "left";

    // 인구 예측값 데이터 (향후 12시간)
    // Chart.js에 쓸 canvas 요소 생성
    const canvas = document.createElement("canvas");
    canvas.id = "populationForecastChart";
    canvas.width = 400;
    canvas.height = 300;

    // canvas를 감싸는 wrapper div 생성 (오버레이)
    const canvasWrapper = document.createElement("div");
    canvasWrapper.style.position = "relative";
    canvasWrapper.style.width = "400px";  // 캔버스 너비와 맞춤
    canvasWrapper.style.height = "300px"; // 캔버스 높이와 맞춤

    // canvas 스타일 수정 (wrapper 안에서 위치 맞춤)
    canvas.style.position = "absolute";
    canvas.style.top = "0";
    canvas.style.left = "0";
    canvas.style.width = "100%";
    canvas.style.height = "100%";

    // canvasWrapper에 canvas 추가
    canvasWrapper.appendChild(canvas);

    // forecasts 데이터
    const forecastsData = data.data.forecasts;

    // forecasts가 빈 배열일 경우 오버레이 생성
    if (!forecastsData || forecastsData.length === 0) {
        const overlay = document.createElement("div");
        overlay.style.position = "absolute";
        overlay.style.top = "0";
        overlay.style.left = "0";
        overlay.style.width = "100%";
        overlay.style.height = "100%";
        overlay.style.backgroundColor = "rgba(255, 255, 255, 0.7)";
        overlay.style.display = "flex";
        overlay.style.alignItems = "center";
        overlay.style.justifyContent = "center";
        overlay.style.fontSize = "1rem";
        overlay.style.fontWeight = "bold";
        overlay.style.color = "#333";
        overlay.innerText = "예측 인구 데이터를 준비 중이에요!";

        canvasWrapper.appendChild(overlay);
    }

    // 모달 구조 구성
    modalContent.appendChild(modalHeader);
    modalContent.appendChild(populationInfoSection);
    modalContent.appendChild(chartTitle);
    modalContent.appendChild(canvasWrapper); // canvas 대신 canvasWrapper를 append
    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    // 바깥 클릭 시 닫기
    modal.addEventListener("click", (e) => {
        if (e.target === modal) modal.remove();
    });

    // Chart.js 데이터 구성
    const ctx = canvas.getContext("2d");
    // const forecastsData = data.data.forecasts;

    const labels = data.data.forecasts.map(f => f.forecastTime.slice(11, 16));
    const minData = data.data.forecasts.map(f => parseInt(f.forecastPopulationMax));
    const maxData = data.data.forecasts.map(f => parseInt(f.forecastPopulationMin));
    const congestion = data.data.forecasts.map(f => f.forecastCongestionLevel);

    // 기존 차트 제거
    if (window.populationForecastChartInstance) {
        window.populationForecastChartInstance.destroy();
    }

    window.populationForecastChartInstance = new Chart(ctx, {
        type: "line",
        data: {
            labels,
            datasets: [
                {
                    label: "예상 최소 인구 수",
                    data: minData,
                    fill: true,
                    borderColor: "rgba(255, 99, 132, 1)",
                    backgroundColor: "rgba(255, 99, 132, 0.2)",
                    borderWidth: 2,
                    tension: 0.4,
                },
                {
                    label: "예상 최대 인구 수",
                    data: maxData,
                    fill: true,
                    borderColor: "rgba(54, 162, 235, 1)",
                    backgroundColor: "rgba(54, 162, 235, 0.2)",
                    borderWidth: 2,
                    tension: 0.4,
                }
            ]
        },
        options: {
            plugins: {
                tooltip: {
                    callbacks: {
                        afterLabel: function (context) {
                            return `혼잡도: ${congestion[context.dataIndex]}`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    title: {
                        display: true,
                        text: '예상 인구 수'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: '시간'
                    }
                }
            }
        }
    });
}