// 중복 실행 방지
if (!window.__BOOKING_SEAT_LOADED__) {
    window.__BOOKING_SEAT_LOADED__ = true;

    /* 티켓 가격 설정 */
    const TICKET_PRICES = {
        adult: 15000,
        youth: 12000,
        senior: 10000,
        special: 8000
    };

    /* 상태 관리 객체 */
    const bookingState = {
        adult: 0,
        youth: 0,
        senior: 0,
        special: 0,
        selectedSeats: []
    };

    function getTotalNeeded() {
        return bookingState.adult + bookingState.youth +
               bookingState.senior + bookingState.special;
    }

    /* 좌석 맵 초기화 */
    function initSeatMap() {
        const grid = document.getElementById("seat-map-grid");
        if (!grid) return;

        grid.innerHTML = "";
        const rows = ["A","B","C","D","E"];

        rows.forEach((row) => {
            const rowDiv = document.createElement("div");
            rowDiv.className = "flex justify-center mb-1";

            for (let col = 1; col <= 15; col++) {
                const displayId = row + col;
                const seat = document.createElement("div");

                // [교정] 예약된 좌석 리스트(window.seatListFromDb)가 있다면 reserved 처리
                const isReserved = window.seatListFromDb && window.seatListFromDb.some(s => s.seatRow === row && s.seatCol === col && s.status === 'RESERVED');
                
                seat.className = isReserved ? "seat reserved" : "seat available";
                seat.innerText = col;

                if (!isReserved) {
                    seat.onclick = () => toggleSeat(displayId, seat);
                } else {
                    seat.innerText = "X";
                }

                rowDiv.appendChild(seat);
            }
            grid.appendChild(rowDiv);
        });
    }

    /* 좌석 선택/해제 */
    function toggleSeat(displayId, element) {
        const index = bookingState.selectedSeats.indexOf(displayId);
        const max = getTotalNeeded();

        if (index > -1) {
            bookingState.selectedSeats.splice(index, 1);
            element.classList.remove("selected");
        } else {
            if (max === 0) {
                alert("인원을 먼저 선택하세요.");
                return;
            }
            if (bookingState.selectedSeats.length >= max) {
                alert("선택 인원을 초과했습니다.");
                return;
            }
            bookingState.selectedSeats.push(displayId);
            element.classList.add("selected");
        }
        updateSummary();
    }

    /* 인원 변경 */
    window.updateCount = function(type, delta) {
        bookingState[type] = Math.max(0, bookingState[type] + delta);
        const el = document.getElementById(type + "-count");
        if (el) el.innerText = bookingState[type];
        updateSummary();
    };

    /* 요약 정보 및 Hidden Input 업데이트 (결제 데이터 핵심) */
    function updateSummary() {
        const totalNeeded = getTotalNeeded();
        const selected = bookingState.selectedSeats.length;
        const seatNames = bookingState.selectedSeats.join(", ");

        const seatEl = document.getElementById("display-seat-names");
        const priceEl = document.getElementById("display-total-price");
        const submitBtn = document.getElementById("submit-booking-btn");

        let totalPrice = 0;
        if (selected > 0) {
            const pricePool = [
                ...Array(bookingState.adult).fill(TICKET_PRICES.adult),
                ...Array(bookingState.youth).fill(TICKET_PRICES.youth),
                ...Array(bookingState.senior).fill(TICKET_PRICES.senior),
                ...Array(bookingState.special).fill(TICKET_PRICES.special)
            ];
            for (let i = 0; i < selected; i++) {
                totalPrice += pricePool[i] || 0;
            }
        }

        if (seatEl) seatEl.innerText = seatNames || "선택 안 됨";
        if (priceEl) priceEl.innerText = "₩" + totalPrice.toLocaleString();

        // 버튼 활성화 상태 제어
        if (submitBtn) {
            const isReady = (selected === totalNeeded && totalNeeded > 0);
            submitBtn.disabled = !isReady;
            submitBtn.style.backgroundColor = isReady ? "#503396" : "#333";
            submitBtn.style.cursor = isReady ? "pointer" : "not-allowed";
        }

        // [핵심] 결제 모달 및 서버 전송을 위한 Hidden Input 값 동기화
        const inputPrice = document.getElementById("input-total-price");
        const inputSeats = document.getElementById("input-seat-ids");
        if (inputPrice) inputPrice.value = totalPrice;
        if (inputSeats) inputSeats.value = bookingState.selectedSeats.join(",");
    }

    /* ===============================
       결제 모달 열기 로직 (수정됨)
    ================================= */
    window.handleBookingSubmit = function() {
        const totalNeeded = getTotalNeeded();
        const selected = bookingState.selectedSeats.length;

        if (selected !== totalNeeded || totalNeeded === 0) {
            alert("좌석 선택이 완료되지 않았습니다.");
            return;
        }

        const totalPrice = document.getElementById("input-total-price").value;
        const seatNames = bookingState.selectedSeats.join(", ");

        // [교정] 시스템 로딩 중 에러 해결을 위해 모달 표시 및 데이터 주입 강제 실행
        const modal = document.getElementById("paymentModal");
        if (modal) {
            modal.style.display = "flex"; // 모달 강제 노출
            
            // 모달 내 텍스트 업데이트
            const mdDetail = document.getElementById("md-detail");
            const mdAmount = document.getElementById("md-amount");
            if (mdDetail) mdDetail.innerText = seatNames;
            if (mdAmount) mdAmount.innerText = Number(totalPrice).toLocaleString();
            
            // 아이콘 리로드 (Lucide)
            if (window.lucide) window.lucide.createIcons();
        } else {
            console.error("결제 모달(paymentModal)을 찾을 수 없습니다.");
            alert("결제 시스템을 불러오는 중 오류가 발생했습니다.");
        }
    };

    document.addEventListener("DOMContentLoaded", initSeatMap);
}