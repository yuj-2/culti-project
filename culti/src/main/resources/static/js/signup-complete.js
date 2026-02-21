// DOM이 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
    // 페이지 로드 시 애니메이션 효과
    console.log('회원가입 완료 페이지 로드됨');

    // 선택사항: 축하 효과 추가 (간단한 콘페티 효과)
    createConfetti();

    // 일정 시간 후 로그인 페이지로 자동 이동 (선택사항)
    // setTimeout(function() {
    //     window.location.href = '/login.html';
    // }, 10000); // 10초 후 자동 이동
});

// 축하 콘페티 효과 (선택사항)
function createConfetti() {
    const colors = ['#503396', '#6b46c1', '#8b5cf6', '#a78bfa', '#c4b5fd'];
    const confettiCount = 50;
    const body = document.body;

    for (let i = 0; i < confettiCount; i++) {
        const confetti = document.createElement('div');
        confetti.style.position = 'fixed';
        confetti.style.width = '10px';
        confetti.style.height = '10px';
        confetti.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
        confetti.style.left = Math.random() * 100 + '%';
        confetti.style.top = '-10px';
        confetti.style.opacity = Math.random();
        confetti.style.transform = 'rotate(' + Math.random() * 360 + 'deg)';
        confetti.style.animation = `confetti-fall ${3 + Math.random() * 2}s linear forwards`;
        confetti.style.zIndex = '9999';
        confetti.style.pointerEvents = 'none';
        confetti.style.borderRadius = Math.random() > 0.5 ? '50%' : '0';

        body.appendChild(confetti);

        // 애니메이션 후 제거
        setTimeout(() => {
            confetti.remove();
        }, 5000);
    }
}

// 로그인 버튼 클릭 추적 (선택사항 - 분석용)
const loginBtn = document.querySelector('.btn-primary');
if (loginBtn) {
    loginBtn.addEventListener('click', function() {
        console.log('로그인 페이지로 이동');
        // 여기에 Google Analytics나 다른 분석 도구 코드 추가 가능
        // gtag('event', 'click', { 'event_category': 'signup_complete', 'event_label': 'login_button' });
    });
}

// 메인으로 이동 버튼 클릭 추적 (선택사항)
const mainBtn = document.querySelector('.btn-secondary');
if (mainBtn) {
    mainBtn.addEventListener('click', function() {
        console.log('메인 페이지로 이동');
        // 여기에 Google Analytics나 다른 분석 도구 코드 추가 가능
        // gtag('event', 'click', { 'event_category': 'signup_complete', 'event_label': 'main_button' });
    });
}

// 웰컴 쿠폰 자동 발급 알림 (선택사항)
setTimeout(function() {
    console.log('웰컴 쿠폰이 발급되었습니다.');
    // 실제 프로젝트에서는 서버에서 ��폰 발급 API 호출
    /*
    fetch('/api/issue-welcome-coupon', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        console.log('쿠폰 발급 완료:', data);
    })
    .catch(error => {
        console.error('쿠폰 발급 오류:', error);
    });
    */
}, 1000);

// 페이지 이탈 방지 (선택사항 - 사용자가 실수로 페이지를 닫는 것 방지)
// window.addEventListener('beforeunload', function(e) {
//     // 실제 프로젝트에서는 특정 조건에서만 활성화
//     // e.preventDefault();
//     // e.returnValue = '';
// });

// 소셜 공유 기능 (선택사항)
function shareOnSocial(platform) {
    const url = encodeURIComponent(window.location.origin);
    const text = encodeURIComponent('CULTI에 가입했어요! 함께 문화생활을 즐겨요 🎉');

    let shareUrl = '';

    switch(platform) {
        case 'facebook':
            shareUrl = `https://www.facebook.com/sharer/sharer.php?u=${url}`;
            break;
        case 'twitter':
            shareUrl = `https://twitter.com/intent/tweet?text=${text}&url=${url}`;
            break;
        case 'kakao':
            // 카카오톡 공유는 카카오 SDK 필요
            console.log('카카오톡 공유 기능은 카카오 SDK가 필요합니다.');
            return;
        default:
            return;
    }

    window.open(shareUrl, '_blank', 'width=600,height=400');
}

// 이메일 확인 재발송 기능 (선택사항)
function resendConfirmationEmail() {
    console.log('확인 이메일 재발송 요청');
    
    // 실제 서버 통신 코드
    /*
    fetch('/api/resend-confirmation', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('확인 이메일이 재발송되었습니다.');
        } else {
            alert('이메일 발송에 실패했습니다.');
        }
    })
    .catch(error => {
        console.error('이메일 재발송 오류:', error);
        alert('서버 오류가 발생했습니다.');
    });
    */
    
    alert('확인 이메일이 재발송되었습니다.');
}

// 회원가입 완료 이벤트 추적 (Google Analytics, Facebook Pixel 등)
function trackSignupComplete() {
    // Google Analytics
    if (typeof gtag !== 'undefined') {
        gtag('event', 'sign_up', {
            'method': 'Email'
        });
    }

    // Facebook Pixel
    if (typeof fbq !== 'undefined') {
        fbq('track', 'CompleteRegistration');
    }

    // 기타 분석 도구
    console.log('회원가입 완료 이벤트 추적');
}

// 페이지 로드 시 추적
trackSignupComplete();
