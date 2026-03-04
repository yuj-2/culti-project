// DOM이 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
    // 폼 요소들
    const findPasswordForm = document.getElementById('findPasswordForm');
    const nameInput = document.getElementById('name');
    const emailInput = document.getElementById('email');
    const nameError = document.getElementById('nameError');
    const emailError = document.getElementById('emailError');
    const submitButton = findPasswordForm.querySelector('.btn-submit');

    // 성공 메시지 박스
    const findPasswordBox = document.querySelector('.find-password-box');
    const successBox = document.getElementById('successBox');
    const sentEmailSpan = document.getElementById('sentEmail');

    // 이름 유효성 검사
    function validateName(name) {
        // 이름은 최소 2자 이상, 한글/영문만 허용
        const nameRegex = /^[가-힣a-zA-Z]{2,}$/;
        return nameRegex.test(name);
    }

    // 이메일 유효성 검사
    function validateEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
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

    // 실시간 유효성 검사 - 이름
    nameInput.addEventListener('blur', function() {
        const name = nameInput.value.trim();
        if (name && !validateName(name)) {
            showError(nameInput, nameError, '이름은 한글 또는 영문 2자 이상 입력해주세요.');
        } else {
            hideError(nameInput, nameError);
        }
    });

    nameInput.addEventListener('input', function() {
        if (nameInput.classList.contains('error')) {
            hideError(nameInput, nameError);
        }
    });

    // 실시간 유효성 검사 - 이메일
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

    // 폼 제출 처리
    findPasswordForm.addEventListener('submit', function(e) {
        e.preventDefault();

        // 모든 에러 메시지 초기화
        hideError(nameInput, nameError);
        hideError(emailInput, emailError);

        const name = nameInput.value.trim();
        const email = emailInput.value.trim();

        let isValid = true;

        // 이름 검증
        if (!name) {
            showError(nameInput, nameError, '이름을 입력해주세요.');
            isValid = false;
        } else if (!validateName(name)) {
            showError(nameInput, nameError, '이름은 한글 또는 영문 2자 이상 입력해주세요.');
            isValid = false;
        }

        // 이메일 검증
        if (!email) {
            showError(emailInput, emailError, '이메일을 입력해주세요.');
            isValid = false;
        } else if (!validateEmail(email)) {
            showError(emailInput, emailError, '올바른 이메일 형식을 입력해주세요.');
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // 로딩 상태 표시
        submitButton.classList.add('loading');
        submitButton.disabled = true;

        // 서버로 전송할 데이터
        const findPasswordData = {
            name: name,
            email: email
        };

        // 실제 서버 통신 코드 (예시)
        console.log('비밀번호 찾기 요청:', findPasswordData);

        // 실제 프로젝트에서는 아래와 같이 서버로 요청을 보냅니다:
        /*
        fetch('/api/find-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(findPasswordData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // 성공: 성공 메시지 표시
                showSuccessMessage(email);
            } else {
                // 실패: 사용자를 찾을 수 없음
                showError(emailError, '입력하신 정보와 일치하는 계정을 찾을 수 없습니다.');
                submitButton.classList.remove('loading');
                submitButton.disabled = false;
            }
        })
        .catch(error => {
            console.error('비밀번호 찾기 오류:', error);
            showError(emailError, '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
            submitButton.classList.remove('loading');
            submitButton.disabled = false;
        });
        */

        // 데모용 타임아웃 (실제 프로젝트에서는 제거하세요)
        setTimeout(function() {
            submitButton.classList.remove('loading');
            submitButton.disabled = false;
            
            // 성공 메시지 표시
            showSuccessMessage(email);
        }, 2000);
    });

    // 성공 메시지 표시 함수
    function showSuccessMessage(email) {
        // 폼 박스 숨기기
        findPasswordBox.style.display = 'none';
        
        // 성공 박스 표시
        sentEmailSpan.textContent = email;
        successBox.classList.add('show');
    }

    // Enter 키로 제출
    emailInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            findPasswordForm.dispatchEvent(new Event('submit'));
        }
    });
});

// 유틸리티 함수들

/**
 * 비밀번호 재설정 토큰 검증
 * 실제 프로젝트에서는 이메일 링크를 클릭했을 때 이 함수를 사용합니다.
 */
function verifyResetToken(token) {
    // 서버로 토큰 검증 요청
    /*
    fetch('/api/verify-reset-token', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ token: token })
    })
    .then(response => response.json())
    .then(data => {
        if (data.valid) {
            // 토큰이 유효하면 비밀번호 재설정 페이지로 이동
            window.location.href = '/reset-password?token=' + token;
        } else {
            alert('유효하지 않거나 만료된 링크입니다.');
            window.location.href = '/find-password';
        }
    })
    .catch(error => {
        console.error('토큰 검증 오류:', error);
        alert('오류가 발생했습니다. 다시 시도해주세요.');
    });
    */
}

/**
 * 비밀번호 재설정
 * 실제 프로젝트에서는 비밀번호 재설정 페이지에서 이 함수를 사용합니다.
 */
function resetPassword(token, newPassword) {
    // 비밀번호 유효성 검사
    if (newPassword.length < 8) {
        alert('비밀번호는 최소 8자 이상이어야 합니다.');
        return;
    }

    // 서버로 비밀번호 재설정 요청
    /*
    fetch('/api/reset-password', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
            token: token,
            newPassword: newPassword
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('비밀번호가 성공적으로 변경되었습니다.');
            window.location.href = '/login';
        } else {
            alert(data.message || '비밀번호 재설정에 실패했습니다.');
        }
    })
    .catch(error => {
        console.error('비밀번호 재설정 오류:', error);
        alert('오류가 발생했습니다. 다시 시도해주세요.');
    });
    */
}

/**
 * 이메일 재전송
 * 사용자가 이메일을 받지 못한 경우
 */
function resendResetEmail(email) {
    // 서버로 이메일 재전송 요청
    /*
    fetch('/api/resend-reset-email', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email: email })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('비밀번호 재설정 이메일이 재전송되었습니다.');
        } else {
            alert(data.message || '이메일 재전송에 실패했습니다.');
        }
    })
    .catch(error => {
        console.error('이메일 재전송 오류:', error);
        alert('오류가 발생했습니다. 다시 시도해주세요.');
    });
    */
}
