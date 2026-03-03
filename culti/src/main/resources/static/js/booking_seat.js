/**
 * [수정] 인원별 티켓 단가 설정
 */
const TICKET_PRICES = {
    adult: 15000,    // 성인
    youth: 12000,    // 청소년
    senior: 10000,   // 경로
    special: 8000    // 우대
};

/**
 * 전역 상태 객체
 */
const bookingState = {
    adult: 0,
    youth: 0,
    special: 0,
    senior: 0,
    selectedSeats: [], // {dbId, displayId}
    timer: 600 
};

/**
 * 총 선택 가능한 인원 합계 계산
 */
function getTotalNeeded() {
    return bookingState.adult + bookingState.youth + bookingState.special + bookingState.senior;
}

// 1. 좌석 그리드 생성
function initSeatMap() {
    const grid = document.getElementById('seat-map-grid');
    if (!grid) return;

    grid.innerHTML = ''; 
    const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'];
    const reservedMock = ['A3', 'A4', 'B10', 'E7', 'H7', 'H8'];

    const seatMap = {};
    if (typeof seatListFromDb !== 'undefined' && seatListFromDb !== null && seatListFromDb.length > 0) {
        seatListFromDb.forEach(seat => {
            if (!seatMap[seat.seatRow]) { seatMap[seat.seatRow] = []; }
            seatMap[seat.seatRow].push(seat);
        });

        rows.forEach(rowLabel => {
            if (seatMap[rowLabel]) {
                const rowDiv = document.createElement('div');
                rowDiv.className = "flex justify-center items-center mb-1";
                const label = document.createElement('div');
                label.className = "w-8 text-[10px] text-gray-600 font-bold";
                label.innerText = rowLabel;
                rowDiv.appendChild(label);

                seatMap[rowLabel].sort((a, b) => a.seatCol - b.seatCol).forEach(seat => {
                    renderSeat(seat.seatId, `${seat.seatRow}${seat.seatCol}`, 0, false, rowDiv, reservedMock);
                });
                grid.appendChild(rowDiv);
            }
        });
    } else {
        rows.forEach((row, rowIndex) => {
            const rowDiv = document.createElement('div');
            rowDiv.className = "flex justify-center items-center mb-1";
            for (let col = 1; col <= 15; col++) {
                const displayId = `${row}${col}`;
                renderSeat(rowIndex * 15 + col, displayId, 0, false, rowDiv, reservedMock);
            }
            grid.appendChild(rowDiv);
        });
    }
    startTimer();
}

function renderSeat(dbId, displayId, price, isVip, parent, reservedList) {
    const isReserved = reservedList.includes(displayId);
    const seat = document.createElement('div');
    seat.id = `seat-${displayId}`;
    seat.setAttribute('data-db-id', dbId);
    seat.className = `seat ${isReserved ? 'reserved' : 'available'}`;
    seat.innerText = isReserved ? 'X' : displayId.replace(/[^0-9]/g, '');

    if (!isReserved) {
        seat.onclick = () => toggleSeat(dbId, displayId, 0, seat);
    }
    parent.appendChild(seat);
}

// 2. 좌석 토글
function toggleSeat(dbId, displayId, price, element) {
    const totalCount = getTotalNeeded();
    const index = bookingState.selectedSeats.findIndex(s => s.dbId === dbId);

    if (index > -1) {
        bookingState.selectedSeats.splice(index, 1);
        element.classList.remove('selected');
    } else {
        if (totalCount === 0) {
            alert("인원을 먼저 선택해주세요.");
            return;
        }
        if (bookingState.selectedSeats.length >= totalCount) {
            alert(`최대 ${totalCount}석까지만 선택 가능합니다.`);
            return;
        }
        bookingState.selectedSeats.push({ dbId, displayId });
        element.classList.add('selected');
    }
    updateSummary();
}

// 3. 인원 수 변경
function updateCount(type, delta) {
    bookingState[type] = Math.max(0, bookingState[type] + delta);
    document.getElementById(`${type}-count`).innerText = bookingState[type];
    
    const total = getTotalNeeded();
    while(bookingState.selectedSeats.length > total) {
        const removed = bookingState.selectedSeats.pop();
        const el = document.getElementById(`seat-${removed.displayId}`);
        if(el) el.classList.remove('selected');
    }
    updateSummary();
}

