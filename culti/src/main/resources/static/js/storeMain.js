/**
 * CULTI 매점 메인 스크립트
 * 주요 기능: 카테고리별 상품 로드, 장바구니 담기, 토스트 알림
 */

document.addEventListener('DOMContentLoaded', function() {
    // 1. 초기 로드
    loadProducts(1);

    // 2. 카테고리 탭 이벤트 바인딩
    const tabButtons = document.querySelectorAll('.category-tabs button');
    tabButtons.forEach((btn, index) => {
        btn.addEventListener('click', function() {
            changeCategory(index + 1, this);
        });
    });
});

/**
 * 카테고리 변경 함수 (전역 유지)
 */
function changeCategory(catId, element) {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    element.classList.add('active');
    loadProducts(catId);
}

/**
 * 서버로부터 상품 리스트를 가져오는 함수
 */
function loadProducts(sCatId) {
    const gridContainer = document.querySelector('.product-grid');
    gridContainer.innerHTML = '<div class="loading" style="text-align:center; padding:50px;">CULTI 메뉴를 준비 중입니다...</div>';

    fetch(`/store/api/products?sCatId=${sCatId}`) 
        .then(response => {
            if (!response.ok) throw new Error('네트워크 응답 에러');
            return response.json();
        })
        .then(data => {
            renderProducts(data);
        })
        .catch(error => {
            console.error('Error:', error);
            gridContainer.innerHTML = '<div class="error" style="text-align:center; padding:50px;">상품을 불러오는 데 실패했습니다.</div>';
        });
}

/**
 * 데이터를 화면에 그리는 함수
 */
function renderProducts(products) {
    const gridContainer = document.querySelector('.product-grid');
    gridContainer.innerHTML = ''; 

    if (products.length === 0) {
        gridContainer.innerHTML = '<div class="empty" style="text-align:center; padding:50px;">현재 판매 중인 상품이 없습니다.</div>';
        return;
    }

    products.forEach(product => {
        // DB 컬럼명과 DTO 필드명이 prodId인지 prod_id인지 확인 필요
        const pId = product.prodId || product.prod_id; 
        
        const productHtml = `
            <div class="product-card" data-id="${pId}">
                <div class="img-wrapper">
                    ${product.isBest === 'Y' ? '<span class="best-badge">Best</span>' : ''}
                    <img src="${product.imgUrl}" alt="${product.name}" 
                         onclick="console.log('Image Path: ${product.imgUrl}')"
                         onerror="this.onerror=null; this.src='/img/no-image.png';">
                </div>
                <div class="product-info">
                    <h3 class="product-name">${product.name}</h3>
                    <p class="product-sub">${product.subText}</p>
                    <div class="price-box">
                        <span class="price">${product.price.toLocaleString()}원</span>
                        <button class="cart-btn" onclick="addToCart(${pId}, '${product.name}')">
                            장바구니 담기
                        </button>
                    </div>
                </div>
            </div>
        `;
        gridContainer.insertAdjacentHTML('beforeend', productHtml);
    });
}

/**
 * 장바구니 담기 함수 (CSRF 토큰 필수)
 */
function addToCart(prodId, prodName) {
    const tokenTag = document.querySelector('meta[name="_csrf"]');
    const headerTag = document.querySelector('meta[name="_csrf_header"]');
    
    if(!tokenTag || !headerTag) {
        alert("로그인 정보가 없거나 보안 토큰이 만료되었습니다.");
        return;
    }

    fetch('/store/api/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [headerTag.content]: tokenTag.content
        },
        body: JSON.stringify({ prodId: prodId, quantity: 1 })
    })
    .then(response => response.json())
    .then(result => {
        if (result.success) {
            showToast(`${prodName}이(가) 담겼습니다.`);
            updateCartCount(result.newCount);
        } else {
            alert('장바구니 담기에 실패했습니다.');
        }
    })
    .catch(error => console.error('Cart Error:', error));
}

/**
 * CULTI 보라색 토스트 알림
 */
function showToast(message) {
    const oldToast = document.querySelector('.culti-toast');
    if (oldToast) oldToast.remove();
    const toastHtml = `<div class="culti-toast">${message}</div>`;
    document.body.insertAdjacentHTML('beforeend', toastHtml);
    const toast = document.querySelector('.culti-toast');
    Object.assign(toast.style, {
        position: 'fixed', bottom: '50px', left: '50%', transform: 'translateX(-50%)',
        backgroundColor: '#503396', color: 'white', padding: '12px 24px',
        borderRadius: '30px', zIndex: '1000', boxShadow: '0 4px 12px rgba(0,0,0,0.2)',
        transition: 'opacity 0.5s', fontSize: '14px', fontWeight: 'bold'
    });
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 500);
    }, 2000);
}

/**
 * 뱃지 숫자 업데이트
 */
function updateCartCount(count) {
    const badge = document.getElementById('cartCount');
    if (badge) {
        badge.innerText = count;
        badge.style.display = count > 0 ? 'block' : 'none';
    }
}