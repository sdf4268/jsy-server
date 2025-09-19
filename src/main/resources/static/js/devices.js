// js/devices.js (전체 코드)

document.addEventListener('DOMContentLoaded', function() {
    const loadButton = document.getElementById('load-ac-button');
    const tableBody = document.getElementById('devices-table-body');
    const registrationModalEl = document.getElementById('registrationModal');
    const registrationModal = new bootstrap.Modal(registrationModalEl);

    // --- 이벤트 리스너 ---
    loadButton.addEventListener('click', loadAirConditioners);
    document.getElementById('save-device-button').addEventListener('click', saveDevice);
    tableBody.addEventListener('click', handleTableButtonClick);

    // --- 함수 정의 ---

    // 1. 에어컨 목록 불러오기 함수
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

    // 2. 테이블 렌더링 함수
    function renderTable(devices) {
        tableBody.innerHTML = '';
        if (devices.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="4" class="text-center">등록된 에어컨이 없습니다.</td></tr>';
            return;
        }

        devices.forEach(device => {
            const buttonHtml = device.registered 
                ? `<button class="btn btn-secondary btn-sm" disabled>등록됨</button>`
                : `<button class="btn btn-success btn-sm register-btn" 
                            data-device-id="${device.deviceId}" 
                            data-label="${device.label}">등록하기</button>`;
            
            const row = `
                <tr data-row-id="${device.deviceId}">
                    <td>${device.label}</td>
                    <td>${device.deviceId}</td>
                    <td>${device.deviceTypeName}</td>
                    <td>${buttonHtml}</td>
                </tr>
            `;
            tableBody.insertAdjacentHTML('beforeend', row);
        });
    }

    // 3. '등록하기' 버튼 클릭 처리 함수
    function handleTableButtonClick(event) {
        if (event.target.classList.contains('register-btn')) {
            const button = event.target;
            const deviceId = button.dataset.deviceId;
            const label = button.dataset.label;

            // 모달에 정보 채우기
            document.getElementById('modal-device-id').value = deviceId;
            document.getElementById('modal-device-label').value = label;
            document.getElementById('device-label-display').value = label;
            document.getElementById('room-select').selectedIndex = 0;

            // 모달 띄우기
            registrationModal.show();
        }
    }

    // 4. 모달에서 '저장하기' 버튼 클릭 시 DB에 저장하는 함수
    function saveDevice() {
        const deviceId = document.getElementById('modal-device-id').value;
        const label = document.getElementById('modal-device-label').value;
        const room = document.getElementById('room-select').value;
        const type = 3; // 에어컨 타입

        if (room === "공간을 선택하세요...") {
            alert("설치된 공간을 선택해주세요.");
            return;
        }

        const registrationData = { deviceId, label, room: parseInt(room) };

        fetch('/api/smartthings/register-device', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(registrationData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('등록에 실패했습니다.');
            }
            return response.text();
        })
        .then(result => {
            console.log(result);
            registrationModal.hide();

            // 화면의 버튼 상태를 '등록됨'으로 즉시 업데이트
            const row = tableBody.querySelector(`tr[data-row-id="${deviceId}"]`);
            if (row) {
                row.cells[3].innerHTML = `<button class="btn btn-secondary btn-sm" disabled>등록됨</button>`;
            }
        })
        .catch(error => {
            alert(error.message);
        });
    }
});