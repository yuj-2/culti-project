/* review-detail.js */
document.addEventListener('DOMContentLoaded', function() {
    
    // 루시드 아이콘 렌더링
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

    // 정렬 콤보박스 이벤트
    const sortSelect = document.getElementById('sort-select');
    if (sortSelect) {
        sortSelect.addEventListener('change', function() {
            const selectedSort = this.value;
            const contentId = this.getAttribute('data-content-id'); 
            
            if(contentId) {
                location.href = `/reservation/detail/${contentId}/reviews?sort=${selectedSort}`;
            }
        });
    }
});