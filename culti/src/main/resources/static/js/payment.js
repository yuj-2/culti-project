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
 * 포트원 결제 실행
 */
function executePortOne(payMethod) {
    const IMP = window.IMP;
    
    // [수정] HTML에서 넘겨준 변수 사용 (PORTONE_IMP_ID)
    IMP.init(PORTONE_IMP_ID); 


    // [수정] HTML에서 넘겨준 변수 사용 (PORTONE_CHANNELS)
    const CHANNEL_KEYS = PORTONE_CHANNELS;

    // 관리자 센터의 채널키를 객체로 관리
    const CHANNEL_KEYS = {

    };


    const buyerEmail = document.querySelector('input[name="userEmail"]')?.value;
    const buyerName = "CULTI_USER";

    if (!buyerEmail) {
        alert("로그인 세션이 만료되었거나 정보가 없습니다. 다시 로그인해 주세요.");
        return;
    }

    const selectedKey = CHANNEL_KEYS[payMethod.toUpperCase()];
    
    if (!selectedKey) {
        alert("해당 결제 수단의 채널 키 설정이 필요합니다.");
        return;
    }

    let payConfig = {
        channelKey: selectedKey,
        merchant_uid: "CULTI_" + new Date().getTime(),
        name: currentPaymentData.title,
        amount: currentPaymentData.amount,
        buyer_email: buyerEmail, 
        buyer_name: buyerName,
        m_redirect_url: window.location.origin + "/reservation/booking/seat"
    };

    IMP.request_pay(payConfig, function (rsp) {
        if (rsp.success) {
            verifyAndSubmit(rsp);
        } else {
            console.error("결제 실패 응답:", rsp);
            alert("결제에 실패했습니다: " + (rsp.error_msg || "네트워크 오류 또는 설정 미비"));
        }
    });
}

/**
 * [수정] 결제 검증 후 결과 페이지로 직접 리다이렉트
 */
function verifyAndSubmit(rsp) {
    fetch("/payment/verify", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            imp_uid: rsp.imp_uid,
            merchant_uid: rsp.merchant_uid,
            total_price: rsp.paid_amount,
            // 폼에 있던 데이터도 같이 보내서 한 번에 DB 저장 처리
            scheduleId: document.querySelector('input[name="scheduleId"]')?.value,
            seatIds: document.getElementById('input-seat-ids')?.value
        })
    }).then(res => {
        if(res.ok) {
            // 서버에서 저장 성공 후 생성된 bookingId를 JSON으로 준다고 가정
            return res.json();
        } else {
            throw new Error("검증 실패");
        }
    }).then(data => {
        // [핵심] 성공하면 바로 결과 페이지로 이동!
        location.href = "/reservation/booking/result/" + data.bookingId;
    }).catch(err => {
        console.error("통신 에러:", err);
        alert("결제 저장 중 오류가 발생했습니다. 고객센터로 문의하세요.");
    });
}
