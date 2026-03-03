// DOM이 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
    // 폼 요소들
    const loginForm = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const emailError = document.getElementById('emailError');
    const passwordError = document.getElementById('passwordError');
    const loginButton = loginForm.querySelector('.btn-login');

    // 소셜 로그인 버튼들
    const kakaoLoginBtn = document.getElementById('kakaoLogin');
    const naverLoginBtn = document.getElementById('naverLogin');
    const googleLoginBtn = document.getElementById('googleLogin');

    // 이메일 유효성 검사
    function validateEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    // 비밀번호 유효성 검사
    function validatePassword(password) {
        return password.length >= 6;
    }

    // 에러 메시지 표시
    function showError(inputElement, errorElement, message) {
        inputElement.classList.add('error');
        errorElement.textContent = message;
        errorElement.classList.add('show');
    }

    // 에러 메시지 숨김
    function hideError(inputElement, errorElement) {
        inputElement.classList.remove('error');
        errorElement.textContent = '';
        errorElement.classList.remove('show');
    }

    // 실시간 유효성 검사
    emailInput.addEventListener('blur', function() {
        const email = emailInput.value.trim();
        if (email && !validateEmail(email)) {
            showError(emailInput, emailError, '올바른 이메일 형식을 입력해주세요.');
        } else {
            hideError(emailInput, emailError);
        }
    });

    emailInput.addEventListener('input', function() {
        if (emailInput.classList.contains('error')) {
            hideError(emailInput, emailError);
        }
    });

    passwordInput.addEventListener('blur', function() {
        const password = passwordInput.value;
        if (password && !validatePassword(password)) {
            showError(passwordInput, passwordError, '비밀번호는 최소 6자 이상이어야 합니다.');
        } else {
            hideError(passwordInput, passwordError);
        }
    });

    passwordInput.addEventListener('input', function() {
        if (passwordInput.classList.contains('error')) {
            hideError(passwordInput, passwordError);
        }
    });

    // 폼 제출 처리
	/*
    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();

        // 모든 에러 메시지 초기화
        hideError(emailInput, emailError);
        hideError(passwordInput, passwordError);

        const email = emailInput.value.trim();
        const password = passwordInput.value;
        const rememberMe = document.getElementById('rememberMe').checked;

        let isValid = true;

        // 이메일 검증
        if (!email) {
            showError(emailInput, emailError, '이메일을 입력해주세요.');
            isValid = false;
        } else if (!validateEmail(email)) {
            showError(emailInput, emailError, '올바른 이메일 형식을 입력해주세요.');
            isValid = false;
        }

        // 비밀번호 검증
        if (!password) {
            showError(passwordInput, passwordError, '비밀번호를 입력해주세요.');
            isValid = false;
        } else if (!validatePassword(password)) {
            showError(passwordInput, passwordError, '비밀번호는 최소 6자 이상이어야 합니다.');
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // 로딩 상태 표시
        loginButton.classList.add('loading');
        loginButton.disabled = true;

        // 서버로 전송할 데이터
        const loginData = {
            email: email,
            password: password,
            rememberMe: rememberMe
        };

        // 실제 서버 통신 코드 (예시)
        // 여기서는 콘솔에 출력만 하고 2초 후 성공으로 처리
        console.log('로그인 시도:', loginData);

        

    });*/

    // 카카오 로그인
    kakaoLoginBtn.addEventListener('click', function() {
        console.log('카카오 로그인 시도');
        
        // 실제 카카오 로그인 연동 코드
        /*
        // Kakao SDK 초기화 (head에 SDK 스크립트 필요)
        Kakao.Auth.login({
            success: function(authObj) {
                console.log('카카오 로그인 성공', authObj);
                // 서버로 토큰 전송 및 처리
                window.location.href = '/auth/kakao/callback?token=' + authObj.access_token;
            },
            fail: function(err) {
                console.error('카카오 로그인 실패', err);
                alert('카카오 로그인에 실패했습니다.');
            }
        });
        */
        
        // 데모용
        alert('카카오 로그인 기능은 카카오 개발자 센터에서 앱 등록 후 사용 가능합니다.');
    });

    // 네이버 로그인
    naverLoginBtn.addEventListener('click', function() {
        console.log('네이버 로그인 시도');
        
        // 실제 네이버 로그인 연동 코드
        /*
        // 네이버 로그인 페이지로 리다이렉트
        const clientId = 'YOUR_CLIENT_ID';
        const redirectUri = encodeURIComponent('YOUR_REDIRECT_URI');
        const state = Math.random().toString(36).substring(2, 15);
        const naverLoginUrl = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}`;
        window.location.href = naverLoginUrl;
        */
        
        // 데모용
        alert('네이버 로그인 기능은 네이버 개발자 센터에서 앱 등록 후 사용 가능합니다.');
    });

    // 구글 로그인
    googleLoginBtn.addEventListener('click', function() {
        console.log('구글 로그인 시도');
        
        // 실제 구글 로그인 연동 코드
        /*
        // Google OAuth2 연동 (Google Identity Services 라이브러리 필요)
        google.accounts.id.initialize({
            client_id: 'YOUR_GOOGLE_CLIENT_ID',
            callback: handleGoogleLogin
        });
        
        google.accounts.id.prompt();
        
        function handleGoogleLogin(response) {
            console.log('구글 로그인 성공', response);
            // 서버로 credential 전송
            fetch('/auth/google/callback', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ credential: response.credential })
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    window.location.href = '/';
                }
            });
        }
        */
        
        // 데모용
        alert('구글 로그인 기능은 Google Cloud Console에서 OAuth 클라이언트 설정 후 사용 가능합니다.');
    });

    // Enter 키로 로그인
    passwordInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            loginForm.dispatchEvent(new Event('submit'));
        }
    });
});

