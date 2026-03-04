let eventsData = []; // 서버에서 받아온 전체 일정 (한 달치)
let currentDate = new Date(2026, 2, 1);
let selectedDate = new Date(2026, 2, 5);
let selectedCategory = '전체';

async function fetchEvents(year, month, category = '전체') {
  const url = `/calendar/api/events?year=${year}&month=${month}&category=${encodeURIComponent(category)}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error('캘린더 일정 로딩 실패');
  eventsData = await res.json(); // [{scheduleId, contentId, title, date, category, rating, image}, ...]
}


let loadedYear = null;
let loadedMonth = null;
let loadedCategory = null;

async function fetchEvents(year, month1to12, category = '전체') {
  const url = `/calendar/api/events?year=${year}&month=${month1to12}&category=${encodeURIComponent(category)}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error('캘린더 일정 로딩 실패');
  eventsData = await res.json();
}

function setActiveFilterButton() {
  const buttons = document.querySelectorAll('#calendar-filters [data-category]');
  buttons.forEach(btn => {
    const isActive = btn.dataset.category === selectedCategory;
    btn.classList.toggle('bg-[#503396]', isActive);
    btn.classList.toggle('text-white', isActive);
    btn.classList.toggle('shadow-md', isActive);
    btn.classList.toggle('shadow-purple-900/20', isActive);

    // 비활성 스타일
    btn.classList.toggle('bg-white', !isActive);
    btn.classList.toggle('text-gray-600', !isActive);
    btn.classList.toggle('border', !isActive);
    btn.classList.toggle('border-gray-200', !isActive);
  });
}

async function renderCalendar() {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();
	
	const needFetch =
	    loadedYear !== year ||
	    loadedMonth !== month ||
	    loadedCategory !== selectedCategory;

	  if (needFetch) {
	    await fetchEvents(year, month + 1, selectedCategory);
	    loadedYear = year;
	    loadedMonth = month;
	    loadedCategory = selectedCategory;
	  }


    document.getElementById('current-month-year').innerText = `${year}년 ${month + 1}월`;

    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    const calendarGrid = document.getElementById('calendar-days');
    calendarGrid.innerHTML = '';

    for (let i = 0; i < firstDay; i++) {
        calendarGrid.innerHTML += `<div class="aspect-square"></div>`;
    }

    for (let day = 1; day <= daysInMonth; day++) {
        const dateString = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

        // 
        const hasEvent = eventsData.some(e => e.date === dateString);

        const isSelected = selectedDate &&
                         selectedDate.getFullYear() === year &&
                         selectedDate.getMonth() === month &&
                         selectedDate.getDate() === day;

        const bgClass = isSelected ? 'bg-[#503396] text-white shadow-lg' : 'bg-white text-gray-800';
        const dotClass = isSelected ? 'bg-white' : 'bg-[#503396]';

        const dotHtml = hasEvent ? `<div class="w-1.5 h-1.5 rounded-full ${dotClass} mt-1"></div>` : '';

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

    // 
    const events = eventsData.filter(e => e.date === dateString);

    if (events.length === 0) {
        eventList.innerHTML = `<p class="text-gray-500 text-center py-8">이 날짜에는 일정이 없습니다.</p>`;
        return;
    }

    let html = '';
    events.forEach(event => {
        html += `
            <div class="group cursor-pointer border border-gray-200 rounded-xl overflow-hidden hover:shadow-lg transition-all duration-200 hover:border-[#503396]"
                 onclick="location.href='/reservation/detail/${event.contentId}'">
                <div class="relative h-40 overflow-hidden bg-gray-100">
                    <img src="${event.image || ''}" alt="${event.title}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300">
                    <div class="absolute top-2 right-2 px-2 py-1 bg-[#252451] text-white rounded-md text-xs font-medium">${event.category}</div>
                </div>
                <div class="p-4">
                    <h4 class="font-semibold text-gray-800 mb-1" style="font-size: 16px;">${event.title}</h4>
                    <span class="inline-block px-2 py-0.5 bg-[#503396]/10 text-[#503396] rounded text-xs font-medium">${event.rating || ''}</span>
                </div>
            </div>
        `;
    });
    eventList.innerHTML = html;
}


async function selectDate(year, month, day) {
    selectedDate = new Date(year, month, day);
    renderCalendar();
}

async function changeMonth(offset) {
    currentDate.setMonth(currentDate.getMonth() + offset);
    await renderCalendar();
}

document.addEventListener('DOMContentLoaded', async () => {
  // 필터 버튼 연결
  document.querySelectorAll('#calendar-filters [data-category]').forEach(btn => {
    btn.addEventListener('click', async () => {
      selectedCategory = btn.dataset.category;
      setActiveFilterButton();

      // 카테고리 바뀌면 다시 받아야 하니까 캐시 깨기
      loadedCategory = null;

      await renderCalendar();
    });
  });

  setActiveFilterButton();
  await renderCalendar();
  lucide.createIcons();
});