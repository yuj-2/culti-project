window.addEventListener('DOMContentLoaded', function() {
    
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

    // 1. 드롭다운 열고 닫기 로직
    const menuContainer = document.getElementById('ticket-menu-container');
    const dropdown = document.getElementById('ticket-dropdown');
    const chevron = document.getElementById('ticket-chevron');
    
    let hideTimeout;

    if(menuContainer && dropdown && chevron) {
        menuContainer.addEventListener('mouseenter', function() {
            clearTimeout(hideTimeout); 
            dropdown.classList.remove('hidden');
            chevron.style.transform = 'rotate(180deg)';
        });

        menuContainer.addEventListener('mouseleave', function() {
            hideTimeout = setTimeout(function() {
                dropdown.classList.add('hidden');
                chevron.style.transform = 'rotate(0deg)';
            }, 300);
        });
    }

    // 2. 카테고리 클릭 시 금고에 저장하고 이동하기
	const dropdownItems = document.querySelectorAll('.header-dropdown-item'); 
	    
    dropdownItems.forEach(item => {
        item.addEventListener('click', function() {
            const category = this.textContent.trim(); 
            sessionStorage.setItem('savedCategory', category); 
            
            // ★ 추가된 마법: reservation.js 한테 금고 털지 말라고 프리패스 부적을 줍니다!
            sessionStorage.setItem('keepCategory', 'true'); 
            
            location.href = '/reservation'; 
        });
    });

});