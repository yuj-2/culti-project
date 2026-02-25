/**
 * 공연 예매 전용 상태 관리 객체
 */
const bookingState = {
    adult: 0,
    youth: 0,
    selectedSeats: [], // {id, price} 객체 저장
    currentFloor: 1,   // 현재 선택된 층 (1F / 2F)
    timer: 600         // 10분 제한 시간
};

/**
 * 1. 층 전환 함수 (1층/2층 탭 클릭 시 호출)
 */
function switchFloor(floorNum) {
    bookingState.currentFloor = floorNum;
    
    // 버튼 UI 활성화 스타일 제어
    document.querySelectorAll('.floor-btn').forEach(btn => {
        btn.classList.remove('active', 'bg-[#503396]', 'text-white');
        btn.classList.add('text-gray-500');
    });
    
    const activeBtn = document.getElementById(`btn-${floorNum}f`);
    activeBtn.classList.add('active', 'bg-[#503396]', 'text-white');
    activeBtn.classList.remove('text-gray-500');

    // 해당 층의 좌석도를 구역별로 다시 그리기
    initSeatMap();
}

/**
 * 2. 좌석 배치도 초기화 및 렌더링
 */
function initSeatMap() {
    const blocks = {
        left: document.getElementById('block-left'),
        center: document.getElementById('block-center'),
        right: document.getElementById('block-right')
    };

    // 기존에 그려진 좌석들 삭제
    Object.values(blocks).forEach(b => b.innerHTML = "");

    if (bookingState.currentFloor === 1) {
        // 1층 배치: 사진처럼 좌측(3-6번), 중앙(7-15번), 우측(16-19번)으로 분할
        renderBlock('left', ['A','B','C','D','E','F'], 3, 6);
        renderBlock('center', ['A','B','C','D','E','F','G','H','I','J'], 7, 15);
        renderBlock('right', ['A','B','C','D','E','F'], 16, 19);
    } else {
        // 2층 배치: 중앙 위주로 넓게 구성 (K열부터 시작)
        renderBlock('center', ['K','L','M','N','O'], 1, 15);
    }
}

/**
 * 3. 구역(Block)별 좌석 생성 함수
 */
function renderBlock(blockId, rows, startCol, endCol) {
    const target = document.getElementById(`block-${blockId}`);
    // 예약 완료된 좌석 (DB 연동 전 가짜 데이터)
    const reservedMock = ['1F-A3', '1F-B10', '1F-H7', '1F-H8']; 

    rows.forEach(row => {
        const rowWrapper = document.createElement('div');
        rowWrapper.className = "row-wrapper";

        // 중앙 블록에만 알파벳(A, B, C...) 행 레이블 표시
        if (blockId === 'center') {
            const label = document.createElement('div');
            label.className = "row-label";
            label.innerText = row;
            rowWrapper.appendChild(label);
        }

        for (let col = startCol; col <= endCol; col++) {
            const seatId = `${bookingState.currentFloor}F-${row}${col}`;
            // VIP 등급 조건 (예: 1층 중앙 G-J열 특정 범위)
            const isVip = (bookingState.currentFloor === 1 && row >= 'G' && row <= 'J') && (col >= 7 && col <= 13);
            const isReserved = reservedMock.includes(seatId);
            const price = isVip ? 150000 : 110000;

            const seat = document.createElement('div');
            seat.id = `seat-${seatId}`;
            seat.className = `seat ${isReserved ? 'reserved' : (isVip ? 'vip' : 'available')}`;
            
            // 다른 층을 갔다가 돌아와도 내가 선택했던 좌석은 'selected' 유지
            if (bookingState.selectedSeats.find(s => s.id === seatId)) {
                seat.classList.add('selected');
            }

            seat.innerText = isReserved ? 'X' : col;

            if (!isReserved) {
                seat.onclick = () => toggleSeat(seatId, price, seat);
            }
            rowWrapper.appendChild(seat);
        }
        target.appendChild(rowWrapper);
    });
}

