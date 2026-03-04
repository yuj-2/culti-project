/**
 * 결제 실행 함수
 */
async function handleTossPayment() {
    // [수정] 만약 booking_seat.js에서 선택된 좌석 배열 이름이 다르다면 그 이름을 써야 합니다.
    // 예: selectedSeatList 가 실제 변수명이라면 그것으로 교체하세요.
    
    // 에러 방지를 위해 변수 존재 여부 체크 로직 추가
    if (typeof selectedSeats === 'undefined') {
        console.error("좌석 선택 데이터(selectedSeats)를 찾을 수 없습니다. 변수명을 확인하세요.");
        alert("시스템 오류: 좌석 데이터를 불러올 수 없습니다.");
        return;
    }

    const seatIds = selectedSeats.map(s => s.seatId); 
    const totalPrice = calculateTotalPrice(); 
    const scheduleId = document.getElementById('scheduleId').value;
    
    // [수정] CSRF 토큰을 가져오는 ID를 확인하세요. (HTML에 id="csrf-token"이 있어야 함)
    const csrfToken = document.getElementById('csrf-token')?.value || 
                      document.querySelector('input[name="_csrf"]')?.value;

    if (seatIds.length === 0) {
        alert("좌석을 선택해주세요.");
        return;
    }

    try {
        const response = await fetch("/reservation/booking/create/json", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "X-CSRF-TOKEN": csrfToken
            },
            body: new URLSearchParams({
                'scheduleId': scheduleId,
                'seatIds': seatIds.join(','),
                'totalPrice': totalPrice
            })
        });

        if (!response.ok) throw new Error("서버 예매 정보 생성 실패");

        const data = await response.json(); 

        const clientKey = 'test_ck_발급받은키'; // 본인 키로 교체
        const tossPayments = TossPayments(clientKey);

        // [참고] 토스 SDK 버전에 따라 amount 형식이 다를 수 있으니 확인하세요.
        await tossPayments.requestPayment('카드', {
            amount: data.totalPrice,
            orderId: data.bookingNumber, // BookingResponseDTO의 필드명과 일치해야 함
            orderName: data.movieTitle,
            successUrl: window.location.origin + '/payment/success',
            failUrl: window.location.origin + '/payment/fail',
        });

    } catch (error) {
        console.error("결제 프로세스 에러:", error);
        alert("오류 발생: " + error.message);
    }
}