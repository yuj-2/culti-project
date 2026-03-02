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

	// ========== 약관 동의 요소들 수정 ==========
	const agreeAll = document.getElementById('agreeAll');
	// 동적으로 생성된 모든 약관 체크박스 (name으로 타겟팅)
	const termCheckboxes = document.querySelectorAll('input[name="agreedTerms"]');
	// 필수 약관들 (class로 타겟팅)
	const requiredTerms = document.querySelectorAll('.required-term');
	const agreeAge = document.getElementById('agreeAge');

    // 비밀번호 강도 표시
    const passwordStrength = document.getElementById('passwordStrength');

    // 모달 요소들
    const termsModal = document.getElementById('termsModal');
    const modalTitle = document.getElementById('modalTitle');
    const modalContent = document.getElementById('modalContent');
    const closeModal = document.getElementById('closeModal');
    const modalConfirm = document.getElementById('modalConfirm');

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

		
		
		const emailData = document.getElementById('email').value;
		const token = document.querySelector("meta[name='_csrf']").content;
		const header = document.querySelector("meta[name='_csrf_header']").content;
		
		fetch('/api/auth/send', {
		  method: 'POST',
		  headers: {
		    'Content-Type': 'application/json', // 서버의 @RequestBody가 인식할 수 있게 설정 
			[header]: token 
			},
			body: JSON.stringify({ 
			        email: emailData // key값을 'email'로 지정
			    })
		})
		.then(response => response.text()) // 서버 응답을 텍스트로 받기
		.then(data => {
		  console.log("서버 응답:", data);
		  alert("인증번호가 발송되었습니다!");
		})
		.catch(error => console.error('에러 발생:', error));
		
		

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
		const token = document.querySelector("meta[name='_csrf']").content;
		const header = document.querySelector("meta[name='_csrf_header']").content;
        if (enteredCode.length === 6) {
            // 서버로 인증번호 확인 요청
            
            fetch('/api/auth/verify', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
					[header]: token
                },
                body: JSON.stringify({ 
                    email: emailInput.value,
                    inputAuthCode: enteredCode 
                })
            })
            .then(response => response.json())
            .then(data => {
                if (data==true) {
                    handleVerificationSuccess();
                } else {
                    handleVerificationError();
                }
            })
            .catch(error => {
                console.error('인증 확인 오류:', error);
                handleVerificationError();
            });
            
            
            // 데모용 코드
			/*
            setTimeout(function() {
                if (enteredCode === verificationCode) {
                    handleVerificationSuccess();
                } else {
                    handleVerificationError();
                }
            }, 500);
			*/
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
        emailInput.readOnly = true;
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

	// 약관 모달 열기 이벤트 바인딩
	const termsLinks = document.querySelectorAll('.terms-link');
	termsLinks.forEach(link => {
	    link.addEventListener('click', function(e) {
	        e.preventDefault();
	        const termId = this.getAttribute('data-term-id');
	        const title = this.parentElement.querySelector('span').textContent;
	        // 숨겨둔 hidden div에서 본문 가져오기
	        const content = document.getElementById('term-content-' + termId).textContent;
	        
	        openTermsModal(title, content);
	    });
	});

	// 모달 열기 함수 (내용을 직접 받도록 수정)
	function openTermsModal(title, content) {
	    modalTitle.textContent = title;
	    modalContent.textContent = content;
	    termsModal.classList.add('active');
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
	    // 모든 약관 체크박스 + 만 14세 이상 체크박스 조절
		
		// 이벤트 발생 시점에 다시 한 번 체크박스들을 찾습니다 (동적 생성 대응)
		const currentTerms = document.querySelectorAll('input[name="agreedTermIds"]');
	    currentTerms.forEach(cb => cb.checked = isChecked);
	    if(agreeAge) agreeAge.checked = isChecked;
	    hideError(null, termsError);
	});

	const termsGroup = document.querySelector('.terms-group');

	if (termsGroup) {
	    termsGroup.addEventListener('change', function(e) {
	        // 클릭된 애가 약관 체크박스이거나 '만 14세' 체크박스인 경우만 실행
	        if (e.target.name === 'agreedTermIds' || e.target.id === 'agreeAge') {
	            
	            // 현재 화면에 존재하는 모든 체크박스를 실시간으로 수집
	            const currentTerms = document.querySelectorAll('input[name="agreedTermIds"]');
	            const termList = Array.from(currentTerms);
	            if(agreeAge) termList.push(agreeAge);

	            // 모든 체크박스가 체크되어 있는지 검사 (하나라도 풀리면 false)
	            const isAllChecked = termList.every(cb => cb.checked);
	            
	            // 전체 동의 체크박스 상태 업데이트
	            agreeAll.checked = isAllChecked;
	            hideError(null, termsError);
	        }
	    });
	}

    // ========== 폼 제출 ==========
/*
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


    });*/

    // 페이지 벗어날 때 타이머 정리
    window.addEventListener('beforeunload', function() {
        clearVerificationTimer();
    });
});