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
	
	// ===============================================================
	// ## 'st-control.html' (기기 제어) 페이지를 위한 코드
	// ===============================================================
	// 'control-cards-container' 요소가 있는 경우에만 이 코드를 실행합니다.
	if (document.getElementById('control-cards-container')) {
	    const container = document.getElementById('control-cards-container');
	    const loadingMessage = document.getElementById('loading-message');

	    // 제어 페이지 초기화 함수
	    function initializeControlPage() {
	        loadingMessage.style.display = 'block';
	        container.innerHTML = ''; // 기존 카드 삭제
	        container.appendChild(loadingMessage);

	        fetch('/api/smartthings/devices-with-status')
	            .then(response => response.json())
	            .then(devices => {
	                loadingMessage.style.display = 'none';
	                if (devices.length === 0) {
	                    container.innerHTML = '<div class="col-12"><p class="text-center text-muted">등록된 기기가 없습니다.</p></div>';
	                    return;
	                }
	                devices.forEach(device => {
	                    container.insertAdjacentHTML('beforeend', createDeviceCardHtml(device));
	                });
	            })
	            .catch(error => {
	                console.error("Error fetching devices with status:", error);
	                loadingMessage.style.display = 'none';
	                container.innerHTML = '<div class="col-12"><p class="text-center text-danger">기기 목록을 불러오는 중 오류가 발생했습니다.</p></div>';
	            });
	    }

	    // 기기 카드 HTML을 생성하는 함수
	    function createDeviceCardHtml(device) {
	        // 현재 상태 표시 (상태가 null일 경우 대비)
	        const power = device.power || 'unknown';
	        const mode = device.mode || 'N/A';
	        const currentTemp = device.currentTemperature != null ? `${device.currentTemperature}°C` : 'N/A';
	        const targetTemp = device.targetTemperature != null ? `${device.targetTemperature}°C` : 'N/A';
	        
	        // 제어 UI용 초기값 설정
	        const isChecked = power === 'on' ? 'checked' : '';
	        const selectedMode = device.mode || 'cool';
	        const selectedTemp = Math.round(device.targetTemperature || 24);

	        // 온도 옵션 HTML 생성 (16~30도)
	        let tempOptions = '';
	        for (let i = 16; i <= 30; i++) {
	            tempOptions += `<option value="${i}" ${i === selectedTemp ? 'selected' : ''}>${i}°C</option>`;
	        }
	        
	        return `
	            <div class="col">
	                <div class="card h-100" data-device-id="${device.deviceId}">
	                    <div class="card-header fw-bold">${device.label}</div>
	                    <div class="card-body">
	                        <h6 class="card-subtitle mb-2 text-muted">현재 상태</h6>
	                        <p class="card-text d-flex justify-content-around">
	                            <span>전원: <span class="badge bg-${power === 'on' ? 'success' : 'secondary'}">${power}</span></span>
	                            <span>모드: <span class="badge bg-info">${mode}</span></span>
	                            <span>현재온도: <span class="badge bg-primary">${currentTemp}</span></span>
	                            <span>목표온도: <span class="badge bg-danger">${targetTemp}</span></span>
	                        </p>
	                        <hr>
	                        <h6 class="card-subtitle mb-2 text-muted">제어</h6>
	                        <div class="row g-2 align-items-center">
	                            <div class="col-4">
	                                <div class="form-check form-switch form-check-lg">
	                                    <input class="form-check-input control-power" type="checkbox" ${isChecked}>
	                                    <label class="form-check-label small">전원</label>
	                                </div>
	                            </div>
	                            <div class="col-4">
	                                <select class="form-select form-select-sm control-mode">
	                                    <option value="cool" ${selectedMode === 'cool' ? 'selected' : ''}>냉방</option>
	                                    <option value="wind" ${selectedMode === 'wind' ? 'selected' : ''}>청정</option>
	                                    <option value="aIComfort" ${selectedMode === 'aIComfort' ? 'selected' : ''}>AI모드</option>
	                                </select>
	                            </div>
	                            <div class="col-4">
	                                <select class="form-select form-select-sm control-temp">${tempOptions}</select>
	                            </div>
	                        </div>
	                    </div>
	                    <div class="card-footer text-end">
	                        <button class="btn btn-primary btn-sm apply-btn">적용</button>
	                    </div>
	                </div>
	            </div>
	        `;
	    }

	    // '적용' 버튼 클릭 이벤트 처리 (이벤트 위임)
	    container.addEventListener('click', function(event) {
	        if (!event.target.classList.contains('apply-btn')) {
	            return; // '적용' 버튼이 아니면 무시
	        }

	        const card = event.target.closest('.card');
	        const deviceId = card.dataset.deviceId;

	        // 카드 내의 제어 값들 읽어오기
	        const powerState = card.querySelector('.control-power').checked ? 'on' : 'off';
	        const mode = card.querySelector('.control-mode').value;
	        const temperature = parseInt(card.querySelector('.control-temp').value, 10);
	        
	        const controlData = { deviceId, powerState, mode, temperature };

	        // 버튼을 '적용 중...'으로 바꾸고 비활성화
	        event.target.disabled = true;
	        event.target.textContent = '적용 중...';

	        // 서버에 제어 명령 보내기
	        fetch('/api/smartthings/control', {
	            method: 'POST',
	            headers: { 'Content-Type': 'application/json' },
	            body: JSON.stringify(controlData)
	        })
	        .then(response => {
	            if (!response.ok) throw new Error('명령 전송에 실패했습니다.');
	            return response.text();
	        })
	        .then(result => {
	            console.log(result);
	            // 2초 후 목록을 새로고침하여 실제 변경된 상태를 반영
	            setTimeout(initializeControlPage, 2000);
	        })
	        .catch(error => {
	            console.error('Control command failed:', error);
	            alert(error.message);
	            // 실패 시 버튼 원상복구
	            event.target.disabled = false;
	            event.target.textContent = '적용';
	        });
	    });

	    // 페이지 로드 시 초기화 함수 실행
	    initializeControlPage();
	}
	
	if (document.getElementById('work-log-list')) {
	    const logList = document.getElementById('work-log-list');
	    const contentInput = document.getElementById('new-log-content');
	    const statusInput = document.getElementById('new-log-status');
	    const addButton = document.getElementById('add-log-btn');

	    // 작업 로그 상태에 따른 뱃지 색상
	    const statusBadges = {
	        "완료": "bg-success",
	        "진행중": "bg-primary",
	        "예정": "bg-warning text-dark"
	    };

	    // 작업 로그 불러오기 함수
	    function loadWorkLogs() {
	        fetch('/api/worklogs')
	            .then(response => response.json())
	            .then(logs => {
	                logList.innerHTML = ''; // 기존 목록 비우기
	                logs.forEach(log => {
	                    const badgeColor = statusBadges[log.status] || 'bg-secondary';
	                    const logHtml = `
	                        <div class="d-flex justify-content-between align-items-center p-2 border-bottom">
	                            <span>${log.content}</span>
	                            <span class="badge ${badgeColor}">${log.status}</span>
	                        </div>
	                    `;
	                    logList.insertAdjacentHTML('beforeend', logHtml);
	                });
	            });
	    }

	    // 새 작업 로그 추가 함수
	    function addWorkLog() {
	        const content = contentInput.value.trim();
	        const status = statusInput.value;

	        if (!content) {
	            alert('작업 내용을 입력하세요.');
	            return;
	        }

	        fetch('/api/worklogs', {
	            method: 'POST',
	            headers: { 'Content-Type': 'application/json' },
	            body: JSON.stringify({ content, status })
	        })
	        .then(response => response.json())
	        .then(() => {
	            contentInput.value = ''; // 입력창 비우기
	            loadWorkLogs(); // 목록 새로고침
	        })
	        .catch(error => console.error("Error adding log:", error));
	    }

	    addButton.addEventListener('click', addWorkLog);
	    
	    // Enter 키로도 추가 가능하도록
	    contentInput.addEventListener('keypress', function(event) {
	        if (event.key === 'Enter') {
	            addWorkLog();
	        }
	    });

	    // 페이지 로드 시 최초로 로그 불러오기
	    loadWorkLogs();
	}
});