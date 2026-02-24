document.addEventListener('DOMContentLoaded', function() {
    
    // =========================================================
    // 1. 정렬 드롭다운 메뉴 (유저님 기존 코드 유지)
    // =========================================================
    const sortBtn = document.getElementById('sort-button');
    const sortDropdown = document.getElementById('sort-dropdown');
    const sortChevron = document.getElementById('sort-chevron');
    const sortText = document.getElementById('sort-text');
    const sortOptions = document.querySelectorAll('.sort-option');

    if (sortBtn && sortDropdown) {
        sortBtn.addEventListener('click', function(e) {
            e.stopPropagation(); 
            sortDropdown.classList.toggle('hidden');
            sortChevron.style.transform = sortDropdown.classList.contains('hidden') ? 'rotate(0deg)' : 'rotate(180deg)';
        });

        sortOptions.forEach(option => {
            option.addEventListener('click', function() {
                sortText.textContent = this.textContent;
                sortDropdown.classList.add('hidden');
                sortChevron.style.transform = 'rotate(0deg)';
            });
        });

        document.addEventListener('click', function(e) {
            if (!sortBtn.contains(e.target) && !sortDropdown.contains(e.target)) {
                sortDropdown.classList.add('hidden');
                sortChevron.style.transform = 'rotate(0deg)';
            }
        });
    }

    // =========================================================
    // 2. 탭 필터링 및 데이터 렌더링 로직 (새로 추가됨!)
    // =========================================================
    let allContents = []; // 백엔드에서 가져온 전체 데이터를 저장해둘 창고
    const contentGrid = document.getElementById('content-grid');
    const tabButtons = document.querySelectorAll('.flex-wrap button'); // 탭 버튼 4개 선택

    // 화면에 카드를 그려주는 전담 함수
    function renderCards(category) {
        if(!contentGrid) return;
        contentGrid.innerHTML = ''; // 화면 한 번 싹 지우기

        // '전체 데이터' 중에서 클릭한 '카테고리'랑 이름이 똑같은 것만 골라내기
        const filteredData = allContents.filter(item => item.category === category);

        // 만약 해당 카테고리에 데이터가 없다면?
        if(filteredData.length === 0) {
            contentGrid.innerHTML = `<div class="col-span-full text-center py-12 text-gray-500 font-medium">현재 등록된 ${category} 콘텐츠가 없습니다.</div>`;
            return;
        }

        // 골라낸 데이터들로 HTML 카드 예쁘게 만들기
        filteredData.forEach(item => {
            const cardHTML = `
                <div class="group bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-xl border border-gray-100 transition-all duration-300 cursor-pointer">
                    <div class="relative overflow-hidden aspect-[3/4] bg-gray-100">
                        <img src="${item.posterUrl}" alt="${item.title}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                        <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-black/0 to-black/0 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                    </div>
                    <div class="p-5">
                        <h3 class="text-gray-900 mb-3 group-hover:text-[#503396] transition-colors line-clamp-1 res-card-title font-bold">
                            ${item.title}
                        </h3>
                        <div class="space-y-2">
                            <div class="flex items-center gap-2">
                                <span class="inline-block px-3 py-1 bg-purple-100 text-[#503396] rounded-lg text-sm font-semibold res-card-badge">
                                    ${item.ageLimit || '전체관람가'}
                                </span>
                            </div>
                            <p class="text-gray-600 text-sm res-card-date">${item.startDate} ~ ${item.endDate}</p>
                        </div>
                    </div>
                </div>
            `;
            contentGrid.insertAdjacentHTML('beforeend', cardHTML);
        });
    }

    // 탭 버튼을 클릭했을 때의 이벤트 설정
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            // 1. 클릭한 버튼의 글자(영화, 공연 등) 가져오기
            const clickedCategory = this.textContent.trim();

            // 2. 모든 버튼의 디자인을 하얀색(비활성화)으로 초기화
            tabButtons.forEach(btn => {
                btn.className = "px-6 py-3 rounded-xl transition-all duration-200 bg-gray-50 text-gray-700 hover:bg-gray-100 res-tab-default";
            });
            // 3. 지금 클릭한 버튼만 보라색(활성화) 디자인으로 변경
            this.className = "px-6 py-3 rounded-xl transition-all duration-200 bg-[#503396] text-white shadow-lg shadow-purple-900/25 res-tab-active";

            // 4. 클릭한 카테고리에 맞춰서 카드 다시 그리기
            renderCards(clickedCategory);
        });
    });

    // =========================================================
    // 3. 최초 페이지 로드 시 API 호출
    // =========================================================
    fetch('/content/api/list')
        .then(response => response.json())
        .then(data => {
            allContents = data; // 전체 데이터를 창고에 저장
            renderCards('영화'); // 처음 접속했을 때는 '영화' 데이터만 먼저 보여주기
        })
        .catch(error => {
            console.error('데이터를 불러오는데 실패했습니다:', error);
            if(contentGrid) {
                contentGrid.innerHTML = '<p class="col-span-full text-center py-12 text-red-500">데이터를 불러오는 중 오류가 발생했습니다.</p>';
            }
        });
});