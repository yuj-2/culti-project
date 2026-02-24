// 리액트의 상태(State) 초기값 설정
const bookingState = {
    selectedContent: null,
    selectedRegion: null,
    selectedPlace: null,
    selectedTime: null
};

// 1. 콘텐츠 선택 로직
function selectContent(el, title) {
    $(".content-item").removeClass("bg-purple-50 text-[#503396] font-bold");
    $(el).addClass("bg-purple-50 text-[#503396] font-bold");
    
    bookingState.selectedContent = title;
    $("#sum-content").text(title);
    updateTimePlaceholder();
    checkValidation();
}

// 2. 광역 지역 선택 로직
function selectRegion(el, regionName) {
    $(".region-item").removeClass("bg-white text-[#503396] font-bold");
    $(el).addClass("bg-white text-[#503396] font-bold");
    
    bookingState.selectedRegion = regionName;
    
    // 상세 극장 목록 렌더링 (리액트의 selectedAreaData)
    const mockPlaces = ["강남 코엑스", "신촌 아트레온", "목동 현대"];
    let html = mockPlaces.map(place => `
        <div class="p-3 mb-2 rounded-lg border border-gray-100 cursor-pointer hover:border-[#503396] text-sm" 
             onclick="selectPlace(this, '${place}')">
            ${place}
        </div>
    `).join('');
    
    $("#area-detail-list").html(html);
    updateTimePlaceholder();
}

// 3. 극장(상세 장소) 선택 로직
function selectPlace(el, placeName) {
    $("#area-detail-list div").removeClass("border-[#503396] bg-purple-50 text-[#503396]");
    $(el).addClass("border-[#503396] bg-purple-50 text-[#503396]");
    
    bookingState.selectedPlace = placeName;
    $("#sum-place").text(placeName);
    
    renderTimeGrid(placeName); // 시간 그리드 노출
    checkValidation();
}

// 4. 시간 그리드 렌더링 (리액트의 mockSchedules.map 부분)
function renderTimeGrid(place) {
    $("#time-empty-msg").addClass("hidden");
    const container = $("#time-grid-container").removeClass("hidden");
    
    const times = [
        {t: "10:00", avai: true}, {t: "12:30", avai: true}, 
        {t: "15:00", avai: false}, {t: "19:00", avai: true}
    ];

    let html = `
        <div class="text-xs font-bold text-gray-900 border-b pb-2">${place}</div>
        <div class="grid grid-cols-3 gap-2 mt-3">
            ${times.map(s => `
                <button class="py-3 rounded text-xs font-medium border transition-all 
                    ${s.avai ? 'bg-gray-50 hover-purple border-gray-200' : 'bg-gray-100 text-gray-300 cursor-not-allowed'}"
                    ${s.avai ? `onclick="setTime('${s.t}', this)"` : 'disabled'}>
                    ${s.t}
                </button>
            `).join('')}
        </div>
    `;
    container.html(html);
}

function setTime(time, el) {
    $("#time-grid-container button").removeClass("selected-purple");
    $(el).addClass("selected-purple");
    
    bookingState.selectedTime = time;
    $("#sum-time").text(time);
    checkValidation();
}

function updateTimePlaceholder() {
    if (!bookingState.selectedContent) {
        $("#time-empty-msg").text("콘텐츠를 먼저 선택해주세요").removeClass("hidden");
        $("#time-grid-container").addClass("hidden");
    } else if (!bookingState.selectedPlace) {
        $("#time-empty-msg").text("극장을 선택해주세요").removeClass("hidden");
        $("#time-grid-container").addClass("hidden");
    }
}

// 5. 버튼 활성화 로직 (리액트의 disabled={!selectedContent...} 부분)
function checkValidation() {
    const isReady = bookingState.selectedContent && bookingState.selectedPlace && bookingState.selectedTime;
    const btn = $("#final-reserve-btn");
    
    if (isReady) {
        btn.prop("disabled", false).removeClass("bg-gray-300 cursor-not-allowed").addClass("bg-[#503396] hover:bg-[#3d2675]");
    } else {
        btn.prop("disabled", true).addClass("bg-gray-300 cursor-not-allowed").removeClass("bg-[#503396]");
    }
}