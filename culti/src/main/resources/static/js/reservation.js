document.addEventListener('DOMContentLoaded', function() {

    // =========================================================
    // DOM 요소들 가져오기
    // =========================================================
    const sortBtn = document.getElementById('sort-button');
    const sortDropdown = document.getElementById('sort-dropdown');
    const sortChevron = document.getElementById('sort-chevron');
    const sortText = document.getElementById('sort-text');
    const sortOptions = document.querySelectorAll('.sort-option');
    const searchInput = document.getElementById('search-input');
    const tabButtons = document.querySelectorAll('.flex-wrap button');
    const contentGrid = document.getElementById('content-grid');

    // =========================================================
    // 1. 핵심! 렌더링 함수 (무조건 호출될 때마다 현재 상태를 금고에 박제!)
    // =========================================================
    window.fetchAndRenderCards = function(category, keyword = '', page = 0) {
        if(!contentGrid) return;

        // 화면에 떠있는 현재 정렬 기준 읽어오기
        const currentSort = sortText ? sortText.textContent.trim() : '인기순';

        // 🔥 제일 중요한 부분: 데이터를 요청할 때마다 '현재 상태'를 모조리 금고에 덮어씁니다!
        sessionStorage.setItem('savedCategory', category);
        sessionStorage.setItem('savedKeyword', keyword);
        sessionStorage.setItem('savedPage', page);
        sessionStorage.setItem('savedSort', currentSort);

        fetch(`/content/api/list?category=${encodeURIComponent(category)}&keyword=${encodeURIComponent(keyword)}&sort=${encodeURIComponent(currentSort)}&page=${page}`)
            .then(response => response.json())
            .then(pageData => {
                contentGrid.innerHTML = '';
                const data = pageData.content;

                if(data.length === 0) {
                    contentGrid.innerHTML = `<div class="col-span-full text-center py-12 text-gray-500 font-medium">검색 결과가 없습니다.</div>`;
                    document.getElementById('pagination-container').innerHTML = '';
                    return;
                }

                // 카드 그리기 (여기에 keepState 티켓 남기는 코드 들어있음!)
                data.forEach(item => {
                    const cardHTML = `
                        <div onclick="sessionStorage.setItem('keepState', 'true'); location.href='/reservation/detail/${item.id}'" class="group bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-xl border border-gray-100 transition-all duration-300 cursor-pointer">
                            <div class="relative overflow-hidden aspect-[3/4] bg-gray-100">
                                <img src="${item.posterUrl}" alt="${item.title}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                                <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-black/0 to-black/0 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                            </div>
                            <div class="p-5">
                                <h3 class="text-gray-900 mb-3 group-hover:text-[#503396] transition-colors line-clamp-1 res-card-title font-bold">${item.title}</h3>
                                <div class="space-y-2">
                                    <div class="flex items-center gap-2">
                                        <span class="inline-block px-3 py-1 bg-purple-100 text-[#503396] rounded-lg text-sm font-semibold res-card-badge">${item.ageLimit || '전체관람가'}</span>
                                    </div>
                                    <p class="text-gray-600 text-sm res-card-date">${item.startDate} ~ ${item.endDate}</p>
                                </div>
                            </div>
                        </div>
                    `;
                    contentGrid.insertAdjacentHTML('beforeend', cardHTML);
                });

                // 페이징 버튼 그리기 실행
                renderPagination(pageData, category, keyword);
            })
            .catch(error => console.error('Error:', error));
    };

    // 페이징 버튼 생성
    function renderPagination(pageData, category, keyword) {
        const paginationContainer = document.getElementById('pagination-container');
        if (!paginationContainer) return;

        if (pageData.totalPages <= 1) {
            paginationContainer.innerHTML = '';
            return;
        }

        let html = `<nav aria-label="Page navigation"><ul class="inline-flex items-center -space-x-px gap-1">`;

        html += `<li class="${pageData.first ? 'opacity-50 pointer-events-none' : ''}">
            <button onclick="window.fetchAndRenderCards('${category}', '${keyword}', ${pageData.number - 1})"
               class="block px-3 py-2 ml-0 leading-tight text-gray-500 bg-white border border-gray-300 rounded-l-lg hover:bg-gray-100 hover:text-gray-700">
                <span class="font-bold">&lt; 이전</span>
            </button>
        </li>`;

        for (let i = 0; i < pageData.totalPages; i++) {
            const isActive = (i === pageData.number);
            const activeClass = isActive ? 'bg-[#503396] text-white border-[#503396] font-bold' : 'text-gray-500 bg-white border-gray-300 hover:bg-gray-100 hover:text-gray-700';
            
            html += `<li>
                <button onclick="window.fetchAndRenderCards('${category}', '${keyword}', ${i})"
                   class="px-4 py-2 leading-tight border rounded-md ${activeClass}">${i + 1}</button>
            </li>`;
        }

        html += `<li class="${pageData.last ? 'opacity-50 pointer-events-none' : ''}">
            <button onclick="window.fetchAndRenderCards('${category}', '${keyword}', ${pageData.number + 1})"
               class="block px-3 py-2 leading-tight text-gray-500 bg-white border border-gray-300 rounded-r-lg hover:bg-gray-100 hover:text-gray-700">
                <span class="font-bold">다음 &gt;</span>
            </button>
        </li>`;

        html += `</ul></nav>`;
        paginationContainer.innerHTML = html;
    }


    // =========================================================
    // 2. 이벤트 리스너 등록 (정렬 드롭다운, 검색, 탭 변경)
    // =========================================================
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

                const currentCategory = sessionStorage.getItem('savedCategory') || '영화';
                const currentKeyword = searchInput ? searchInput.value.trim() : '';
                // 정렬을 바꿨으니 0페이지로 새로 통신!
                window.fetchAndRenderCards(currentCategory, currentKeyword, 0);
            });
        });

        document.addEventListener('click', function(e) {
            if (!sortBtn.contains(e.target) && !sortDropdown.contains(e.target)) {
                sortDropdown.classList.add('hidden');
                sortChevron.style.transform = 'rotate(0deg)';
            }
        });
    }

    if (searchInput) {
        // 🔥 브라우저 자동완성 오지랖 차단! (input -> keyup으로 변경됨)
        searchInput.addEventListener('keyup', function(e) {
            const currentKeyword = this.value.trim(); 
            const currentCategory = sessionStorage.getItem('savedCategory') || '영화'; 
            window.fetchAndRenderCards(currentCategory, currentKeyword, 0);
        });
    }

    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const clickedCategory = this.textContent.trim();
            
            tabButtons.forEach(btn => {
                btn.className = "px-6 py-3 rounded-xl transition-all duration-200 bg-gray-50 text-gray-700 hover:bg-gray-100 res-tab-default";
            });
            this.className = "px-6 py-3 rounded-xl transition-all duration-200 bg-[#503396] text-white shadow-lg shadow-purple-900/25 res-tab-active";

            const currentKeyword = searchInput ? searchInput.value.trim() : '';
            window.fetchAndRenderCards(clickedCategory, currentKeyword, 0); 
        });
    });


    // =========================================================
    // 3. 페이지 최초 진입 시 상태 복원 및 초기 호출!
    // =========================================================
    const referrer = document.referrer; 
    const navEntries = performance.getEntriesByType("navigation");
    const navType = navEntries.length > 0 ? navEntries[0].type : "";

    const isReload = navType === "reload";
    const isBackForward = navType === "back_forward";
    const isFromDetail = referrer.includes('/reservation/detail');
    const keepCategory = sessionStorage.getItem('keepCategory');
    
    // 🔥 카드를 눌렀다는 증명서(티켓) 꺼내기!
    const keepState = sessionStorage.getItem('keepState'); 

    let initCategory = '영화';
    let initKeyword = '';
    let initPage = 0;
    let initSort = '인기순';

    // 🔥 조건문에 keepState === 'true' 추가됨!
    if (isReload || isBackForward || isFromDetail || keepCategory || keepState === 'true') {
        initCategory = sessionStorage.getItem('savedCategory') || '영화';
        initKeyword = sessionStorage.getItem('savedKeyword') || '';
        initPage = parseInt(sessionStorage.getItem('savedPage')) || 0;
        initSort = sessionStorage.getItem('savedSort') || '인기순';
        
        // 화면의 검색어와 정렬 글자도 복구
        if (searchInput) searchInput.value = initKeyword;
        if (sortText) sortText.textContent = initSort;

        // 화면 탭 활성화 복구
        tabButtons.forEach(btn => {
            if (btn.textContent.trim() === initCategory) {
                btn.className = "px-6 py-3 rounded-xl transition-all duration-200 bg-[#503396] text-white shadow-lg shadow-purple-900/25 res-tab-active";
            } else {
                btn.className = "px-6 py-3 rounded-xl transition-all duration-200 bg-gray-50 text-gray-700 hover:bg-gray-100 res-tab-default";
            }
        });

    } else {
        // 완전 첫 진입이면 싹 지우기
        sessionStorage.removeItem('savedCategory');
        sessionStorage.removeItem('savedKeyword');
        sessionStorage.removeItem('savedPage');
        sessionStorage.removeItem('savedSort');
        sessionStorage.removeItem('keepCategory');
    }

    // 🔥 티켓은 한 번 썼으니 찢어버리기!
    sessionStorage.removeItem('keepState');

    // 모든 준비가 끝났으면 최초 1회 통신 실행!
    window.fetchAndRenderCards(initCategory, initKeyword, initPage);

});

