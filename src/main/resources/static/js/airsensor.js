document.addEventListener('DOMContentLoaded', () => {

    // ❗️ 중요: 여기에 본인의 디바이스 ID를 입력하세요.
    const DEVICE_ID = "JSY1109"; 

    // HTML 요소 가져오기
    const deviceIdDisplay = document.getElementById('device-id-display');
    const datePicker = document.getElementById('date-picker');
    const latestTemp = document.getElementById('latest-temp');
    const latestHumi = document.getElementById('latest-humi');
    const latestCo2 = document.getElementById('latest-co2');
    const latestDust10 = document.getElementById('latest-dust10');
    const latestDust25 = document.getElementById('latest-dust25');
    const latestTime = document.getElementById('latest-time');

    // Chart.js 초기화 (이전과 동일)
    const ctx = document.getElementById('daily-chart').getContext('2d');
    let dailyChart = new Chart(ctx, { 
        type: 'line', data: { labels: [], datasets: [ { label: '온도 (°C)', data: [], borderColor: 'rgba(255, 99, 132, 1)', yAxisID: 'y_temp', }, { label: '습도 (%)', data: [], borderColor: 'rgba(54, 162, 235, 1)', yAxisID: 'y_humi', }, { label: '미세먼지 (PM2.5)', data: [], borderColor: 'rgba(75, 192, 192, 1)', yAxisID: 'y_dust', } ] }, options: { responsive: true, scales: { x: { display: true, title: { display: true, text: '시간' } }, y_temp: { type: 'linear', display: true, position: 'left', title: { display: true, text: '온도 (°C)' } }, y_humi: { type: 'linear', display: true, position: 'right', title: { display: true, text: '습도 (%)' }, grid: { drawOnChartArea: false } }, y_dust: { type: 'linear', display: false, } } }
    });
    
    // 최신 데이터를 가져와서 카드에 업데이트하는 함수
    async function fetchLatestData() {
        try {
            // 변경된 API 경로: /api/airsensor/latest/{deviceId}
            const response = await fetch(`/api/airsensor/latest/${DEVICE_ID}`);
            if (!response.ok) throw new Error('최신 데이터를 가져오지 못했습니다.');
            
            const data = await response.json();
            
            latestTemp.textContent = `${data.temp} °C`;
            latestHumi.textContent = `${data.humi} %`;
            latestCo2.textContent = `${data.co2} ppm`;
            latestDust10.textContent = `${data.dust10} µg/m³`;
            latestDust25.textContent = `${data.dust25} µg/m³`;
            
            const saveDate = new Date(data.id.saveDate);
            latestTime.textContent = saveDate.toLocaleString('ko-KR');

        } catch (error) {
            console.error(error);
            latestTime.textContent = "데이터 로딩 실패";
        }
    }

    // 특정 날짜의 데이터를 가져와서 차트를 그리는 함수
    async function fetchDailyData(dateString) {
        try {
            // 변경된 API 경로: /api/airsensor/daily?deviceId=...&date=...
            const response = await fetch(`/api/airsensor/daily?deviceId=${DEVICE_ID}&date=${dateString}`);
            if (!response.ok) throw new Error('일일 데이터를 가져오지 못했습니다.');

            const data = await response.json();

            const labels = data.map(d => new Date(d.id.saveDate).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' }));
            const tempData = data.map(d => d.temp);
            const humiData = data.map(d => d.humi);
            const dust25Data = data.map(d => d.dust25);

            dailyChart.data.labels = labels;
            dailyChart.data.datasets[0].data = tempData;
            dailyChart.data.datasets[1].data = humiData;
            dailyChart.data.datasets[2].data = dust25Data;
            dailyChart.update();

        } catch (error) {
            console.error(error);
        }
    }
    
    datePicker.addEventListener('change', () => {
        fetchDailyData(datePicker.value);
    });

    function initialize() {
        deviceIdDisplay.textContent = DEVICE_ID;
        const today = new Date().toISOString().split('T')[0];
        datePicker.value = today;

        fetchLatestData();
        fetchDailyData(today);
        
        setInterval(fetchLatestData, 60000); // 1분마다 최신 데이터 자동 갱신
    }

    initialize();
});