// js/devices.js (통합 버전)

document.addEventListener('DOMContentLoaded', function() {

	// ===============================================================
	// ## 'st-devices.html' (기기 관리) 페이지를 위한 코드
	// ===============================================================
	// '에어컨 목록 불러오기' 버튼이 있는 경우에만 이 코드를 실행합니다.
	if (document.getElementById('load-ac-button')) {
		const loadButton = document.getElementById('load-ac-button');
		const tableBody = document.getElementById('devices-table-body');

		const registrationModalEl = document.getElementById('registrationModal');
		const registrationModal = new bootstrap.Modal(registrationModalEl);
		const deleteConfirmModalEl = document.getElementById('deleteConfirmModal');
		const deleteConfirmModal = new bootstrap.Modal(deleteConfirmModalEl);

		loadButton.addEventListener('click', loadAirConditioners);
		document.getElementById('save-device-button').addEventListener('click', saveDevice);
		document.getElementById('confirm-delete-button').addEventListener('click', confirmDelete);
		tableBody.addEventListener('click', handleTableButtonClick);

		function loadAirConditioners() {
			tableBody.innerHTML = '<tr><td colspan="4" class="text-center">로딩 중...</td></tr>';
			fetch('/api/smartthings/getAirconDevices')
				.then(response => response.json())
				.then(devices => renderDevicesTable(devices))
				.catch(error => {
					console.error('Error:', error);
					tableBody.innerHTML = `<tr><td colspan="4" class="text-center text-danger">오류 발생: ${error.message}</td></tr>`;
				});
		}

		function renderDevicesTable(devices) {
			tableBody.innerHTML = '';
			if (devices.length === 0) {
				tableBody.innerHTML = '<tr><td colspan="4" class="text-center">등록된 에어컨이 없습니다.</td></tr>';
				return;
			}
			devices.forEach(device => {
				const buttonHtml = device.registered
					? `<span class="badge bg-success me-2">등록됨</span><button class="btn btn-danger btn-sm delete-btn" data-device-id="${device.deviceId}">삭제</button>`
					: `<button class="btn btn-success btn-sm register-btn" data-device-id="${device.deviceId}" data-label="${device.label}">등록하기</button>`;
				const row = `<tr data-row-id="${device.deviceId}"><td>${device.label}</td><td>${device.deviceId}</td><td>${device.deviceTypeName}</td><td class="text-center">${buttonHtml}</td></tr>`;
				tableBody.insertAdjacentHTML('beforeend', row);
			});
		}

		function handleTableButtonClick(event) {
			const button = event.target;
			if (button.classList.contains('register-btn')) {
				const deviceId = button.dataset.deviceId;
				const label = button.dataset.label;
				document.getElementById('modal-device-id').value = deviceId;
				document.getElementById('modal-device-label').value = label;
				document.getElementById('device-label-display').value = label;
				document.getElementById('room-select').selectedIndex = 0;
				registrationModal.show();
			} else if (button.classList.contains('delete-btn')) {
				deleteConfirmModalEl.dataset.deviceId = button.dataset.deviceId;
				deleteConfirmModal.show();
			}
		}

		function confirmDelete() {
			deleteDevice(deleteConfirmModalEl.dataset.deviceId);
		}

		function saveDevice() {
			const deviceId = document.getElementById('modal-device-id').value;
			const label = document.getElementById('modal-device-label').value;
			const room = document.getElementById('room-select').value;
			if (room === "공간을 선택하세요...") {
				alert("설치된 공간을 선택해주세요.");
				return;
			}
			fetch('/api/smartthings/register-device', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ deviceId, label, room: parseInt(room) })
			}).then(response => {
				if (!response.ok) throw new Error('등록 실패');
				registrationModal.hide();
				loadAirConditioners();
			}).catch(error => alert(error.message));
		}

		function deleteDevice(deviceId) {
			fetch(`/api/smartthings/delete/${deviceId}`, { method: 'DELETE' })
				.then(response => {
					if (!response.ok) throw new Error('삭제 실패');
					deleteConfirmModal.hide();
					loadAirConditioners();
				}).catch(error => {
					deleteConfirmModal.hide();
					alert(error.message);
				});
		}
	}


	// ===============================================================
	// ## 'st-data.html' (최근 데이터) 페이지를 위한 코드
	// ===============================================================
	// '조회하기' 버튼이 있는 경우에만 이 코드를 실행합니다.
	if (document.getElementById('search-button')) {
		const deviceSelect = document.getElementById('device-select');
		const startDateInput = document.getElementById('start-date');
		const endDateInput = document.getElementById('end-date');
		const searchButton = document.getElementById('search-button');
		const noDataMessage = document.getElementById('no-data-message');

		// 차트 인스턴스를 저장할 변수
		let comboChartInstance = null;

		// 페이지가 처음 로드될 때 실행되는 초기화 함수
		function initializeDataPage() {
			const today = new Date().toISOString().split('T')[0];
			startDateInput.value = today;
			endDateInput.value = today;
			noDataMessage.classList.remove('d-none'); // 처음에는 "조회 조건을 선택해주세요" 메시지 표시

			fetch('/api/smartthings/registered-devices')
				.then(response => response.json())
				.then(devices => {
					deviceSelect.innerHTML = '<option selected disabled>기기를 선택하세요...</option>';
					if (devices.length === 0) {
						deviceSelect.innerHTML = '<option disabled>등록된 기기가 없습니다.</option>';
						searchButton.disabled = true;
					} else {
						devices.forEach(device => {
							// 사용자가 수정한 device.label을 사용
							const option = `<option value="${device.deviceId}">${device.label}</option>`;
							deviceSelect.insertAdjacentHTML('beforeend', option);
						});
					}
				})
				.catch(error => console.error('Error fetching devices:', error));
		}

		// '조회하기' 버튼 클릭 시 실행될 함수
		function searchLogs() {
			const deviceId = deviceSelect.value;
			const startDate = startDateInput.value;
			const endDate = endDateInput.value;
			if (!deviceId || deviceId === '기기를 선택하세요...') {
				alert('기기를 선택해주세요.');
				return;
			}
			noDataMessage.classList.add('d-none'); // 로딩 시작 시 메시지 숨김

			const url = `/api/smartthings/logs?deviceId=${deviceId}&startDate=${startDate}&endDate=${endDate}`;
			fetch(url)
				.then(response => response.json())
				.then(logs => {
					renderChart(logs); // 차트 그리기 함수 호출
				})
				.catch(error => {
					console.error('Error:', error);
					noDataMessage.innerText = '데이터 조회 중 오류가 발생했습니다.';
					noDataMessage.classList.remove('d-none');
				});
		}

		// 다중 축 차트를 그리는 함수
		// 다중 축 차트를 그리는 함수 (최종 완성 버전)
		function renderChart(logs) {
			if (comboChartInstance) {
				comboChartInstance.destroy();
			}

			if (logs.length === 0) {
				noDataMessage.innerText = '해당 기간의 데이터가 없습니다.';
				noDataMessage.classList.remove('d-none');
				return;
			}
			noDataMessage.classList.add('d-none');

			const reversedLogs = logs.slice().reverse();
			const labels = reversedLogs.map(log => new Date(log.dataCollectionTimestamp).toLocaleString('ko-KR'));
			const targetTemps = reversedLogs.map(log => log.targetTemperature);
			const currentTemps = reversedLogs.map(log => log.currentTemperature);

			let lastKnownFanSpeed = 0;
			const fanBarColors = [];

			const fanSpeeds = reversedLogs.map(log => {
				switch (log.mode) {
					case 'cool': fanBarColors.push('rgba(54, 162, 235, 0.7)'); break;
					case 'wind': fanBarColors.push('rgba(75, 192, 192, 0.7)'); break;
					case 'aIComfort': fanBarColors.push('rgba(201, 203, 207, 0.7)'); break;
					default: fanBarColors.push('rgba(201, 203, 207, 0.7)');
				}

				let currentFanSpeed;
				if (log.power === 'off') {
					currentFanSpeed = 0;
				} else {
					switch (log.fanMode) {
						case 'auto': currentFanSpeed = lastKnownFanSpeed; break;
						case '1': currentFanSpeed = 1; break;
						case '2': currentFanSpeed = 2; break;
						case '3': currentFanSpeed = 3; break;
						case '4': currentFanSpeed = 4; break;
						case 'max': currentFanSpeed = 5; break;
						default: currentFanSpeed = lastKnownFanSpeed;
					}
				}
				lastKnownFanSpeed = currentFanSpeed;
				return currentFanSpeed;
			});

			const ctx = document.getElementById('comboChart').getContext('2d');
			comboChartInstance = new Chart(ctx, {
				type: 'bar',
				data: {
					labels: labels,
					datasets: [
						{
							label: '풍량',
							data: fanSpeeds,
							backgroundColor: fanBarColors,
							yAxisID: 'y_fan',
							// ❗️❗️❗️ 여기에 옵션 추가 ❗️❗️❗️
							minBarLength: 5,         // 0단일 때도 막대가 보이도록 최소 높이 5px 지정
							barPercentage: 1.0,      // 막대 사이 간격 제거
							categoryPercentage: 1.0, // 막대 사이 간격 제거
						},
						{
							label: '목표 온도',
							data: targetTemps,
							borderColor: 'rgba(255, 99, 132, 1)',
							borderDash: [5, 5],
							stepped: true,
							type: 'line',
							yAxisID: 'y_temp',
						},
						{
							label: '현재 온도',
							data: currentTemps,
							borderColor: 'rgba(75, 192, 192, 1)',
							stepped: true,
							type: 'line',
							yAxisID: 'y_temp',
						}
					]
				},
				options: {
					scales: {
						y_temp: {
							position: 'left',
							title: { display: true, text: '온도 (°C)' }
						},
						y_fan: {
							position: 'right',
							min: 0,
							max: 5,
							ticks: {
								stepSize: 1,
								callback: (value) => ['0단', '1단', '2단', '3단', '4단', 'Max'][value]
							},
							title: { display: true, text: '풍량' },
							grid: { drawOnChartArea: false }
						}
					}
				}
			});
		}

		// 이벤트 리스너 연결 및 초기화 함수 실행
		searchButton.addEventListener('click', searchLogs);
		initializeDataPage();
	}
});