// 화면이 다 그려진 후 Lucide 아이콘을 활성화
lucide.createIcons();

// 주소 복사 기능
function copyAddress() {
    var addressText = document.getElementById("place-address").innerText;
    navigator.clipboard.writeText(addressText).then(function() {
        alert("주소가 복사되었습니다: " + addressText);
    }).catch(function(err) {
        alert("주소 복사에 실패했습니다.");
    });
}

// 회차 모달 열기
function openScheduleModal() {
    document.getElementById('scheduleModal').classList.remove('hidden');
    document.getElementById('scheduleModal').classList.add('flex');
}

// 회차 모달 닫기
function closeScheduleModal() {
    document.getElementById('scheduleModal').classList.add('hidden');
    document.getElementById('scheduleModal').classList.remove('flex');
}

// =========================================================
// 4. 지도 렌더링 로직
// =========================================================

// 카카오맵 지도 띄우기
window.onload = function() {
    var addressElement = document.getElementById("place-address");
    
    if(addressElement) {
        var addressText = addressElement.innerText;

        if(addressText && !addressText.includes("미정") && !addressText.includes("등록되지")) {
            var mapContainer = document.getElementById('map');
            var mapOption = {
                center: new kakao.maps.LatLng(33.450701, 126.570667),
                level: 3
            };  

            var map = new kakao.maps.Map(mapContainer, mapOption); 
            var geocoder = new kakao.maps.services.Geocoder();

			var placeName = document.getElementById('place-name-data')?.textContent.trim() || '장소 안내';

			geocoder.addressSearch(addressText, function(result, status) {
			    if (status === kakao.maps.services.Status.OK) {
			        var coords = new kakao.maps.LatLng(result[0].y, result[0].x);
			        var marker = new kakao.maps.Marker({ map: map, position: coords });

			        var infowindow = new kakao.maps.InfoWindow({
			            content: '<div style="width:150px;text-align:center;padding:6px 0;font-weight:bold;color:#503396;font-size:14px;">' + placeName + '</div>',
			            removable: true // 닫기 버튼(X)을 추가해서 수동으로 닫을 수도 있게 합니다.
			        });

			        // [추가] 마커를 클릭했을 때 인포윈도우를 표시합니다.
			        kakao.maps.event.addListener(marker, 'click', function() {
			            infowindow.open(map, marker);
			        });

					// 다른데 클릭하면 인포윈도우 닫음
			        kakao.maps.event.addListener(map, 'click', function() {
			            infowindow.close();
			        });

			        map.setCenter(coords);
			    } 
			});
        } else {
            document.getElementById('map').innerHTML = '<div class="w-full h-full flex items-center justify-center text-gray-500 font-medium bg-gray-100">지도 정보가 없습니다.</div>';
        }
    }
};