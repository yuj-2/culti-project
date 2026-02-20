document.addEventListener('DOMContentLoaded', function() {
    
    const sortBtn = document.getElementById('sort-button');
    const sortDropdown = document.getElementById('sort-dropdown');
    const sortChevron = document.getElementById('sort-chevron');
    const sortText = document.getElementById('sort-text');
    const sortOptions = document.querySelectorAll('.sort-option');

    if (sortBtn && sortDropdown) {
        // 1. 버튼 클릭 시 메뉴 열기/닫기 및 화살표 돌리기
        sortBtn.addEventListener('click', function(e) {
            e.stopPropagation(); // 클릭 이벤트가 다른 곳으로 번지는 것 막기
            sortDropdown.classList.toggle('hidden');
            sortChevron.style.transform = sortDropdown.classList.contains('hidden') ? 'rotate(0deg)' : 'rotate(180deg)';
        });

        // 2. 옵션(최신순 등) 클릭 시 글자 바뀌고 창 닫히기
        sortOptions.forEach(option => {
            option.addEventListener('click', function() {
                sortText.textContent = this.textContent;
                sortDropdown.classList.add('hidden');
                sortChevron.style.transform = 'rotate(0deg)';
            });
        });

        // 3. 버튼 밖의 빈 화면을 클릭하면 메뉴창 닫히기
        document.addEventListener('click', function(e) {
            if (!sortBtn.contains(e.target) && !sortDropdown.contains(e.target)) {
                sortDropdown.classList.add('hidden');
                sortChevron.style.transform = 'rotate(0deg)';
            }
        });
    }
});