// 소셜 로그인 통합 처리 함수들

/**
 * 카카오 로그인 초기화 및 로그인
 * 사용 전에 Kakao SDK를 HTML에 추가해야 합니다:
 * <script src="https://developers.kakao.com/sdk/js/kakao.js"></script>
 */
function initKakaoLogin(appKey) {
    if (typeof Kakao !== 'undefined') {
        Kakao.init(appKey);
        console.log('카카오 SDK 초기화 완료');
    }
}

/**
 * 네이버 로그인 URL 생성
 */
function getNaverLoginUrl(clientId, redirectUri, state) {
    const url = new URL('https://nid.naver.com/oauth2.0/authorize');
    url.searchParams.append('response_type', 'code');
    url.searchParams.append('client_id', clientId);
    url.searchParams.append('redirect_uri', redirectUri);
    url.searchParams.append('state', state);
    return url.toString();
}

/**
 * 구글 로그인 초기화
 * 사용 전에 Google Identity Services 라이브러리를 HTML에 추가해야 합니다:
 * <script src="https://accounts.google.com/gsi/client" async defer></script>
 */
function initGoogleLogin(clientId, callback) {
    if (typeof google !== 'undefined') {
        google.accounts.id.initialize({
            client_id: clientId,
            callback: callback
        });
        console.log('구글 SDK 초기화 완료');
    }
}

// 유틸리티 함수들

/**
 * 쿠키 설정
 */
function setCookie(name, value, days) {
    const expires = new Date();
    expires.setTime(expires.getTime() + (days * 24 * 60 * 60 * 1000));
    document.cookie = name + '=' + value + ';expires=' + expires.toUTCString() + ';path=/';
}

/**
 * 쿠키 가져오기
 */
function getCookie(name) {
    const nameEQ = name + '=';
    const ca = document.cookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

/**
 * 로컬 스토리지에 토큰 저장
 */
function saveAuthToken(token) {
    localStorage.setItem('authToken', token);
}

/**
 * 로컬 스토리지에서 토큰 가져오기
 */
function getAuthToken() {
    return localStorage.getItem('authToken');
}

/**
 * 로그아웃 (토큰 제거)
 */
function logout() {
    localStorage.removeItem('authToken');
    sessionStorage.clear();
    window.location.href = '/login';
}
