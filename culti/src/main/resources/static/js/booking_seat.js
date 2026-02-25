/**
 * 리액트 App의 상태와 동일한 로직을 수행하는 전역 상태 객체
 */
const bookingState = {
    adult: 0,
    youth: 0,
    selectedSeats: [], // {id, price, row, isVip}
    maxSeats: 0,
    timer: 600 // 10분 (초 단위)
};

// 1. 좌석 그리드 생성 (리액트의 generateSeats 로직)
function initSeatMap() {
    const grid = document.getElementById('seat-map-grid');
    const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'];
    const reservedMock = ['A3', 'A4', 'B10', 'E7', 'H7', 'H8']; // 실제론 DB의 schedule_seat 데이터 사용

    rows.forEach((row, rowIndex) => {
        const rowDiv = document.createElement('div');
        rowDiv.className = "flex justify-center items-center mb-1";

        // 행 레이블
        const label = document.createElement('div');
        label.className = "w-8 text-[10px] text-gray-600 font-bold";
        label.innerText = row;
        rowDiv.appendChild(label);

        for (let col = 1; col <= 15; col++) {
            const seatId = `${row}${col}`;
            const isReserved = reservedMock.includes(seatId);
            const isVip = rowIndex >= 6 && col >= 5 && col <= 11; // G-J열 중앙
            const basePrice = isVip ? 18000 : 12000;

            const seat = document.createElement('div');
            seat.id = `seat-${seatId}`;
            seat.className = `seat ${isReserved ? 'reserved' : (isVip ? 'vip' : 'available')}`;
            seat.innerText = isReserved ? 'X' : col;

            if (!isReserved) {
                seat.onclick = () => toggleSeat(seatId, basePrice, row, isVip, seat);
            }
            rowDiv.appendChild(seat);
        }
        grid.appendChild(rowDiv);
    });
    startTimer();
}

// 2. 좌석 토글 (리액트의 handleSeatSelect 로직)
function toggleSeat(id, price, row, isVip, element) {
    const totalCount = bookingState.adult + bookingState.youth;
    const index = bookingState.selectedSeats.findIndex(s => s.id === id);

    if (index > -1) {
        // 해제
        bookingState.selectedSeats.splice(index, 1);
        element.classList.remove('selected');
    } else {
        // 인원수 체크
        if (totalCount === 0) {
            alert("인원을 먼저 선택해주세요.");
            return;
        }
        if (bookingState.selectedSeats.length >= totalCount) {
            alert(`최대 ${totalCount}석까지만 선택 가능합니다.`);
            return;
        }
        // 선택
        bookingState.selectedSeats.push({ id, price });
        element.classList.add('selected');
    }
    updateSummary();
}

// 3. 인원 수 변경
function updateCount(type, delta) {
    bookingState[type] = Math.max(0, bookingState[type] + delta);
    document.getElementById(`${type}-count`).innerText = bookingState[type];
    
    // 인원 줄였을 때 초과 선택된 좌석 해제
    const total = bookingState.adult + bookingState.youth;
    while(bookingState.selectedSeats.length > total) {
        const removed = bookingState.selectedSeats.pop();
        document.getElementById(`seat-${removed.id}`).classList.remove('selected');
    }
    updateSummary();
}

// 4. 하단 요약 바 업데이트 (리액트의 totalPrice 계산 로직)
function updateSummary() {
    const seatNames = bookingState.selectedSeats.map(s => s.id).sort().join(', ');
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

// 5. 점유 타이머 (10분)
function startTimer() {
    const timerEl = document.getElementById('hold-timer');
    const interval = setInterval(() => {
        const min = String(Math.floor(bookingState.timer / 60)).padStart(2, '0');
        const sec = String(bookingState.timer % 60).padStart(2, '0');
        timerEl.innerText = `좌석 임시 점유 중 ${min}:${sec}`;
        
        if (bookingState.timer <= 0) {
            clearInterval(interval);
            alert("점유 시간이 만료되었습니다. 다시 선택해주세요.");
            location.reload();
        }
        bookingState.timer--;
    }, 1000);
}

// 6. 예매 제출 (백엔드 컨트롤러와 연동)
function handleBookingSubmit() {
    // 1. 선택된 좌석들의 ID만 추출 (예: ["A1", "A2"])
    const selectedSeatIds = bookingState.selectedSeats.map(s => s.id);
    const totalPriceValue = bookingState.selectedSeats.reduce((sum, s) => sum + s.price, 0);

    if (selectedSeatIds.length === 0) {
        alert("좌석을 선택해 주세요.");
        return;
    }

    if (confirm(`${selectedSeatIds.join(', ')} 좌석으로 예매를 진행하시겠습니까?`)) {
        
        // 2. HTML에 숨겨둔 Form의 input들에 값 채우기
        // (아까 HTML에 추가한 id들과 정확히 맞아야 합니다)
        const seatIdsInput = document.getElementById('input-seat-ids');
        const totalPriceInput = document.getElementById('input-total-price');
        const bookingForm = document.getElementById('bookingForm');

        if (seatIdsInput && totalPriceInput && bookingForm) {
            // 스프링 DTO가 List<Long>으로 받을 수 있게 콤마(,)로 연결된 문자열로 전달
            // 만약 DB의 seat_id가 숫자라면, 여기서 적절히 변환 로직을 넣을 수 있습니다.
            // 현재는 시각적인 좌석번호(A1)를 보내는 예시입니다.
            seatIdsInput.value = selectedSeatIds.join(','); 
            totalPriceInput.value = totalPriceValue;

            // 3. 서버(Controller)로 폼 전송!
            bookingForm.submit();
        } else {
            console.error("전송용 Form을 찾을 수 없습니다. HTML에 <form id='bookingForm'>이 있는지 확인하세요.");
            alert("시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}
// 파일 최하단
document.addEventListener('DOMContentLoaded', function() {
    console.log("DOM 로드 완료 - 좌석 생성을 시작합니다.");
    initSeatMap(); // 여기서 함수를 호출해야 안전합니다.
});