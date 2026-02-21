// DOM이 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
    // 폼 요소들
    const signupForm = document.getElementById('signupForm');
    const nameInput = document.getElementById('name');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const passwordConfirmInput = document.getElementById('passwordConfirm');
    const phoneInput = document.getElementById('phone');
    const signupBtn = document.getElementById('signupBtn');

    // 인증 관련 요소들
    const sendVerifyBtn = document.getElementById('sendVerifyBtn');
    const verificationGroup = document.getElementById('verificationGroup');
    const codeInputs = document.querySelectorAll('.code-input');
    const timerDisplay = document.getElementById('timerDisplay');
    const resendBtn = document.getElementById('resendBtn');

    // 에러 메시지 요소들
    const nameError = document.getElementById('nameError');
    const emailError = document.getElementById('emailError');
    const passwordError = document.getElementById('passwordError');
    const passwordConfirmError = document.getElementById('passwordConfirmError');
    const phoneError = document.getElementById('phoneError');
    const verificationError = document.getElementById('verificationError');
    const verificationSuccess = document.getElementById('verificationSuccess');
    const termsError = document.getElementById('termsError');

    // 약관 동의 요소들
    const agreeAll = document.getElementById('agreeAll');
    const requiredTerms = document.querySelectorAll('.required-term');
    const agreeTerms = document.getElementById('agreeTerms');
    const agreePrivacy = document.getElementById('agreePrivacy');
    const agreeMarketing = document.getElementById('agreeMarketing');
    const agreeAge = document.getElementById('agreeAge');

    // 비밀번호 강도 표시
    const passwordStrength = document.getElementById('passwordStrength');

    // 모달 요소들
    const termsModal = document.getElementById('termsModal');
    const modalTitle = document.getElementById('modalTitle');
    const modalContent = document.getElementById('modalContent');
    const closeModal = document.getElementById('closeModal');
    const modalConfirm = document.getElementById('modalConfirm');
    const termsLinks = document.querySelectorAll('.terms-link');

    // 상태 변수
    let verificationTimer = null;
    let timeLeft = 180; // 3분
    let isEmailVerified = false;
    let verificationCode = '';

    // ========== 유효성 검사 함수들 ==========

    // 이름 유효성 검사
    function validateName(name) {
        return name.trim().length >= 2;
    }

    // 이메일 유효성 검사
    function validateEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    // 비밀번호 유효성 검사 및 강도 측정
    function validatePassword(password) {
        const minLength = password.length >= 8;
        const hasLetter = /[a-zA-Z]/.test(password);
        const hasNumber = /\d/.test(password);
        const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);

        const strength = [minLength, hasLetter, hasNumber, hasSpecial].filter(Boolean).length;

        return {
            isValid: minLength && hasLetter && (hasNumber || hasSpecial),
            strength: strength
        };
    }

    // 전화번호 유효성 검사 및 포맷팅
    function validatePhone(phone) {
        const phoneRegex = /^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$/;
        return phoneRegex.test(phone.replace(/-/g, ''));
    }

    // 에러 메시지 표시
    function showError(inputElement, errorElement, message) {
        if (inputElement) inputElement.classList.add('error');
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.classList.add('show');
        }
    }

    // 에러 메시지 숨김
    function hideError(inputElement, errorElement) {
        if (inputElement) inputElement.classList.remove('error');
        if (errorElement) {
            errorElement.textContent = '';
            errorElement.classList.remove('show');
        }
    }

    // 성공 메시지 표시
    function showSuccess(inputElement, successElement, message) {
        if (inputElement) inputElement.classList.add('success');
        if (successElement) {
            successElement.textContent = message;
            successElement.classList.add('show');
        }
    }

    // ========== 이벤트 핸들러 ==========

    // 이름 유효성 검사
    nameInput.addEventListener('blur', function() {
        const name = nameInput.value.trim();
        if (name && !validateName(name)) {
            showError(nameInput, nameError, '이름은 2자 이상 입력해주세요.');
        } else {
            hideError(nameInput, nameError);
        }
    });

    nameInput.addEventListener('input', function() {
        if (nameInput.classList.contains('error')) {
            hideError(nameInput, nameError);
        }
    });

    // 이메일 유효성 검사
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
        // 이메일이 변경되면 인증 초기화
        if (isEmailVerified) {
            isEmailVerified = false;
            verificationGroup.style.display = 'none';
            clearVerificationTimer();
        }
    });

    // 비밀번호 실시간 강도 체크
    passwordInput.addEventListener('input', function() {
        const password = passwordInput.value;
        const result = validatePassword(password);
        
        if (password.length === 0) {
            passwordStrength.style.display = 'none';
            return;
        }

        passwordStrength.style.display = 'block';
        const strengthBar = passwordStrength.querySelector('.strength-bar-fill');
        const strengthText = passwordStrength.querySelector('.strength-text');

        strengthBar.className = 'strength-bar-fill';
        
        if (result.strength <= 2) {
            strengthBar.classList.add('weak');
            strengthText.textContent = '약함';
            strengthText.style.color = '#dc2626';
        } else if (result.strength === 3) {
            strengthBar.classList.add('medium');
            strengthText.textContent = '보통';
            strengthText.style.color = '#f59e0b';
        } else {
            strengthBar.classList.add('strong');
            strengthText.textContent = '강함';
            strengthText.style.color = '#10b981';
        }

        if (passwordInput.classList.contains('error')) {
            hideError(passwordInput, passwordError);
        }
    });

    passwordInput.addEventListener('blur', function() {
        const password = passwordInput.value;
        const result = validatePassword(password);
        
        if (password && !result.isValid) {
            showError(passwordInput, passwordError, '8자 이상, 영문/숫자/특수문자 중 2가지 이상 조합해주세요.');
        } else {
            hideError(passwordInput, passwordError);
        }
    });

    // 비밀번호 확인
    passwordConfirmInput.addEventListener('input', function() {
        if (passwordConfirmInput.classList.contains('error')) {
            hideError(passwordConfirmInput, passwordConfirmError);
        }
    });

    passwordConfirmInput.addEventListener('blur', function() {
        const password = passwordInput.value;
        const passwordConfirm = passwordConfirmInput.value;
        
        if (passwordConfirm && password !== passwordConfirm) {
            showError(passwordConfirmInput, passwordConfirmError, '비밀번호가 일치하지 않습니다.');
        } else {
            hideError(passwordConfirmInput, passwordConfirmError);
        }
    });

    // 전화번호 자동 포맷팅
    phoneInput.addEventListener('input', function() {
        let phone = phoneInput.value.replace(/[^0-9]/g, '');
        
        if (phone.length > 11) {
            phone = phone.slice(0, 11);
        }
        
        if (phone.length > 6) {
            phone = phone.slice(0, 3) + '-' + phone.slice(3, 7) + '-' + phone.slice(7);
        } else if (phone.length > 3) {
            phone = phone.slice(0, 3) + '-' + phone.slice(3);
        }
        
        phoneInput.value = phone;

        if (phoneInput.classList.contains('error')) {
            hideError(phoneInput, phoneError);
        }
    });

    phoneInput.addEventListener('blur', function() {
        const phone = phoneInput.value;
        if (phone && !validatePhone(phone)) {
            showError(phoneInput, phoneError, '올바른 전화번호를 입력해주세요.');
        } else {
            hideError(phoneInput, phoneError);
        }
    });

    // ========== 이메일 인증 ==========

    // 인증번호 발송
    sendVerifyBtn.addEventListener('click', function() {
        const email = emailInput.value.trim();
        
        if (!email) {
            showError(emailInput, emailError, '이메일을 입력해주세요.');
            emailInput.focus();
            return;
        }
        
        if (!validateEmail(email)) {
            showError(emailInput, emailError, '올바른 이메일 형식을 입력해주세요.');
            emailInput.focus();
            return;
        }

        // 서버로 인증번호 발송 요청
        sendVerifyBtn.disabled = true;
        sendVerifyBtn.textContent = '발송중...';

        // 실제 서버 통신 코드
        /*
        fetch('/api/send-verification', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email: email })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                verificationCode = data.code; // 개발용, 실제로는 서버에서만 보관
                showVerificationInput();
            } else {
                showError(emailInput, emailError, data.message || '인증번호 발송에 실패했습니다.');
                sendVerifyBtn.disabled = false;
                sendVerifyBtn.textContent = '인증번호 발송';
            }
        })
        .catch(error => {
            console.error('인증번호 발송 오류:', error);
            showError(emailInput, emailError, '서버 오류가 발생했습니다.');
            sendVerifyBtn.disabled = false;
            sendVerifyBtn.textContent = '인증번호 발송';
        });
        */

        // 데모용 코드
        setTimeout(function() {
            verificationCode = Math.floor(100000 + Math.random() * 900000).toString();
            console.log('인증번호 (데모):', verificationCode);
            showVerificationInput();
            sendVerifyBtn.textContent = '재발송';
            sendVerifyBtn.disabled = false;
        }, 1000);
    });

    // 인증번호 입력창 표시 및 타이머 시작
    function showVerificationInput() {
        verificationGroup.style.display = 'block';
        hideError(emailInput, emailError);
        verificationSuccess.classList.remove('show');
        verificationError.classList.remove('show');
        
        // 입력란 초기화
        codeInputs.forEach(input => {
            input.value = '';
            input.classList.remove('filled');
        });
        codeInputs[0].focus();
        
        // 타이머 시작
        startVerificationTimer();
    }

    // 타이머 시작
    function startVerificationTimer() {
        clearVerificationTimer();
        timeLeft = 180; // 3분
        updateTimerDisplay();
        
        verificationTimer = setInterval(function() {
            timeLeft--;
            updateTimerDisplay();
            
            if (timeLeft <= 0) {
                clearVerificationTimer();
                showError(null, verificationError, '인증 시간이 만료���었습니다. 다시 시도해주세요.');
            }
        }, 1000);
    }

    // 타이머 표시 업데이트
    function updateTimerDisplay() {
        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        timerDisplay.textContent = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
        
        if (timeLeft <= 30) {
            timerDisplay.style.color = '#dc2626';
        } else {
            timerDisplay.style.color = '#f59e0b';
        }
    }

    // 타이머 정리
    function clearVerificationTimer() {
        if (verificationTimer) {
            clearInterval(verificationTimer);
            verificationTimer = null;
        }
    }

    // 인증번호 재발송
    resendBtn.addEventListener('click', function() {
        sendVerifyBtn.click();
    });

    // 인증번호 입력 처리
    codeInputs.forEach((input, index) => {
        // 숫자만 입력
        input.addEventListener('input', function(e) {
            const value = e.target.value;
            
            if (!/^\d$/.test(value)) {
                e.target.value = '';
                return;
            }
            
            e.target.classList.add('filled');
            
            // 다음 입력칸으로 이동
            if (index < codeInputs.length - 1) {
                codeInputs[index + 1].focus();
            }
            
            // 모든 칸이 채워졌는지 확인
            checkVerificationCode();
        });
        
        // 백스페이스 처리
        input.addEventListener('keydown', function(e) {
            if (e.key === 'Backspace') {
                if (e.target.value === '') {
                    if (index > 0) {
                        codeInputs[index - 1].focus();
                        codeInputs[index - 1].value = '';
                        codeInputs[index - 1].classList.remove('filled');
                    }
                } else {
                    e.target.value = '';
                    e.target.classList.remove('filled');
                }
                verificationError.classList.remove('show');
            }
        });
        
        // 붙여넣기 처리
        input.addEventListener('paste', function(e) {
            e.preventDefault();
            const pastedData = e.clipboardData.getData('text').replace(/\D/g, '');
            
            if (pastedData.length === 6) {
                codeInputs.forEach((inp, idx) => {
                    inp.value = pastedData[idx] || '';
                    if (pastedData[idx]) {
                        inp.classList.add('filled');
                    }
                });
                codeInputs[5].focus();
                checkVerificationCode();
            }
        });
    });

    // 인증번호 확인
    function checkVerificationCode() {
        const enteredCode = Array.from(codeInputs).map(input => input.value).join('');
        
        if (enteredCode.length === 6) {
            // 서버로 인증번호 확인 요청
            /*
            fetch('/api/verify-code', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ 
                    email: emailInput.value,
                    code: enteredCode 
                })
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    handleVerificationSuccess();
                } else {
                    handleVerificationError();
                }
            })
            .catch(error => {
                console.error('인증 확인 오류:', error);
                handleVerificationError();
            });
            */
            
            // 데모용 코드
            setTimeout(function() {
                if (enteredCode === verificationCode) {
                    handleVerificationSuccess();
                } else {
                    handleVerificationError();
                }
            }, 500);
        }
    }

    // 인증 성공 처리
    function handleVerificationSuccess() {
        isEmailVerified = true;
        clearVerificationTimer();
        hideError(null, verificationError);
        showSuccess(null, verificationSuccess, '✓ 이메일 인증이 완료되었습니다.');
        
        codeInputs.forEach(input => {
            input.disabled = true;
            input.style.borderColor = '#10b981';
        });
        
        resendBtn.disabled = true;
        emailInput.disabled = true;
    }

    // 인증 실패 처리
    function handleVerificationError() {
        showError(null, verificationError, '인증번호가 일치하지 않습니다.');
        codeInputs.forEach(input => {
            input.value = '';
            input.classList.remove('filled');
        });
        codeInputs[0].focus();
    }

    // ========== 약관 모달 ==========

    // 약관 모달 열기
    termsLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const modalType = link.getAttribute('data-modal');
            openTermsModal(modalType);
        });
    });

    // 약관 모달 열기 함수
    function openTermsModal(type) {
        let title = '';
        let content = '';

        // 실제 프로젝트에서는 서버에서 약관 내용을 가져옵니다
        /*
        fetch(`/api/terms/${type}`)
            .then(response => response.json())
            .then(data => {
                modalTitle.textContent = data.title;
                modalContent.textContent = data.content;
                termsModal.classList.add('active');
            })
            .catch(error => {
                console.error('약관 로드 오류:', error);
                alert('약관을 불러오는데 실패했습니다.');
            });
        */

        // 데모용 약관 내용 (DB에서 불러온다고 가정)
        switch(type) {
            case 'terms':
                title = '이용약관';
                content = `제1조 (목적)
이 약관은 CULTI(이하 "회사")가 제공하는 모든 서비스(이하 "서비스")의 이용조건 및 절차, 회사와 회원 간의 권리, 의무 및 책임사항, 기타 필요한 사항을 규정함을 목적으로 합니다.

제2조 (정의)
1. "서비스"란 회사가 제공하는 영화, 전시, 공연 예매 및 관련된 제반 서비스를 의미합니다.
2. "회원"이란 회사의 서비스에 접속하여 이 약관에 따라 회사와 이용계약을 체결하고 회사가 제공하는 서비스를 이용하는 고객을 말합니다.
3. "아이디(ID)"란 회원의 식별과 서비스 이용을 위하여 회원이 정하고 회사가 승인하는 문자와 숫자의 조합을 의미합니다.
4. "비밀번호"란 회원이 부여받은 아이디와 일치되는 회원임을 확인하고 비밀보호를 위해 회원 자신이 정한 문자 또는 숫자의 조합을 의미합니다.

제3조 (약관의 게시와 개정)
1. 회사는 이 약관의 내용을 회원이 쉽게 알 수 있도록 서비스 초기 화면에 게시합니다.
2. 회사는 관련 법령을 위배하지 않는 범위에서 이 약관을 개정할 수 있습니다.
3. 회사가 약관을 개정할 경우에는 적용일자 및 개정사유를 명시하여 현행약관과 함께 제1항의 방식에 따라 그 개정약관의 적용일자 7일 전부터 적용일자 전일까지 공지합니다.

제4조 (회원가입)
1. 회원가입은 신청자가 온라인으로 회사에서 제공하는 소정의 가입신청 양식에서 요구하는 사항을 기록하여 가입을 완료하는 것으로 성립됩니다.
2. 회사는 다음 각 호에 해당하는 경우에 대해서는 그 신청에 대한 승낙을 제한할 수 있고, 그 사유가 해소될 때까지 승낙을 유보할 수 있습니다.
   가. 실명이 아니거나 타인의 명의를 이용하여 신청한 경우
   나. 허위의 정보를 기재하거나, 회사가 요구하는 내용을 기재하지 않은 경우
   다. 만 14세 미만이 신청한 경우

제5조 (서비스의 제공 및 변경)
1. 회사는 다음과 같은 서비스를 제공합니다.
   가. 영화, 전시, 공연 예매 서비스
   나. 문화 콘텐츠 정보 제공 서비스
   다. 기타 회사가 추가 개발하거나 다른 회사와의 제휴계약 등을 통해 회원에게 제공하는 일체의 서비스
2. 회사는 서비스의 내용을 변경할 수 있으며, 변경된 서비스 내용을 사전에 공지합니다.

제6조 (서비스의 중단)
1. 회사는 컴퓨터 등 정보통신설비의 보수점검, 교체 및 고장, 통신의 두절 등의 사유가 발생한 경우에는 서비스의 제공을 일시적으로 중단할 수 있습니다.
2. 회사는 제1항의 ��유로 서비스의 제공이 일시적으로 중단됨으로 인하여 이용자 또는 제3자가 입은 손해에 대하여 배상합니다. 단, 회사가 고의 또는 과실이 없음을 입증하는 경우에는 그러하지 아니합니다.

제7조 (회원탈퇴 및 자격 상실)
1. 회원은 회사에 언제든지 탈퇴를 요청할 수 있으며 회사는 즉시 회원탈퇴를 처리합니다.
2. 회원이 다음 각 호의 사유에 해당하는 경우, 회사는 회원자격을 제한 및 정지시킬 수 있습니다.
   가. 가입 신청 시에 허위 내용을 등록한 경우
   나. 다른 사람의 서비스 이용을 방해하거나 그 정보를 도용하는 등 전자상거래 질서를 위협하는 경우
   다. 서비스를 이용하여 법령 또는 이 약관이 금지하거나 공서양속에 반하는 행위를 하는 경우

부칙
이 약관은 2026년 2월 21일부터 적용됩니다.`;
                break;
            case 'privacy':
                title = '개인정보 수집 및 이용 동의';
                content = `CULTI(이하 "회사")는 개인정보 보호법에 따라 이용자의 개인정보 보호 및 권익을 보호하고 개인정보와 관련한 이용자의 고충을 원활하게 처리할 수 있도록 다음과 같은 처리방침을 두고 있습니다.

1. 개인정보의 수집 및 이용 목적
회사는 수집한 개인정보를 다음의 목적을 위해 활용합니다.
가. 서비스 제공에 관한 계약 이행 및 서비스 제공에 따른 요금정산
   - 콘텐츠 제공, 예매 서비스 제공, 구매 및 요금 결제, 물품배송 또는 청구서 등 발송
나. 회원 관리
   - 회원제 서비스 이용에 따른 본인확인, 개인식별, 불량회원의 부정 이용 방지와 비인가 사용 방지, 가입 의사 확인, 연령확인, 불만처리 등 민원처리, 고지사항 전달
다. 마케팅 및 광고에 활용
   - 이벤트 등 광고성 정보 전달, 접속 빈도 파악, 회원의 서비스 이용에 대한 통계

2. 수집하는 개인정보의 항목
회사는 회원가입, 서비스 이용 등을 위해 아래와 같은 개인정보를 수집하고 있습니다.
가. 필수항목: 이름, 이메일 주소, 비밀번호, 전화번호
나. 선택항목: 생년월일, 성별
다. 자동 수집 항목: IP주소, 쿠키, 서비스 이용 기록, 방문 기록

3. 개인정보의 보유 및 이용기간
회사는 개인정보 수집 및 이용목적이 달성된 후에는 예외 없이 해당 정보를 지체 없이 파기합니다.
가. 회원가입정보: 회원 탈퇴 시까지
나. 예매 정보: 예매 완료 후 5년간 보관
다. 부정이용 기록: 부정 이용 방지를 위해 수집일로부터 1년간 보관

4. 개인정보의 파기절차 및 방법
회사는 원칙적으로 개인정보 수집 및 이용목적이 달성된 후에는 해당 정보를 지체없이 파기합니다.
가. 파기절차
   - 회원님이 회원가입 등을 위해 입력하신 정보는 목적이 달성된 후 별도의 DB로 옮겨져(종이의 경우 별도의 서류함) 내부 방침 및 기타 관련 법령에 의한 정보보호 사유에 따라 일정 기간 저장된 후 파기됩니다.
나. 파기방법
   - 종이에 출력된 개인정보는 분쇄기로 분쇄하거나 소각을 통하여 파기합니다.
   - 전자적 파일 형태로 저장된 개인정보는 기록을 재생할 수 없는 기술적 방법을 사용하여 삭제합니다.

5. 개인정보 제공
회사는 이용자의 개인정보를 원칙적으로 외부에 제공하지 않습니다. 다만, 아래의 경우에는 예외로 합니다.
가. 이용자들이 사전에 동의한 경우
나. 법령의 규정에 의거하거나, 수사 목적으로 법령에 정해진 절차와 방법에 따라 수사기관의 요구가 있는 경우

6. 개인정보의 안전성 확보조치
회사는 개인정보의 안전성 확보를 위해 다음과 같은 조치를 취하고 있습니다.
가. 관리적 조치: 내부관리계획 수립 및 시행, 정기적 직원 교육
나. 기술적 조치: 개인정보처리시스템 등의 접근권한 관리, 접근통제시스템 설치, 고유식별정보 등의 암호화, 보안프로그램 설치
다. 물리적 조치: 전산실, 자료보관실 등의 접근통제

7. 개인정보 보호책임자
회사는 개인정보 처리에 관한 업무를 총괄해서 책임지고, 개인정보 처리와 관련한 정보주체의 불만처리 및 피해구제 등을 위하여 아래와 같이 개인정보 보호책임자를 지정하고 있습니다.
- 개인정보 보호책임자: 홍길동
- 이메일: privacy@culti.com
- 전화번호: 02-1234-5678

시행일자: 2026년 2월 21일`;
                break;
            case 'marketing':
                title = '마케팅 정보 수신 동의';
                content = `CULTI(이하 "회사")는 회원님께 다양한 혜택과 이벤트 정보를 제공하기 위하여 아래와 같이 마케팅 활용에 대한 동의를 받고 있습니다.

1. 마케팅 활용 목적
회사는 수집한 개인정보를 다음과 같은 목적으로 활용합니다.
가. 신규 서비스 및 이벤트 정보 안내
나. 맞춤형 서비스 및 상품 추천
다. 할인 쿠폰 및 프로모션 정보 제공
라. 참여 이벤트 및 경품 행사 안내

2. 수신 정보의 종류
가. 이메일: 이벤트, 프로모션, 신규 서비스 안내
나. SMS/MMS: 예매 정보, 할인 쿠폰, 긴급 공지사항
다. 앱 푸시 알림: 실시간 이벤트, 관심 콘텐츠 추천

3. 정보 발송 주체
가. 발송 주체: CULTI
나. 발송 빈도: 주 1~2회 (이벤트 및 프로모션에 따라 변동 가능)

4. 개인정보의 보유 및 이용기간
마케팅 활용 동의일로부터 회원 탈퇴 시 또는 동의 철회 시까지

5. 동의 거부권 및 불이익
귀하는 위와 같은 마케팅 정보 수신에 대한 동의를 거부할 수 있습니다.
다만, 동의를 거부하시는 경우 각종 이벤트 및 프로모션 안내를 받으실 수 없습니다.
동의하지 않으셔도 CULTI의 기본 서비스 이용에는 제한이 없습니다.

6. 수신 동의 변경
회원님은 언제든지 마이페이지에서 마케팅 정보 수신 동의를 변경하실 수 있습니다.
가. 웹사이트: 마이페이지 > 설정 > 알림설정
나. 이메일: 수신 거부 링크 클릭
다. 고객센터: 1234-5678 (평일 09:00~18:00)

7. 개인정보 보호책임자
- 이름: 홍길동
- 이메일: privacy@culti.com
- 전화번호: 02-1234-5678

본 동의는 선택사항이며, 동의하지 않으셔도 서비스 이용이 가능합니다.

시행일자: 2026년 2월 21일`;
                break;
            default:
                title = '약관';
                content = '약관 내용을 불러오는 중 오류가 발생했습니다.';
        }

        // 모달에 내용 표시
        modalTitle.textContent = title;
        modalContent.textContent = content;
        termsModal.classList.add('active');
        
        // body 스크롤 방지
        document.body.style.overflow = 'hidden';
    }

    // 약관 모달 닫기
    function closeTermsModal() {
        termsModal.classList.remove('active');
        document.body.style.overflow = '';
    }

    closeModal.addEventListener('click', closeTermsModal);
    modalConfirm.addEventListener('click', closeTermsModal);

    // 모달 외부 클릭 시 닫기
    termsModal.addEventListener('click', function(e) {
        if (e.target === termsModal) {
            closeTermsModal();
        }
    });

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && termsModal.classList.contains('active')) {
            closeTermsModal();
        }
    });

    // ========== 약관 동의 ==========

    // 전체 동의 체크박스
    agreeAll.addEventListener('change', function() {
        const isChecked = agreeAll.checked;
        agreeTerms.checked = isChecked;
        agreePrivacy.checked = isChecked;
        agreeMarketing.checked = isChecked;
        agreeAge.checked = isChecked;
        hideError(null, termsError);
    });

    // 개별 체크박스
    [agreeTerms, agreePrivacy, agreeMarketing, agreeAge].forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            const allChecked = agreeTerms.checked && agreePrivacy.checked && 
                             agreeMarketing.checked && agreeAge.checked;
            agreeAll.checked = allChecked;
            hideError(null, termsError);
        });
    });

    // ========== 폼 제출 ==========

    signupForm.addEventListener('submit', function(e) {
        e.preventDefault();

        // 모든 에러 메시지 초기화
        document.querySelectorAll('.error-message').forEach(el => el.classList.remove('show'));
        document.querySelectorAll('input').forEach(el => el.classList.remove('error'));

        let isValid = true;
        let firstErrorField = null;

        // 이름 검증
        const name = nameInput.value.trim();
        if (!name) {
            showError(nameInput, nameError, '이름을 입력해주세요.');
            isValid = false;
            if (!firstErrorField) firstErrorField = nameInput;
        } else if (!validateName(name)) {
            showError(nameInput, nameError, '이름은 2자 이상 입력해주세요.');
            isValid = false;
            if (!firstErrorField) firstErrorField = nameInput;
        }

        // 이메일 검증
        const email = emailInput.value.trim();
        if (!email) {
            showError(emailInput, emailError, '이메일을 입력해주세요.');
            isValid = false;
            if (!firstErrorField) firstErrorField = emailInput;
        } else if (!validateEmail(email)) {
            showError(emailInput, emailError, '올바른 이메일 형식을 입력해주세요.');
            isValid = false;
            if (!firstErrorField) firstErrorField = emailInput;
        } else if (!isEmailVerified) {
            showError(emailInput, emailError, '이메일 인증을 완료해주세요.');
            isValid = false;
            if (!firstErrorField) firstErrorField = emailInput;
        }

        // 비밀번호 검증
        const password = passwordInput.value;
        const result = validatePassword(password);
        if (!password) {
            showError(passwordInput, passwordError, '비밀번호를 입력해주세요.');
            isValid = false;
            if (!firstErrorField) firstErrorField = passwordInput;
        } else if (!result.isValid) {
            showError(passwordInput, passwordError, '8자 이상, 영문/숫자/특수문자 중 2가지 이상 조합해주세요.');
            isValid = false;
            if (!firstErrorField) firstErrorField = passwordInput;
        }

        // 비밀번호 확인 검증
        const passwordConfirm = passwordConfirmInput.value;
        if (!passwordConfirm) {
            showError(passwordConfirmInput, passwordConfirmError, '비밀번호 확인을 입력해주세요.');
            isValid = false;
            if (!firstErrorField) firstErrorField = passwordConfirmInput;
        } else if (password !== passwordConfirm) {
            showError(passwordConfirmInput, passwordConfirmError, '비밀번호가 일치하지 않습니다.');
            isValid = false;
            if (!firstErrorField) firstErrorField = passwordConfirmInput;
        }

        // 전화번호 검증
        const phone = phoneInput.value;
        if (!phone) {
            showError(phoneInput, phoneError, '전화번호를 입력해주세요.');
            isValid = false;
            if (!firstErrorField) firstErrorField = phoneInput;
        } else if (!validatePhone(phone)) {
            showError(phoneInput, phoneError, '올바른 전화번호를 입력해주세요.');
            isValid = false;
            if (!firstErrorField) firstErrorField = phoneInput;
        }

        // 필수 약관 검증
        const allRequiredChecked = Array.from(requiredTerms).every(checkbox => checkbox.checked);
        if (!allRequiredChecked) {
            showError(null, termsError, '필수 약관에 동의해주세요.');
            isValid = false;
            if (!firstErrorField) firstErrorField = agreeTerms;
        }

        if (!isValid) {
            if (firstErrorField) {
                firstErrorField.focus();
            }
            return;
        }

        // 로딩 상태 표시
        signupBtn.classList.add('loading');
        signupBtn.disabled = true;

        // 서버로 전송할 데이터
        const signupData = {
            name: name,
            email: email,
            password: password,
            phone: phone,
            birthdate: document.getElementById('birthdate').value,
            gender: document.querySelector('input[name="gender"]:checked')?.value,
            agreeMarketing: agreeMarketing.checked
        };

        console.log('회원가입 데이터:', signupData);

        // 실제 서버 통신 코드
        /*
        fetch('/api/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(signupData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // 회원가입 성공
                window.location.href = '/signup-complete.html';
            } else {
                // 회원가입 실패
                alert(data.message || '회원가입에 실패했습니다.');
                signupBtn.classList.remove('loading');
                signupBtn.disabled = false;
            }
        })
        .catch(error => {
            console.error('회원가입 오류:', error);
            alert('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
            signupBtn.classList.remove('loading');
            signupBtn.disabled = false;
        });
        */

        // 데모용 타임아웃
        setTimeout(function() {
            signupBtn.classList.remove('loading');
            signupBtn.disabled = false;
            window.location.href = '/signup-complete.html';
        }, 2000);
    });

    // 페이지 벗어날 때 타이머 정리
    window.addEventListener('beforeunload', function() {
        clearVerificationTimer();
    });
});