/**
 * 전역 상태 객체
 */
const bookingState = {
    adult: 0,
    youth: 0,
    selectedSeats: [], // {dbId, displayId, price}
    timer: 600 
};

// 1. 좌석 그리드 생성
function initSeatMap() {
    const grid = document.getElementById('seat-map-grid');
    if (!grid) return;

    grid.innerHTML = ''; // 기존 내용 초기화

    // [에러 해결] rows 변수가 정의되지 않아 발생한 문제를 해결하기 위해 행 배열을 선언합니다.
    const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'];
    const reservedMock = ['A3', 'A4', 'B10', 'E7', 'H7', 'H8'];

    // 1-1. 서버에서 넘어온 seatListFromDb를 행(Row)별로 그룹화 (있는 경우만)
    const seatMap = {};
    if (typeof seatListFromDb !== 'undefined' && seatListFromDb !== null) {
        seatListFromDb.forEach(seat => {
            if (!seatMap[seat.seatRow]) {
                seatMap[seat.seatRow] = [];
            }
            seatMap[seat.seatRow].push(seat);
        });

        // 실제 데이터를 기반으로 그리기
        rows.forEach(rowLabel => {
            if (seatMap[rowLabel]) {
                const rowDiv = document.createElement('div');
                rowDiv.className = "flex justify-center items-center mb-1";

                const label = document.createElement('div');
                label.className = "w-8 text-[10px] text-gray-600 font-bold";
                label.innerText = rowLabel;
                rowDiv.appendChild(label);

                seatMap[rowLabel].sort((a, b) => a.seatCol - b.seatCol).forEach(seat => {
                    renderSeat(seat.seatId, `${seat.seatRow}${seat.seatCol}`, seat.basePrice, seat.grade === 'VIP', rowDiv, reservedMock);
                });
                grid.appendChild(rowDiv);
            }
        });
    } else {
        // [임시/방어용] 데이터가 없을 경우 가짜 데이터로 생성
        console.warn("DB 좌석 데이터를 찾을 수 없어 임시 데이터를 생성합니다.");
        let mockDbId = 1;
        rows.forEach((row, rowIndex) => {
            const rowDiv = document.createElement('div');
            rowDiv.className = "flex justify-center items-center mb-1";
            // ... (기본 행 레이블 생성 로직 생략)
            for (let col = 1; col <= 15; col++) {
                const displayId = `${row}${col}`;
                const isVip = rowIndex >= 6 && col >= 5 && col <= 11;
                renderSeat(mockDbId++, displayId, isVip ? 18000 : 12000, isVip, rowDiv, reservedMock);
            }
            grid.appendChild(rowDiv);
        });
    }
    startTimer();
}

// 좌석 개별 렌더링 함수
function renderSeat(dbId, displayId, price, isVip, parent, reservedList) {
    const isReserved = reservedList.includes(displayId);
    const seat = document.createElement('div');
    seat.id = `seat-${displayId}`;
    seat.setAttribute('data-db-id', dbId);
    seat.className = `seat ${isReserved ? 'reserved' : (isVip ? 'vip' : 'available')}`;
    seat.innerText = isReserved ? 'X' : displayId.replace(/[^0-9]/g, '');

    if (!isReserved) {
        seat.onclick = () => toggleSeat(dbId, displayId, price, seat);
    }
    parent.appendChild(seat);
}

// 2. 좌석 토글
function toggleSeat(dbId, displayId, price, element) {
    const totalCount = bookingState.adult + bookingState.youth;
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
        bookingState.selectedSeats.push({ dbId, displayId, price });
        element.classList.add('selected');
    }
    updateSummary();
}

// 3. 인원 수 변경
function updateCount(type, delta) {
    bookingState[type] = Math.max(0, bookingState[type] + delta);
    document.getElementById(`${type}-count`).innerText = bookingState[type];
    
    const total = bookingState.adult + bookingState.youth;
    while(bookingState.selectedSeats.length > total) {
        const removed = bookingState.selectedSeats.pop();
        const el = document.getElementById(`seat-${removed.displayId}`);
        if(el) el.classList.remove('selected');
    }
    updateSummary();
}

// 4. 하단 요약 바 업데이트
function updateSummary() {
    const seatNames = bookingState.selectedSeats.map(s => s.displayId).sort().join(', ');
    const total = bookingState.selectedSeats.reduce((sum, s) => sum + s.price, 0);

    document.getElementById('display-seat-names').innerText = seatNames || '선택 안 됨';
    document.getElementById('display-total-price').innerText = `₩${total.toLocaleString()}`;

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

// 5. 점유 타이머 (ID 수정 및 로직 최적화)
function startTimer() {
    // HTML의 id="timer"와 일치하도록 수정했습니다.
    const timerEl = document.getElementById('timer'); 
    if(!timerEl) {
        console.error("타이머 요소를 찾을 수 없습니다 (id='timer' 확인 필요)");
        return;
    }

    const interval = setInterval(() => {
        const min = String(Math.floor(bookingState.timer / 60)).padStart(2, '0');
        const sec = String(bookingState.timer % 60).padStart(2, '0');
        
        // HTML 구조에 맞게 시간만 업데이트합니다.
        timerEl.innerText = `${min}:${sec}`;
        
        if (bookingState.timer <= 0) {
            clearInterval(interval);
            alert("점유 시간이 만료되었습니다. 다시 선택해주세요.");
            location.reload();
        }
        bookingState.timer--;
    }, 1000);
}

// 6. 예매 제출
function handleBookingSubmit() {
    const seatIds = bookingState.selectedSeats.map(s => s.dbId);
    const displayNames = bookingState.selectedSeats.map(s => s.displayId);
    const totalPriceValue = bookingState.selectedSeats.reduce((sum, s) => sum + s.price, 0);

    if (seatIds.length === 0) {
        alert("좌석을 선택해 주세요.");
        return;
    }

    if (confirm(`${displayNames.join(', ')} 좌석으로 예매를 진행하시겠습니까?`)) {
        const seatIdsInput = document.getElementById('input-seat-ids');
        const totalPriceInput = document.getElementById('input-total-price');
        const bookingForm = document.getElementById('bookingForm');

        if (seatIdsInput && totalPriceInput && bookingForm) {
            seatIdsInput.value = seatIds.join(','); 
            totalPriceInput.value = totalPriceValue;
            bookingForm.submit();
        }
    }
}

document.addEventListener('DOMContentLoaded', initSeatMap);