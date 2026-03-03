document.addEventListener('DOMContentLoaded', function() {
    
    // =========================================================
    // 1. 정렬 드롭다운 메뉴
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

               const currentCategory = sessionStorage.getItem('savedCategory') || '영화';
               const currentKeyword = searchInput ? searchInput.value.trim() : '';
               fetchAndRenderCards(currentCategory, currentKeyword);
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
    // 2. 백엔드(API) 호출 및 데이터 렌더링 로직
    // =========================================================
    const contentGrid = document.getElementById('content-grid');
    if (!contentGrid) return;
    
    const tabButtons = document.querySelectorAll('.flex-wrap button'); 
    const searchInput = document.getElementById('search-input'); 

    function fetchAndRenderCards(category, keyword = '') {
        if(!contentGrid) return;
        
      const sortTextElement = document.getElementById('sort-text');
      const currentSort = sortTextElement ? sortTextElement.textContent.trim() : '인기순';
        
      fetch(`/content/api/list?category=${encodeURIComponent(category)}&keyword=${encodeURIComponent(keyword)}&sort=${encodeURIComponent(currentSort)}`)
               .then(response => response.json())
               .then(data => {
                contentGrid.innerHTML = '';

                if(data.length === 0) {
                    contentGrid.innerHTML = `<div class="col-span-full text-center py-12 text-gray-500 font-medium">검색 결과가 없습니다.</div>`;
                    return;
                }

                data.forEach(item => {
                    const cardHTML = `
                        <div onclick="location.href='/reservation/detail/${item.id}'" class="group bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-xl border border-gray-100 transition-all duration-300 cursor-pointer">
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
            })
            .catch(error => console.error('Error:', error));
    }

    // ---------------------------------------------------------
    // 금고에서 카테고리 꺼내오기 & 뒤로가기 감지 (유지)
    // ---------------------------------------------------------
    let savedCategory = sessionStorage.getItem('savedCategory');
   const keepCategory = sessionStorage.getItem('keepCategory');
   
    const referrer = document.referrer; 
    const navEntries = performance.getEntriesByType("navigation");
    const navType = navEntries.length > 0 ? navEntries[0].type : "";

    const isReload = navType === "reload";
    const isBackForward = navType === "back_forward";
    const isFromDetail = referrer.includes('/reservation/detail');
    
   if (!isReload && !isBackForward && !isFromDetail && !keepCategory) {
        savedCategory = '영화';
        sessionStorage.removeItem('savedCategory');
    } else if (!savedCategory) {
        savedCategory = '영화';
    }
    
   sessionStorage.removeItem('keepCategory');
   
   tabButtons.forEach(btn => {
        if (btn.textContent.trim() === savedCategory) {
            btn.className = "px-6 py-3 rounded-xl transition-all duration-200 bg-[#503396] text-white shadow-lg shadow-purple-900/25 res-tab-active";
        } else {
            btn.className = "px-6 py-3 rounded-xl transition-all duration-200 bg-gray-50 text-gray-700 hover:bg-gray-100 res-tab-default";
        }
    });
   
    // ---------------------------------------------------------
    // 이벤트 리스너들 
    // ---------------------------------------------------------
    
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const currentKeyword = this.value.trim(); 
            const currentCategory = sessionStorage.getItem('savedCategory') || '영화'; 
            fetchAndRenderCards(currentCategory, currentKeyword);
        });
    }

    // 2. 탭 버튼을 클릭했을 때
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const clickedCategory = this.textContent.trim();
            sessionStorage.setItem('savedCategory', clickedCategory);
            
            tabButtons.forEach(btn => {
                btn.className = "px-6 py-3 rounded-xl transition-all duration-200 bg-gray-50 text-gray-700 hover:bg-gray-100 res-tab-default";
            });
            this.className = "px-6 py-3 rounded-xl transition-all duration-200 bg-[#503396] text-white shadow-lg shadow-purple-900/25 res-tab-active";

            const currentKeyword = searchInput ? searchInput.value.trim() : '';
            fetchAndRenderCards(clickedCategory, currentKeyword);
        });
    });

    // =========================================================
    // 3. 최초 페이지 로드 시 백엔드 호출
    // =========================================================
    const currentKeyword = searchInput ? searchInput.value.trim() : '';
    fetchAndRenderCards(savedCategory, currentKeyword);

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

			        // [선택 사항] 지도의 다른 부분을 클릭하면 인포윈도우를 닫고 싶다면 아래 코드를 추가하세요.
			        /*
			        kakao.maps.event.addListener(map, 'click', function() {
			            infowindow.close();
			        });
			        */

			        map.setCenter(coords);
			    } 
			});
        } else {
            document.getElementById('map').innerHTML = '<div class="w-full h-full flex items-center justify-center text-gray-500 font-medium bg-gray-100">지도 정보가 없습니다.</div>';
        }
    }
};