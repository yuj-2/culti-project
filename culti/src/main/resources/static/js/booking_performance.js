/**
 * 공연 예매 전용 상태 관리 객체
 */
const bookingState = {
    adult: 0,
    selectedSeats: [], // {id, price} 객체 저장
    currentFloor: 1,   // 현재 선택된 층 (1F / 2F)
    timer: 600         // 10분 제한 시간
};

/**
 * 1. 층 전환 함수 (1층/2층 탭 클릭 시 호출)
 * 지호 님이 요청하신 '버튼으로 층 나누기'의 핵심 로직입니다.
 */
function switchFloor(floorNum) {
    bookingState.currentFloor = floorNum;
    
    // 버튼 UI 활성화 스타일 제어
    document.querySelectorAll('.floor-btn').forEach(btn => {
        btn.classList.remove('active', 'bg-[#503396]', 'text-white');
        btn.classList.add('text-gray-500');
    });
    
    const activeBtn = document.getElementById(`btn-${floorNum}f`);
    if (activeBtn) {
        activeBtn.classList.add('active', 'bg-[#503396]', 'text-white');
        activeBtn.classList.remove('text-gray-500');
    }

    // 좌석도 상단 라벨 변경
    const label = document.getElementById('current-floor-label');
    if (label) label.innerText = `${floorNum}F (${floorNum}층)`;

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

    // 기존에 그려진 좌석들 삭제 (층 전환 시 필수)
    Object.values(blocks).forEach(b => { if(b) b.innerHTML = ""; });

    if (bookingState.currentFloor === 1) {
        // 1층 배치: 기준 구역 분할
        renderBlock('left', ['A','B','C','D','E','F'], 1, 6);
        renderBlock('center', ['A','B','C','D','E','F','G','H','I','J'], 7, 15);
        renderBlock('right', ['A','B','C','D','E','F'], 16, 21);
    } else {
        // 2층 배치: 기준 K열부터 시작
        renderBlock('left', ['K','L','M'], 1, 6);
        renderBlock('center', ['K','L','M','N','O'], 7, 15);
        renderBlock('right', ['K','L','M'], 16, 21);
    }
}

/**
 * 3. 구역(Block)별 좌석 생성 함수
 */
function renderBlock(blockId, rows, startCol, endCol) {
    const target = document.getElementById(`block-${blockId}`);
    if (!target) return; // 깨짐 방지 안전 장치

    rows.forEach(row => {
        const rowWrapper = document.createElement('div');
        rowWrapper.className = "row-wrapper";

        // 중앙 블록에만 알파벳 행 레이블 표시
        if (blockId === 'center') {
            const label = document.createElement('div');
            label.className = "row-label";
            label.innerText = row;
            rowWrapper.appendChild(label);
        }

        for (let col = startCol; col <= endCol; col++) {
            const seatId = `${bookingState.currentFloor}F-${row}${col}`;
            
            // 등급 및 가격 판정 로직
            let gradeInfo = { cls: 'a-seat', price: 60000 };
            if (bookingState.currentFloor === 1) {
                if (row <= 'C') gradeInfo = { cls: 'vip', price: 150000 };
                else if (row <= 'F') gradeInfo = { cls: 'r-seat', price: 120000 };
                else gradeInfo = { cls: 's-seat', price: 90000 };
            } else {
                if (row <= 'L') gradeInfo = { cls: 's-seat', price: 90000 };
                else gradeInfo = { cls: 'a-seat', price: 60000 };
            }

            const seat = document.createElement('div');
            seat.id = `seat-${seatId}`;
            seat.className = `seat ${gradeInfo.cls}`;
            
            // 다른 층을 갔다가 돌아와도 선택했던 좌석은 'selected' 유지
            if (bookingState.selectedSeats.find(s => s.id === seatId)) {
                seat.classList.add('selected');
            }

            seat.innerText = col;
            seat.onclick = () => toggleSeat(seatId, gradeInfo.price, seat);
            rowWrapper.appendChild(seat);
        }
        target.appendChild(rowWrapper);
    });
}

/**
 * 4. 좌석 선택 토글 로직
 */
function toggleSeat(id, price, element) {
    const index = bookingState.selectedSeats.findIndex(s => s.id === id);

    if (index > -1) {
        bookingState.selectedSeats.splice(index, 1);
        element.classList.remove('selected');
    } else {
        if (bookingState.adult === 0) {
            alert("상단에서 관람 인원을 먼저 선택해주세요.");
            return;
        }
        if (bookingState.selectedSeats.length >= bookingState.adult) {
            alert(`이미 ${bookingState.adult}석을 모두 선택하셨습니다.`);
            return;
        }
        bookingState.selectedSeats.push({ id, price });
        element.classList.add('selected');
    }
    updateSummary();
}