// 4. 하단 요약 바 업데이트 (실시간 가격 합산 적용)
function updateSummary() {
    const totalNeeded = getTotalNeeded();
    const currentSelected = bookingState.selectedSeats.length;
    const priceDisplayEl = document.getElementById('display-total-price');
    const seatDisplayEl = document.getElementById('display-seat-names');
    
    // 좌석 이름 업데이트
    const seatNames = bookingState.selectedSeats.map(s => s.displayId).sort().join(', ');
    seatDisplayEl.innerText = seatNames || '선택 안 됨';

    // [핵심 수정] 실시간 가격 계산 로직
    let currentTotalAmount = 0;

    if (currentSelected > 0) {
        // 선택한 인원 구성을 가격 배열로 풀기 (높은 가격순 정렬)
        const pricePool = [
            ...Array(bookingState.adult).fill(TICKET_PRICES.adult),
            ...Array(bookingState.youth).fill(TICKET_PRICES.youth),
            ...Array(bookingState.senior).fill(TICKET_PRICES.senior),
            ...Array(bookingState.special).fill(TICKET_PRICES.special)
        ].sort((a, b) => b - a);

        // 현재 선택된 좌석 수만큼 요금 합산
        for (let i = 0; i < currentSelected; i++) {
            if (pricePool[i]) {
                currentTotalAmount += pricePool[i];
            }
        }
        
        // ₩ 기호와 함께 출력
        priceDisplayEl.innerText = `₩${currentTotalAmount.toLocaleString()}`;
    } else {
        // 선택된 좌석이 없으면 가격 미표시
        priceDisplayEl.innerText = ""; 
    }

    // 결제 버튼 및 데이터 업데이트
    if (totalNeeded > 0 && currentSelected === totalNeeded) {
        updateSubmitButton(true);
        const totalPriceInput = document.getElementById('input-total-price');
        if (totalPriceInput) totalPriceInput.value = currentTotalAmount;
    } else {
        updateSubmitButton(false);
    }
}

/**
 * 결제 버튼 상태 업데이트 함수
 */
function updateSubmitButton(active) {
    const btn = document.getElementById('submit-booking-btn');
    if (active) {
        btn.disabled = false;
        btn.classList.replace('bg-gray-800', 'bg-[#503396]');
        btn.classList.replace('cursor-not-allowed', 'cursor-pointer');
    } else {
        btn.disabled = true;
        btn.classList.replace('bg-[#503396]', 'bg-gray-800');
        btn.classList.replace('cursor-pointer', 'cursor-not-allowed');
    }
}

// 5. 점유 타이머
function startTimer() {
    const timerEl = document.getElementById('timer'); 
    if(!timerEl) return;
    const interval = setInterval(() => {
        const min = String(Math.floor(bookingState.timer / 60)).padStart(2, '0');
        const sec = String(bookingState.timer % 60).padStart(2, '0');
        timerEl.innerText = `${min}:${sec}`;
        if (bookingState.timer <= 0) {
            clearInterval(interval);
            alert("점유 시간이 만료되었습니다. 다시 선택해주세요.");
            location.reload();
        }
        bookingState.timer--;
    }, 1000);
}

// 6. 예매 제출 핸들러
function handleBookingSubmit() {
    const selectedSeats = bookingState.selectedSeats;
    const seatIds = selectedSeats.map(s => s.dbId);
    
    // 현재 표시된 금액 가져오기
    const priceText = document.getElementById('display-total-price').innerText;
    const totalPriceValue = parseInt(priceText.replace(/[^0-9]/g, '')) || 0;

    if (seatIds.length === 0) {
        alert("좌석을 선택해 주세요.");
        return;
    }

    const seatIdsInput = document.getElementById('input-seat-ids');
    const totalPriceInput = document.getElementById('input-total-price');
    
    if (seatIdsInput) seatIdsInput.value = seatIds.join(',');
    if (totalPriceInput) totalPriceInput.value = totalPriceValue;

    const paymentData = {
        title: document.querySelector('h3.text-xl')?.innerText || '영화 예매',
        amount: totalPriceValue,
        details: selectedSeats.map(s => s.displayId).sort().join(', ')
    };

    if (typeof openPaymentModal === 'function') {
        openPaymentModal(paymentData);
    } else {
        if (confirm("결제를 진행하시겠습니까?")) {
            document.getElementById('bookingForm').submit();
        }
    }
}

document.addEventListener('DOMContentLoaded', initSeatMap);