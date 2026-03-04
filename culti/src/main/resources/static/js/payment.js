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

/**
 * 결제 검증 및 실제 예매 폼 전송
 */
function verifyAndSubmit(rsp) {
    // [보안] fetch 요청 시 시큐리티 CSRF 토큰 처리가 필요할 수 있습니다.
    fetch("/payment/verify", {
        method: "POST",
        headers: { 
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            imp_uid: rsp.imp_uid,
            merchant_uid: rsp.merchant_uid,
            total_price: rsp.paid_amount
        })
    }).then(res => {
        if(res.ok) {
            const bookingForm = document.getElementById('bookingForm');
            if (bookingForm) {
                // 검증된 금액을 폼에 세팅 후 전송
                document.getElementById('input-total-price').value = rsp.paid_amount;
                bookingForm.submit();
            } else {
                console.error("전송할 예약 폼(bookingForm)을 찾을 수 없습니다.");
            }
        } else {
            alert("서버 검증에 실패했습니다. 결제가 취소될 수 있습니다.");
        }
    }).catch(err => {
        console.error("통신 에러:", err);
        alert("결제 처리 중 통신 오류가 발생했습니다.");
    });
}
}