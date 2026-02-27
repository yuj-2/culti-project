let currentPaymentData = null;

/**
 * 결제 모달 열기
 */
function openPaymentModal(data) {
    currentPaymentData = data;
    document.getElementById('md-title').innerText = data.title;
    document.getElementById('md-detail').innerText = data.details;
    document.getElementById('md-amount').innerText = data.amount.toLocaleString();
    document.getElementById('paymentModal').style.display = 'flex';
}

/**
 * 결제 모달 닫기
 */
function closeModal() {
    document.getElementById('paymentModal').style.display = 'none';
}

/**
 * 포트원 결제 실행 (채널 키 방식 최적화)
 */
function executePortOne(payMethod) {
    const IMP = window.IMP;
    // [중요] 관리자 센터의 '내 식별코드'와 일치하는지 다시 확인하세요.
    IMP.init("imp06217828"); 

    // 관리자 센터의 채널키를 객체로 관리
    const CHANNEL_KEYS = {
        KAKAO: "channel-key-37e73800-4d4e-45c6-b48e-73133dbaee29",
        TOSS: "channel-key-208b6533-848c-4fd4-83b3-0590b86ef4c0",
        NAVER: "channel-key-e64f15df-20b8-4b98-b80e-142f114773e6",
        CARD: "channel-key-95ebcbee-1000-43a5-884e-0d2173d27a37"
    };

    // 시큐리티가 적용된 HTML 폼에서 이메일 정보 추출
    const buyerEmail = document.querySelector('input[name="userEmail"]')?.value;
    const buyerName = "CULTI_USER"; // 필요 시 세션 유저 이름으로 연동

    if (!buyerEmail) {
        alert("로그인 세션이 만료되었거나 정보가 없습니다. 다시 로그인해 주세요.");
        return;
    }

    // 결제 수단 대문자 변환하여 키 매칭
    const selectedKey = CHANNEL_KEYS[payMethod.toUpperCase()];
    
    if (!selectedKey) {
        alert("해당 결제 수단의 채널 키 설정이 필요합니다.");
        return;
    }

    // V2 방식: pg/pay_method 파라미터 없이 channelKey만 사용
    let payConfig = {
        channelKey: selectedKey,
        merchant_uid: "CULTI_" + new Date().getTime(),
        name: currentPaymentData.title,
        amount: currentPaymentData.amount,
        buyer_email: buyerEmail, 
        buyer_name: buyerName,
        m_redirect_url: window.location.origin + "/reservation/booking/seat"
    };

    console.log("결제 요청 데이터:", payConfig);

    IMP.request_pay(payConfig, function (rsp) {
        if (rsp.success) {
            verifyAndSubmit(rsp);
        } else {
            // 에러 메시지 undefined 방지 처리
            console.error("결제 실패 응답:", rsp);
            alert("결제에 실패했습니다: " + (rsp.error_msg || "네트워크 오류 또는 설정 미비"));
        }
    });
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