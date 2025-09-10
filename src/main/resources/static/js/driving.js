document.addEventListener('DOMContentLoaded', () => {

    document.getElementById('trip-id-display').textContent = "내 출퇴근 기록";

    let map;
    let speedRpmChart, throttleBrakeChart;

    // 메인 로직 (이전과 동일)
    async function loadDrivingDataFromFile() {
        try {
            const response = await fetch('/data/driving-log/driving-log.json'); 
            if (!response.ok) throw new Error('driving-log.json 파일을 불러올 수 없습니다.');
            
            const data = await response.json();
            if (data.length === 0) {
                console.warn('파일에 데이터가 없습니다.');
                return;
            }
            
            const centerPoint = data[Math.floor(data.length / 2)];
            initMap(centerPoint.latitude, centerPoint.longitude);
            
            drawPath(data);
            updateCharts(data);

        } catch (error) {
            console.error(error);
        }
    }
    
    // --- ⭐️ 지도 관련 함수 (Leaflet.js 용으로 수정) ---
    function initMap(centerLat, centerLng) {
        // Leaflet 지도를 'map' div에 초기화합니다.
        map = L.map('map').setView([centerLat, centerLng], 15); // 초기 확대 레벨 설정

        // OpenStreetMap의 지도 타일을 추가합니다. (API 키 필요 없음)
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map);
    }

    function drawPath(pathData) {
        // Leaflet은 [위도, 경도] 쌍의 배열을 사용합니다.
        const latlngs = pathData.map(log => [log.latitude, log.longitude]);

        // 경로를 지도에 그립니다.
        const polyline = L.polyline(latlngs, { color: 'red' }).addTo(map);
        
        // 경로가 화면에 잘 보이도록 지도의 경계를 자동으로 조절합니다.
        map.fitBounds(polyline.getBounds());
    }

    // --- 차트 업데이트 함수 (이전과 동일) ---
    function updateCharts(logData) {
        const labels = logData.map(log => new Date(log.log_time).toLocaleTimeString());
        
        speedRpmChart.data.labels = labels;
        speedRpmChart.data.datasets[0].data = logData.map(d => d.speed);
        speedRpmChart.data.datasets[1].data = logData.map(d => d.rpm);
        speedRpmChart.update();
        
        throttleBrakeChart.data.labels = labels;
        throttleBrakeChart.data.datasets[0].data = logData.map(d => d.throttle);
        throttleBrakeChart.data.datasets[1].data = logData.map(d => d.brake);
        throttleBrakeChart.update();
    }
    
    // --- 차트 초기화 (이전과 동일) ---
    const chartOptions = { responsive: true, scales: { x: { ticks: { maxTicksLimit: 10 } } } };
    const speedRpmCtx = document.getElementById('speed-rpm-chart').getContext('2d');
    speedRpmChart = new Chart(speedRpmCtx, { type: 'line', data: { labels:[], datasets:[{label:'속도 (km/h)', data:[], borderColor:'rgba(54, 162, 235, 1)', yAxisID:'y_speed'},{label:'RPM',data:[],borderColor:'rgba(75, 192, 192, 1)',yAxisID:'y_rpm'}]}, options: {...chartOptions, scales:{...chartOptions.scales, y_speed:{position:'left'}, y_rpm:{position:'right', grid:{drawOnChartArea:false}}}}});
    const throttleBrakeCtx = document.getElementById('throttle-brake-chart').getContext('2d');
    throttleBrakeChart = new Chart(throttleBrakeCtx, { type: 'line', data: { labels:[], datasets:[{label:'가속 페달 (%)', data:[], borderColor:'rgba(255, 159, 64, 1)'},{label:'브레이크 (%)',data:[],borderColor:'rgba(255, 99, 132, 1)'}]}, options: chartOptions});

    // 메인 함수 호출
    loadDrivingDataFromFile();
});