// '에어컨 목록 불러오기' 버튼에 이벤트 리스너 추가
document.getElementById('load-ac-button').addEventListener('click', function() {
    const tableBody = document.getElementById('devices-table-body');
    tableBody.innerHTML = '<tr><td colspan="4" class="text-center">로딩 중...</td></tr>';

    // 백엔드 API를 호출하여 에어컨 목록을 가져옵니다.
    fetch('/api/smartthings/getAirconDevices')
        .then(response => {
            if (!response.ok) {
                throw new Error('서버에서 데이터를 가져오는 데 실패했습니다.');
            }
            return response.json();
        })
        .then(devices => {
            tableBody.innerHTML = ''; // 기존 '로딩 중...' 메시지 삭제

            if (devices.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="4" class="text-center">등록된 에어컨이 없습니다.</td></tr>';
                return;
            }

            // 받아온 데이터로 테이블의 각 행(row)을 만듭니다.
            devices.forEach(device => {
                const row = `
                    <tr>
                        <td>${device.label}</td>
                        <td>${device.deviceId}</td>
                        <td>${device.deviceTypeName}</td>
                        <td>
                            <button class="btn btn-success btn-sm">등록하기</button>
                        </td>
                    </tr>
                `;
                tableBody.insertAdjacentHTML('beforeend', row);
            });
        })
        .catch(error => {
            console.error('Error:', error);
            tableBody.innerHTML = `<tr><td colspan="4" class="text-center text-danger">오류 발생: ${error.message}</td></tr>`;
        });
});