/**
 * 5. 인원수 변경
 */
function updateCount(type, delta) {
    bookingState.adult = Math.max(0, bookingState.adult + delta);
    document.getElementById('adult-count').innerText = bookingState.adult;
    
    // 인원을 줄였을 때 선택된 좌석이 더 많으면 자동 해제
    while(bookingState.selectedSeats.length > bookingState.adult) {
        const removed = bookingState.selectedSeats.pop();
        const el = document.getElementById(`seat-${removed.id}`);
        if(el) el.classList.remove('selected');
    }
    updateSummary();
}

/**
 * 6. 하단 요약 업데이트
 */
function updateSummary() {
    const seatNames = bookingState.selectedSeats.map(s => s.id).sort().join(', ');
    const total = bookingState.selectedSeats.reduce((sum, s) => sum + s.price, 0);

    document.getElementById('display-seat-names').innerText = seatNames || '선택 안 됨';
    document.getElementById('display-total-price').innerText = `₩${total.toLocaleString()}`;

    const btn = document.getElementById('submit-booking-btn');
    if (bookingState.adult > 0 && bookingState.selectedSeats.length === bookingState.adult) {
        btn.disabled = false;
        btn.classList.replace('bg-gray-800', 'bg-[#503396]');
        btn.classList.replace('cursor-not-allowed', 'cursor-pointer');
    } else {
        btn.disabled = true;
        btn.classList.replace('bg-[#503396]', 'bg-gray-800');
        btn.classList.replace('cursor-pointer', 'cursor-not-allowed');
    }
}

/**
 * 7. 타이머
 */
function startTimer() {
    const timerEl = document.getElementById('hold-timer');
    const interval = setInterval(() => {
        const min = String(Math.floor(bookingState.timer / 60)).padStart(2, '0');
        const sec = String(bookingState.timer % 60).padStart(2, '0');
        timerEl.innerText = `임시 점유 중 ${min}:${sec}`;
        if (bookingState.timer-- <= 0) {
            clearInterval(interval);
            alert("시간 만료로 새로고침합니다.");
            location.reload();
        }
    }, 1000);
}

window.onload = () => {
    initSeatMap();
    startTimer();
};

/**
 * 8. 결제하기 버튼 클릭 시 서버로 데이터 전송
 */
function handleBookingSubmit() {
    const totalCount = bookingState.adult;
    const selectedCount = bookingState.selectedSeats.length;

    // 최종 확인
    if (selectedCount !== totalCount) {
        alert(`선택하신 좌석(${selectedCount}석)이 설정한 인원(${totalCount}명)과 일치하지 않습니다.`);
        return;
    }

    if (confirm("선택하신 좌석으로 결제를 진행하시겠습니까?")) {
        // 서버로 보낼 임시 폼 생성
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/reservation/booking/create';

        // 1. 스케줄 ID (HTML에서 scheduleId 값을 가져와야 합니다)
        const scheduleIdInput = document.createElement('input');
        scheduleIdInput.type = 'hidden';
        scheduleIdInput.name = 'scheduleId';
        scheduleIdInput.value = new URLSearchParams(window.location.search).get('scheduleId');
        form.appendChild(scheduleIdInput);

        // 2. 선택한 좌석 ID 리스트 (쉼표로 구분)
        const seatIdsInput = document.createElement('input');
        seatIdsInput.type = 'hidden';
        seatIdsInput.name = 'seatIds';
        seatIdsInput.value = bookingState.selectedSeats.map(s => s.id).join(',');
        form.appendChild(seatIdsInput);

        // 3. 총 결제 금액
        const totalPriceInput = document.createElement('input');
        totalPriceInput.type = 'hidden';
        totalPriceInput.name = 'totalPrice';
        totalPriceInput.value = bookingState.selectedSeats.reduce((sum, s) => sum + s.price, 0);
        form.appendChild(totalPriceInput);

        // CSRF 토큰 추가 (스프링 시큐리티 사용 시 필수)
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        if (csrfToken) {
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = '_csrf';
            csrfInput.value = csrfToken;
            form.appendChild(csrfInput);
        }

        document.body.appendChild(form);
        form.submit(); // 컨트롤러의 createBooking 메서드로 전송
    }
}