/**
 * 4. 좌석 선택 토글 로직
 */
function toggleSeat(id, price, element) {
    const totalCount = bookingState.adult + bookingState.youth;
    const index = bookingState.selectedSeats.findIndex(s => s.id === id);

    if (index > -1) {
        // 이미 선택된 좌석이면 해제
        bookingState.selectedSeats.splice(index, 1);
        element.classList.remove('selected');
    } else {
        // 인원수 미선택 체크
        if (totalCount === 0) {
            alert("상단에서 관람 인원을 먼저 선택해주세요.");
            return;
        }
        // 최대 선택 가능 수 체크
        if (bookingState.selectedSeats.length >= totalCount) {
            alert(`이미 ${totalCount}석을 모두 선택하셨습니다.`);
            return;
        }
        // 좌석 선택 추가
        bookingState.selectedSeats.push({ id, price });
        element.classList.add('selected');
    }
    updateSummary();
}

/**
 * 5. 인원수 변경 (성인/청소년 +/-)
 */
function updateCount(type, delta) {
    bookingState[type] = Math.max(0, bookingState[type] + delta);
    document.getElementById(`${type}-count`).innerText = bookingState[type];
    
    // 인원수를 줄여서 현재 선택된 좌석보다 적어지면, 마지막 좌석부터 자동 해제
    const total = bookingState.adult + bookingState.youth;
    while(bookingState.selectedSeats.length > total) {
        const removed = bookingState.selectedSeats.pop();
        const el = document.getElementById(`seat-${removed.id}`);
        if(el) el.classList.remove('selected');
    }
    updateSummary();
}

/**
 * 6. 하단 요약 바 업데이트 (실시간 금액 및 좌석명)
 */
function updateSummary() {
    const seatNames = bookingState.selectedSeats.map(s => s.id).sort().join(', ');
    const total = bookingState.selectedSeats.reduce((sum, s) => sum + s.price, 0);

    document.getElementById('display-seat-names').innerText = seatNames || '선택 안 됨';
    document.getElementById('display-total-price').innerText = `₩${total.toLocaleString()}`;

    // 결제하기 버튼 활성화 제어
    const btn = document.getElementById('submit-booking-btn');
    const totalNeeded = bookingState.adult + bookingState.youth;
    
    if (totalNeeded > 0 && bookingState.selectedSeats.length === totalNeeded) {
        btn.disabled = false;
        btn.classList.replace('bg-gray-300', 'bg-[#503396]');
        btn.classList.replace('cursor-not-allowed', 'cursor-pointer');
    } else {
        btn.disabled = true;
        btn.classList.replace('bg-[#503396]', 'bg-gray-300');
        btn.classList.replace('cursor-pointer', 'cursor-not-allowed');
    }
}

/**
 * 7. 10분 점유 타이머
 */
function startTimer() {
    const timerEl = document.getElementById('hold-timer');
    const interval = setInterval(() => {
        const min = String(Math.floor(bookingState.timer / 60)).padStart(2, '0');
        const sec = String(bookingState.timer % 60).padStart(2, '0');
        timerEl.innerText = `좌석 임시 점유 중 ${min}:${sec}`;
        
        if (bookingState.timer <= 0) {
            clearInterval(interval);
            alert("점유 시간이 만료되어 페이지를 새로고침합니다.");
            location.reload();
        }
        bookingState.timer--;
    }, 1000);
}

/**
 * 8. 결제하기 버튼 클릭 시 데이터 확인
 */
function handleBookingSubmit() {
    if(confirm("선택하신 좌석으로 예매를 진행하시겠습니까?")) {
        console.log("예매 정보:", bookingState.selectedSeats);
        // 여기서 결제 API 호출 또는 서버 전송 로직 수행
    }
}

// 초기 페이지 로드 시 실행
window.onload = () => {
    initSeatMap();
    startTimer();
};