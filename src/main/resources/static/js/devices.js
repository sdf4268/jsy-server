document.addEventListener('DOMContentLoaded', function() {
    const loadButton = document.getElementById('load-ac-button');
    const tableBody = document.getElementById('devices-table-body');
    
    // Modal 인스턴스 생성
    const registrationModalEl = document.getElementById('registrationModal');
    const registrationModal = new bootstrap.Modal(registrationModalEl);
    const deleteConfirmModalEl = document.getElementById('deleteConfirmModal');
    const deleteConfirmModal = new bootstrap.Modal(deleteConfirmModalEl);

    // --- 이벤트 리스너 ---
    loadButton.addEventListener('click', loadAirConditioners);
    document.getElementById('save-device-button').addEventListener('click', saveDevice);
    document.getElementById('confirm-delete-button').addEventListener('click', confirmDelete);
    tableBody.addEventListener('click', handleTableButtonClick);

    // --- 함수 정의 ---

    // 1. 에어컨 목록 불러오기 함수 (변경 없음)
    function loadAirConditioners() {
        tableBody.innerHTML = '<tr><td colspan="4" class="text-center">로딩 중...</td></tr>';
        fetch('/api/smartthings/getAirconDevices')
            .then(response => response.json())
            .then(devices => {
                renderTable(devices);
            })
            .catch(error => {
                console.error('Error:', error);
                tableBody.innerHTML = `<tr><td colspan="4" class="text-center text-danger">오류 발생: ${error.message}</td></tr>`;
            });
    }

    // 2. 테이블 렌더링 함수 (변경 없음)
    function renderTable(devices) {
        tableBody.innerHTML = '';
        if (devices.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="4" class="text-center">등록된 에어컨이 없습니다.</td></tr>';
            return;
        }

        devices.forEach(device => {
			const buttonHtml = device.registered 
			            ? `<span class="badge bg-success me-2">등록됨</span>
			               <button class="btn btn-danger btn-sm delete-btn" data-device-id="${device.deviceId}">삭제</button>`
			            : `<button class="btn btn-success btn-sm register-btn" 
			                        data-device-id="${device.deviceId}" 
			                        data-label="${device.label}">등록하기</button>`;
            
            const row = `
                <tr data-row-id="${device.deviceId}">
                    <td>${device.label}</td>
                    <td>${device.deviceId}</td>
                    <td>${device.deviceTypeName}</td>
                    <td class="text-center">${buttonHtml}</td>
                </tr>
            `;
            tableBody.insertAdjacentHTML('beforeend', row);
        });
    }

    // 3. 테이블 버튼 클릭 처리 (수정됨)
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
	        const deviceId = button.dataset.deviceId;
            // 삭제할 ID를 모달에 임시 저장
            deleteConfirmModalEl.dataset.deviceId = deviceId;
	        deleteConfirmModal.show();
	    }
	}

    // 4. (신규) 삭제 모달의 '삭제하기' 버튼 클릭 시 최종 실행
    function confirmDelete() {
        const deviceId = deleteConfirmModalEl.dataset.deviceId;
        deleteDevice(deviceId);
    }

    // 5. DB에 기기 저장 함수 (수정됨)
    function saveDevice() {
        const deviceId = document.getElementById('modal-device-id').value;
        const label = document.getElementById('modal-device-label').value;
        const room = document.getElementById('room-select').value;
        
        if (room === "공간을 선택하세요...") {
            alert("설치된 공간을 선택해주세요."); // 간단한 유효성 검사는 alert 유지
            return;
        }

        const registrationData = { deviceId, label, room: parseInt(room) };

        fetch('/api/smartthings/register-device', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(registrationData)
        })
        .then(response => {
            if (!response.ok) { throw new Error('등록에 실패했습니다.'); }
            return response.text();
        })
        .then(result => {
            console.log(result);
            registrationModal.hide();
            // ✨ 성공 후, 목록을 새로고침하여 UI를 갱신합니다.
            loadAirConditioners(); 
        })
        .catch(error => { alert(error.message); });
    }
	
	// 6. DB에서 기기 삭제 함수 (수정됨)
	function deleteDevice(deviceId) {
	    fetch(`/api/smartthings/delete/${deviceId}`, {
	        method: 'DELETE'
	    })
	    .then(response => {
	        if (!response.ok) { throw new Error('기기 삭제에 실패했습니다.'); }
	        return response.text();
	    })
	    .then(result => {
	        console.log(result);
	        deleteConfirmModal.hide();
            // ✨ 성공 후, 목록을 새로고침하여 UI를 갱신합니다.
	        loadAirConditioners(); 
	    })
	    .catch(error => {
            deleteConfirmModal.hide(); // 에러 발생 시에도 모달은 닫아줍니다.
            alert(error.message);
        });
	}
});