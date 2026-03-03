
  (function() {
      const MY_IMP_CODE = "imp06217828"; // 🚨 여기에 본인의 '가맹점 식별코드'를 입력하세요!

      function init() {
          if (window.IMP) {
              window.IMP.init(MY_IMP_CODE);
              console.log("✅ 포트원 시스템 초기화 완료 (식별코드: " + MY_IMP_CODE + ")");
          } else {
              console.error("❌ 포트원 라이브러리(iamport.js)를 찾을 수 없습니다.");
          }
      }

      // 라이브러리 로드 시점에 즉시 실행
      init();
      
      // 페이지 로드 완료 시점에 다시 한번 보장
      window.addEventListener('load', init);
  })();

  /**
   * 2. UI 상태 복구 및 모달 닫기
   */
  window.closeModal = function() {
      const modal = document.getElementById("paymentModal");
      if (modal) modal.style.display = "none";
      
      const submitBtn = document.getElementById('submit-booking-btn');
      if (submitBtn) {
          submitBtn.disabled = false;
          submitBtn.style.opacity = '1';
          submitBtn.innerText = "결제하기";
      }
  };

  /**
   * 3. 결제 실행
   */
  window.executePortOne = function(type) {
      console.log("🚀 결제 프로세스 시작:", type);
      
      try {
          const scheduleId = document.getElementById("scheduleId")?.value;
          const totalPrice = document.getElementById("input-total-price")?.value;
          const seatIdsRaw = document.getElementById("input-seat-ids")?.value;
          const userEmail = document.getElementById("userEmail")?.value || "test@culti.com";

          if (!scheduleId || !totalPrice) {
              alert("예매 정보가 올바르지 않습니다. 다시 시도해주세요.");
              return;
          }

          const seatIds = seatIdsRaw ? seatIdsRaw.split(",").map(s => s.trim()) : [];
          const submitBtn = document.getElementById('submit-booking-btn');

          if (submitBtn) {
              submitBtn.disabled = true;
              submitBtn.innerText = "결제 진행 중...";
          }

          let pg = "html5_inicis"; 
          if (type === "KAKAO") pg = "kakaopay.TC0ONETIME";
          else if (type === "NAVER") pg = "naverpay";
          else if (type === "TOSS") pg = "tosspay";

          window.IMP.request_pay({
              pg: pg,
              pay_method: "card",
              merchant_uid: "CULTI_" + new Date().getTime(), // 중복 방지를 위한 타임스탬프
              name: document.getElementById("md-title")?.innerText || "CULTI 영화 예매",
              amount: parseInt(totalPrice),
              buyer_email: userEmail
          }, function(rsp) {
              console.log("📩 포트원 응답 수신:", rsp);
              
              if (rsp.success) {
                  // 검증 함수 호출
                  verifyPayment(rsp, scheduleId, totalPrice, seatIds);
              } else {
                  alert("결제에 실패하였습니다: " + rsp.error_msg);
                  window.closeModal();
              }
          });
      } catch (e) {
          console.error("⚠️ 결제 실행 중 스크립트 에러:", e);
          window.closeModal();
      }
  };

  /**
   * 4. 서버 검증 및 최종 리다이렉트
   */
  function verifyPayment(rsp, scheduleId, totalPrice, seatIds) {
      const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
      const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

      console.log("🔍 서버 결제 검증 요청 중...");


	  fetch("/api/payment/verify", {
	      method: "POST",
		  headers: {
		      "Content-Type": "application/json",
		      [csrfHeader]: csrfToken
		  },
	      body: JSON.stringify({
	          scheduleId: scheduleId,
	         seatIds: seatIds,   // ✅ 여기 수정
	          totalPrice: totalPrice,
	          impUid: rsp.imp_uid,
	          merchantUid: rsp.merchant_uid
	      })
      })
      .then(res => {
          if (!res.ok) throw new Error("서버 검증 응답 오류 (HTTP " + res.status + ")");
          return res.json();
      })
      .then(data => {
          if (data.status === "SUCCESS" || data.bookingId) {
              console.log("✅ 예매 완료. 결과 페이지로 이동합니다.");
              const bookingId = data.bookingId || data.id;
              location.href = "/reservation/booking/result/" + bookingId;
          } else {
              alert("검증 실패: " + (data.message || "결제 정보 불일치"));
              window.closeModal();
          }
      })
      .catch(err => {
          console.error("❌ 서버 통신 실패:", err);
          alert("결제는 성공했으나 서버 저장 중 오류가 발생했습니다. (DB 좌석 확인 필요)");
          window.closeModal();
      });
  }