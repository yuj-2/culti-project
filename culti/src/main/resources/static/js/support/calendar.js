    // 더미 데이터 (나중에는 스프링 부트 DB에서 가져올 데이터)
    const mockEvents = [
        { id: 1, title: '듄: 파트 2', date: '2026-03-05', category: '영화', rating: '12세 이상', image: 'https://images.unsplash.com/photo-1518043129420-ed9d4efcdcc9?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080' },
        { id: 2, title: '오페라의 유령', date: '2026-03-05', category: '공연', rating: '8세 이상', image: 'https://images.unsplash.com/photo-1599746791393-f2811f61f896?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080' },
        { id: 3, title: '아이유 콘서트', date: '2026-03-12', category: '공연', rating: '전체 관람가', image: 'https://images.unsplash.com/photo-1566735355837-2269c24e644e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080' },
        { id: 4, title: '모네 전시회', date: '2026-03-15', category: '전시', rating: '전체 관람가', image: 'https://images.unsplash.com/photo-1569342380852-035f42d9ca41?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080' }
    ];

    let currentDate = new Date(2026, 2, 1);
    let selectedDate = new Date(2026, 2, 5);

    function renderCalendar() {
        const year = currentDate.getFullYear();
        const month = currentDate.getMonth();
        
        document.getElementById('current-month-year').innerText = `${year}년 ${month + 1}월`;
        
        const firstDay = new Date(year, month, 1).getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        
        const calendarGrid = document.getElementById('calendar-days');
        calendarGrid.innerHTML = ''; // 초기화

        // 빈 칸 채우기
        for (let i = 0; i < firstDay; i++) {
            calendarGrid.innerHTML += `<div class="aspect-square"></div>`;
        }

        // 날짜 채우기
        for (let day = 1; day <= daysInMonth; day++) {
            const dateString = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            
            // 이벤트가 있는지 확인
            const hasEvent = mockEvents.some(e => e.date === dateString);
            
            // 선택된 날짜인지 확인
            const isSelected = selectedDate && 
                             selectedDate.getFullYear() === year && 
                             selectedDate.getMonth() === month && 
                             selectedDate.getDate() === day;

            const bgClass = isSelected ? 'bg-[#503396] text-white shadow-lg' : 'bg-white text-gray-800';
            const dotClass = isSelected ? 'bg-white' : 'bg-[#503396]';
            
            let dotHtml = hasEvent ? `<div class="w-1.5 h-1.5 rounded-full ${dotClass} mt-1"></div>` : '';

            calendarGrid.innerHTML += `
                <div onclick="selectDate(${year}, ${month}, ${day})" 
                     class="aspect-square border border-gray-200 rounded-lg flex flex-col items-center justify-center relative cursor-pointer transition-all duration-200 hover:border-[#503396] ${bgClass}">
                    <span class="font-medium" style="font-size: 14px;">${day}</span>
                    ${dotHtml}
                </div>
            `;
        }
        renderEvents();
    }

    function renderEvents() {
        const eventList = document.getElementById('event-list');
        const title = document.getElementById('selected-date-title');
        
        if (!selectedDate) {
            title.innerText = '날짜를 선택해주세요';
            eventList.innerHTML = `<p class="text-gray-500 text-center py-8">날짜를 선택하면 일정을 확인할 수 있습니다.</p>`;
            return;
        }

        const year = selectedDate.getFullYear();
        const month = String(selectedDate.getMonth() + 1).padStart(2, '0');
        const day = String(selectedDate.getDate()).padStart(2, '0');
        const dateString = `${year}-${month}-${day}`;

        title.innerText = `${selectedDate.getMonth() + 1}월 ${selectedDate.getDate()}일 일정`;

        const events = mockEvents.filter(e => e.date === dateString);

        if (events.length === 0) {
            eventList.innerHTML = `<p class="text-gray-500 text-center py-8">이 날짜에는 일정이 없습니다.</p>`;
            return;
        }

        let html = '';
        events.forEach(event => {
            html += `
                <div class="group cursor-pointer border border-gray-200 rounded-xl overflow-hidden hover:shadow-lg transition-all duration-200 hover:border-[#503396]">
                    <div class="relative h-40 overflow-hidden bg-gray-100">
                        <img src="${event.image}" alt="${event.title}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300">
                        <div class="absolute top-2 right-2 px-2 py-1 bg-[#252451] text-white rounded-md text-xs font-medium">${event.category}</div>
                    </div>
                    <div class="p-4">
                        <h4 class="font-semibold text-gray-800 mb-1" style="font-size: 16px;">${event.title}</h4>
                        <span class="inline-block px-2 py-0.5 bg-[#503396]/10 text-[#503396] rounded text-xs font-medium">${event.rating}</span>
                    </div>
                </div>
            `;
        });
        eventList.innerHTML = html;
    }

    function selectDate(year, month, day) {
        selectedDate = new Date(year, month, day);
        renderCalendar();
    }

    function changeMonth(offset) {
        currentDate.setMonth(currentDate.getMonth() + offset);
        renderCalendar();
    }

    // 페이지 로딩 완료 시 초기화
    document.addEventListener('DOMContentLoaded', () => {
        renderCalendar();
        lucide.createIcons();
    });