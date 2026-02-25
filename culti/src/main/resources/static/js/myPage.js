// DOM이 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
    // 사이드바 네비게이션
    const navItems = document.querySelectorAll('.nav-item');
    const tabContents = document.querySelectorAll('.tab-content');

    // 필터 탭
    const filterTabs = document.querySelectorAll('.filter-tab');
    
    // 쿠폰/포인트 탭
    const benefitTabs = document.querySelectorAll('.benefit-tab');
    const benefitSections = document.querySelectorAll('.benefit-section');

    // 로그아웃 버튼
    const logoutBtn = document.getElementById('logoutBtn');

    // 회원 탈퇴 관련
    const deleteAccountBtn = document.getElementById('deleteAccountBtn');
    const deleteModal = document.getElementById('deleteModal');
    const closeDeleteModal = document.getElementById('closeDeleteModal');
    const cancelDelete = document.getElementById('cancelDelete');
    const confirmDelete = document.getElementById('confirmDelete');

    // 프로필 폼
    const profileForm = document.getElementById('profileForm');
    const cancelEdit = document.getElementById('cancelEdit');

    // ========== 네비게이션 ==========
    navItems.forEach(item => {
        item.addEventListener('click', function() {
            const targetTab = this.getAttribute('data-tab');
            
            // 활성 상태 변경
            navItems.forEach(nav => nav.classList.remove('active'));
            this.classList.add('active');
            
            // 탭 컨텐츠 전환
            tabContents.forEach(content => content.classList.remove('active'));
            const targetContent = document.getElementById(targetTab);
            if(targetContent) {
                targetContent.classList.add('active');
            }
        });
    });

    // ========== 필터 탭 (예매 내역) ==========
    filterTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const filter = this.getAttribute('data-filter');
            
            filterTabs.forEach(t => t.classList.remove('active'));
            this.classList.add('active');
            
            // 필터링 로직 (서버에서 데이터 가져오기)
            filterReservations(filter);
        });
    });

    function filterReservations(filter) {
        // 실제로는 서버에서 필터링된 데이터를 가져옴
        console.log('필터:', filter);
    }

    // ========== 쿠폰/포인트 탭 ==========
    benefitTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const benefitType = this.getAttribute('data-benefit');
            
            benefitTabs.forEach(t => t.classList.remove('active'));
            this.classList.add('active');
            
            benefitSections.forEach(section => section.classList.remove('active'));
            document.getElementById(benefitType).classList.add('active');
        });
    });

    // ========== 데이터 로딩 및 렌더링 ==========

    // 사용자 정보 로드
	/*
    function loadUserInfo() {
        // 데모용
        const userData = {
            name: '홍길동',
            nickname: '길동이',
            email: 'hong@example.com',
            phone: '010-1234-5678',
            birthdate: '1990-01-01',
            gender: 'male'
        };
        
        document.getElementById('userName').textContent = userData.name;
        document.getElementById('userEmail').textContent = userData.email;

        // 폼 초기값 설정
        const nameInput = document.getElementById('profileName');
        if(nameInput) nameInput.value = userData.name;

        const nicknameInput = document.getElementById('profileNickname');
        if(nicknameInput) nicknameInput.value = userData.nickname;

        const emailInput = document.getElementById('profileEmail');
        if(emailInput) emailInput.value = userData.email;

        const phoneInput = document.getElementById('profilePhone');
        if(phoneInput) phoneInput.value = userData.phone;

        const birthInput = document.getElementById('profileBirth');
        if(birthInput) birthInput.value = userData.birthdate;

        if(userData.gender) {
            const radio = document.querySelector(`input[name="gender"][value="${userData.gender}"]`);
            if(radio) radio.checked = true;
        }
    }*/

    // 예매 내역 렌더링
    function renderReservations(reservations, containerId) {
        const container = document.getElementById(containerId);
        if(!container) return;
        
        if (reservations.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="16" y1="2" x2="16" y2="6"></line>
                        <line x1="8" y1="2" x2="8" y2="6"></line>
                        <line x1="3" y1="10" x2="21" y2="10"></line>
                    </svg>
                    <h3>예매 내역이 없습니다</h3>
                    <p>첫 예매를 시작해보세요!</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = reservations.map(item => `
            <div class="reservation-item">
                <div class="reservation-image">
                    <img src="${item.image}" alt="${item.title}">
                </div>
                <div class="reservation-info">
                    <span class="reservation-category">${item.category}</span>
                    <h3 class="reservation-title">${item.title}</h3>
                    <div class="reservation-details">
                        <p>예매번호: ${item.reservationNumber}</p>
                        <p>관람일시: ${item.date} ${item.time}</p>
                        <p>좌석: ${item.seat}</p>
                        <p>결제금액: ${item.price.toLocaleString()}원</p>
                    </div>
                </div>
                <div class="reservation-actions">
                    ${item.status === 'upcoming' ? `
                        <button class="btn-small primary" onclick="viewTicket('${item.id}')">티켓 보기</button>
                        <button class="btn-small secondary" onclick="cancelReservation('${item.id}')">예매 취소</button>
                    ` : `
                        <button class="btn-small secondary" onclick="writeReview('${item.id}')">리뷰 작성</button>
                    `}
                </div>
            </div>
        `).join('');
    }

    // 취소/환불 내역 렌더링
    function renderCancellations(cancellations) {
        const container = document.getElementById('cancellationList');
        if (!container) return;

        if (cancellations.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                     <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="15" y1="9" x2="9" y2="15"></line>
                        <line x1="9" y1="9" x2="15" y2="15"></line>
                    </svg>
                    <h3>취소/환불 내역이 없습니다</h3>
                </div>
            `;
            return;
        }

        container.innerHTML = cancellations.map(item => `
            <div class="reservation-item canceled">
                <div class="reservation-image">
                    <img src="${item.image}" alt="${item.title}">
                    <div class="cancel-overlay">취소됨</div>
                </div>
                <div class="reservation-info">
                    <span class="reservation-category">${item.category}</span>
                    <h3 class="reservation-title">${item.title}</h3>
                    <div class="reservation-details">
                        <p>예매번호: ${item.reservationNumber}</p>
                        <p>취소일시: ${item.cancelDate}</p>
                        <p>환불금액: ${item.price.toLocaleString()}원</p>
                    </div>
                </div>
                <div class="reservation-actions">
                     <button class="btn-small secondary" disabled>환불 완료</button>
                </div>
            </div>
        `).join('');
    }

    // 쿠폰 렌더링
    function renderCoupons(coupons) {
        const container = document.getElementById('couponList');
        if(!container) return;
        
        if (coupons.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"></circle>
                        <path d="M12 6v6l4 2"></path>
                    </svg>
                    <h3>보유 쿠폰이 없습니다</h3>
                    <p>이벤트를 통해 쿠폰을 받아보세요!</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = coupons.map(coupon => `
            <div class="coupon-card">
                <div class="coupon-header">
                    <div class="coupon-discount">${coupon.discount}</div>
                    <span class="coupon-type">${coupon.type}</span>
                </div>
                <h4 class="coupon-name">${coupon.name}</h4>
                <p class="coupon-description">${coupon.description}</p>
                <p class="coupon-expiry">유효기간: ${coupon.expiry}</p>
            </div>
        `).join('');
    }

    // 포인트 내역 렌더링
    function renderPoints(points) {
        const container = document.getElementById('pointList');
        if(!container) return;
        
        if (points.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <h3>포인트 내역이 없습니다</h3>
                </div>
            `;
            return;
        }
        
        container.innerHTML = points.map(point => `
            <div class="point-item">
                <div class="point-item-info">
                    <span class="point-item-title">${point.title}</span>
                    <span class="point-item-date">${point.date}</span>
                </div>
                <span class="point-item-amount ${point.type}">${point.type === 'plus' ? '+' : '-'}${point.amount.toLocaleString()}P</span>
            </div>
        `).join('');
    }

    // 찜 목록 렌더링
    function renderFavorites(favorites) {
        const container = document.getElementById('favoritesList');
        if(!container) return;
        
        if (favorites.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                    </svg>
                    <h3>찜한 콘텐츠가 없습니다</h3>
                    <p>마음에 드는 콘텐츠를 찜해보세요!</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = favorites.map(item => `
            <div class="favorite-item" onclick="goToDetail('${item.id}')">
                <div class="favorite-image">
                    <img src="${item.image}" alt="${item.title}">
                    <div class="favorite-heart" onclick="event.stopPropagation(); toggleFavorite('${item.id}')">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" stroke-width="2">
                            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                        </svg>
                    </div>
                </div>
                <div class="favorite-info">
                    <h4 class="favorite-title">${item.title}</h4>
                    <p class="favorite-date">${item.period}</p>
                </div>
            </div>
        `).join('');
    }

    // ========== 초기 데이터 로드 ==========
    
    // 데모 데이터
    const demoReservations = [
        {
            id: '1',
            category: '영화',
            title: '오펜하이머',
            reservationNumber: 'R20260222001',
            date: '2026.03.01',
            time: '14:00',
            seat: 'A열 5번',
            price: 15000,
            image: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=300&h=400&fit=crop',
            status: 'upcoming'
        },
        {
            id: '2',
            category: '전시',
            title: '모네 특별전',
            reservationNumber: 'R20260222002',
            date: '2026.02.28',
            time: '10:00',
            seat: '일반 1매',
            price: 20000,
            image: 'https://images.unsplash.com/photo-1547891654-e66ed7ebb968?w=300&h=400&fit=crop',
            status: 'upcoming'
        },
        {
            id: '3',
            category: '공연',
            title: '오페라의 유령',
            reservationNumber: 'R20260215001',
            date: '2026.02.15',
            time: '19:00',
            seat: 'R석 12번',
            price: 80000,
            image: 'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?w=300&h=400&fit=crop',
            status: 'completed'
        }
    ];

    const demoCancellations = [
        {
            id: 'c1',
            category: '영화',
            title: '파묘',
            reservationNumber: 'R20260115003',
            date: '2026.01.20',
            time: '18:30',
            seat: 'H열 12번',
            price: 14000,
            image: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=300&h=400&fit=crop',
            status: 'canceled',
            cancelDate: '2026.01.18'
        },
        {
             id: 'c2',
             category: '공연',
             title: '노트르담 드 파리',
             reservationNumber: 'R20260110005',
             date: '2026.02.05',
             time: '19:00',
             seat: 'VIP석 2매',
             price: 320000,
             image: 'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?w=300&h=400&fit=crop',
             status: 'canceled',
             cancelDate: '2026.01.25'
        }
    ];

    const demoCoupons = [
        {
            discount: '5,000원',
            type: '금액',
            name: '신규가입 환영 쿠폰',
            description: '전 상품 사용 가능',
            expiry: '2026.03.31까지'
        },
        {
            discount: '10%',
            type: '할인',
            name: '영화 특가 쿠폰',
            description: '영화 예매 시 10% 할인',
            expiry: '2026.02.28까지'
        }
    ];

    const demoPoints = [
        {
            title: '오페라의 유령 예매',
            date: '2026.02.15',
            amount: 4000,
            type: 'plus'
        },
        {
            title: '모네 특별전 예매',
            date: '2026.02.20',
            amount: 1000,
            type: 'plus'
        }
    ];

    const demoFavorites = [
        {
            id: '1',
            title: '듄: 파트 2',
            period: '2026.03.01 - 상영중',
            image: 'https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=300&h=400&fit=crop'
        },
        {
            id: '2',
            title: '인상주의 미술 전시',
            period: '2026.01.15 - 2026.04.30',
            image: 'https://images.unsplash.com/photo-1577083165633-14ebcdb0f658?w=300&h=400&fit=crop'
        }
    ];

    // 초기 렌더링
    loadUserInfo();
    renderReservations(demoReservations.slice(0, 2), 'recentReservations');
    renderReservations(demoReservations, 'allReservations');
    renderCancellations(demoCancellations);
    renderCoupons(demoCoupons);
    renderPoints(demoPoints);
    renderFavorites(demoFavorites);

    // ========== 프로필 수정 ==========
    
    profileForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const formData = {
            name: document.getElementById('profileName').value,
            nickname: document.getElementById('profileNickname').value,
            phone: document.getElementById('profilePhone').value,
            birthdate: document.getElementById('profileBirth').value,
            gender: document.querySelector('input[name="gender"]:checked')?.value,
            currentPassword: document.getElementById('currentPassword').value,
            newPassword: document.getElementById('newPassword').value,
            confirmPassword: document.getElementById('confirmPassword').value
        };

        // 비밀번호 변경 시 유효성 검사
        if (formData.newPassword) {
            if (!formData.currentPassword) {
                alert('현재 비밀번호를 입력해주세요.');
                return;
            }
            if (formData.newPassword !== formData.confirmPassword) {
                alert('새 비밀번호가 일치하지 않습니다.');
                return;
            }
        }

        // 데모용
        alert('회원정보가 수정되었습니다.');
        console.log('수정 데이터:', formData);
        
        // 화면 업데이트
        document.getElementById('userName').textContent = formData.name;
    });

    cancelEdit.addEventListener('click', function() {
        if (confirm('수정을 취소하시겠습니까?')) {
            profileForm.reset();
            loadUserInfo();
        }
    });

    // ========== 로그아웃 ==========
    
    logoutBtn.addEventListener('click', function() {
        if (confirm('로그아웃 하시겠습니까?')) {
            window.location.href = '/login.html';
        }
    });

    // ========== 회원 탈퇴 ==========
    
    deleteAccountBtn.addEventListener('click', function() {
        deleteModal.classList.add('active');
        document.body.style.overflow = 'hidden';
    });

    function closeDeleteAccountModal() {
        deleteModal.classList.remove('active');
        document.body.style.overflow = '';
        document.getElementById('deletePassword').value = '';
    }

    closeDeleteModal.addEventListener('click', closeDeleteAccountModal);
    cancelDelete.addEventListener('click', closeDeleteAccountModal);

    confirmDelete.addEventListener('click', function() {
        const password = document.getElementById('deletePassword').value;
        
        if (!password) {
            alert('비밀번호를 입력해주세요.');
            return;
        }

        // 데모용
        if (password === 'demo') {
            alert('회원 탈퇴가 완료되었습니다.');
            window.location.href = '/';
        } else {
            alert('비밀번호가 일치하지 않습니다.');
        }
    });

    // 모달 외부 클릭 시 닫기
    deleteModal.addEventListener('click', function(e) {
        if (e.target === deleteModal) {
            closeDeleteAccountModal();
        }
    });

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && deleteModal.classList.contains('active')) {
            closeDeleteAccountModal();
        }
    });
});

// ========== 전역 함수 (HTML에서 호출) ==========

// 티켓 보기
function viewTicket(id) {
    console.log('티켓 보기:', id);
    alert('티켓 상세 페이지로 이동합니다.');
}

// 예매 취소
function cancelReservation(id) {
    if (confirm('예매를 취소하시겠습니까?')) {
        console.log('예매 취소:', id);
        alert('예매가 취소되었습니다.');
    }
}

// 리뷰 작성
function writeReview(id) {
    console.log('리뷰 작성:', id);
    alert('리뷰 작성 페이지로 이동합니다.');
}

// 찜 토글
function toggleFavorite(id) {
    console.log('찜 토글:', id);
    alert('찜 목록에서 삭제되었습니다.');
}

// 상세 페이지로 이동
function goToDetail(id) {
    console.log('상세 페이지로 이동:', id);
    alert('상세 페이지로 이동합니다.');
}