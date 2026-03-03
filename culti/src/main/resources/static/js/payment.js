/* ================================
   CULTI Payment Script
================================ */

(function () {

    const MY_IMP_CODE = "imp06217828"; // 가맹점 식별코드

    function init() {
        if (window.IMP) {
            window.IMP.init(MY_IMP_CODE);
            console.log("✅ 포트원 초기화 완료:", MY_IMP_CODE);
        } else {
            console.error("❌ iamport.js 로드 실패");
        }
    }

    window.addEventListener("load", init);

})();


/* ================================
   모달 닫기
================================ */

window.closeModal = function () {

    const modal = document.getElementById("paymentModal");
    if (modal) modal.style.display = "none";

    const submitBtn = document.getElementById("submit-booking-btn");
    if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.innerText = "결제하기";
        submitBtn.style.opacity = "1";
    }
};


/* ================================
   결제 실행
================================ */

window.executePortOne = function (type) {

    console.log("🚀 결제 시작:", type);

    if (!window.IMP) {
        alert("결제 시스템이 로드되지 않았습니다.");
        return;
    }

    try {

        const scheduleId = document.getElementById("scheduleId")?.value;
        const totalPrice = document.getElementById("input-total-price")?.value;
        const seatIdsRaw = document.getElementById("input-seat-ids")?.value;
        const userEmail = document.getElementById("userEmail")?.value || "test@culti.com";

        if (!scheduleId || !totalPrice) {
            alert("예매 정보가 올바르지 않습니다.");
            return;
        }

        const seatIds = seatIdsRaw
            ? seatIdsRaw.split(",").map(s => s.trim())
            : [];

        // 🔥🔥🔥 여기 추가 (4종 인원 읽기)
        const adultCount   = parseInt(document.getElementById("adult-count")?.innerText || 0);
        const youthCount   = parseInt(document.getElementById("youth-count")?.innerText || 0);
        const seniorCount  = parseInt(document.getElementById("senior-count")?.innerText || 0);
        const specialCount = parseInt(document.getElementById("special-count")?.innerText || 0);

        const submitBtn = document.getElementById("submit-booking-btn");
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerText = "결제 진행 중...";
        }

        let pg = "html5_inicis";

        if (type === "KAKAO") {
            pg = "kakaopay.TC0ONETIME";
        } else if (type === "NAVER") {
            pg = "naverpay";
        } else if (type === "TOSS") {
            pg = "tosspay";
        }

        window.IMP.request_pay({
            pg: pg,
            pay_method: "card",
            merchant_uid: "CULTI_" + Date.now(),
            name: document.getElementById("md-title")?.innerText || "CULTI 예매",
            amount: parseInt(totalPrice),
            buyer_email: userEmail
        }, function (rsp) {

            console.log("📩 결제 응답:", rsp);

            if (rsp.success) {

                verifyPayment(
                    rsp,
                    scheduleId,
                    totalPrice,
                    seatIds,
                    adultCount,
                    youthCount,
                    seniorCount,
                    specialCount
                );

            } else {
                alert("결제 실패: " + rsp.error_msg);
                window.closeModal();
            }

        });

    } catch (e) {
        console.error("⚠️ 결제 실행 중 오류:", e);
        window.closeModal();
    }
};


/* ================================
   서버 검증 (🔥 수정 완료)
================================ */

function verifyPayment(
    rsp,
    scheduleId,
    totalPrice,
    seatIds,
    adultCount,
    youthCount,
    seniorCount,
    specialCount
) {

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    console.log("🔍 서버 검증 요청");

    fetch("/payment/verify", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify({
            scheduleId: scheduleId,
            seatIds: seatIds,
            adultCount: adultCount,
            youthCount: youthCount,
            seniorCount: seniorCount,
            specialCount: specialCount,
            totalPrice: parseInt(totalPrice),
            impUid: rsp.imp_uid,
            merchantUid: rsp.merchant_uid
        })
    })
    .then(res => {
        if (!res.ok) {
            throw new Error("HTTP 오류: " + res.status);
        }
        return res.json();
    })
    .then(data => {

        if (data.bookingId || data.status === "SUCCESS") {

            const bookingId = data.bookingId || data.id;
            console.log("✅ 예매 성공:", bookingId);

            location.href = "/reservation/booking/result/" + bookingId;

        } else {
            alert("검증 실패: " + (data.message || "오류 발생"));
            window.closeModal();
        }

    })
    .catch(err => {
        console.error("❌ 서버 통신 오류:", err);
        alert("결제는 되었으나 서버 저장 중 오류가 발생했습니다.");
        window.closeModal();
    });
}