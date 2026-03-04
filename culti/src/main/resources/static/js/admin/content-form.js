document.addEventListener('DOMContentLoaded', function() {
    let scheduleIndex = 1; // 기본으로 0번째가 있으니 다음은 1번째부터 시작
    
    const addBtn = document.getElementById('addScheduleBtn');
    const container = document.getElementById('schedule-container'); // 컨테이너 미리 찾기

    // 1. 회차 동적 추가 로직 (기존 코드 유지)
    if(addBtn) {
        addBtn.addEventListener('click', function() {
            // 백틱(`)을 사용해서 HTML 구조를 그대로 넣습니다.
            const newSchedule = `
                <div class="card bg-light mb-3 schedule-item border-0">
                    <div class="card-body row align-items-end">
                        <div class="col-md-2">
                            <label class="form-label fw-bold text-secondary">회차</label>
                            <input type="number" class="form-control" name="schedules[${scheduleIndex}].sessionNum" placeholder="예: ${scheduleIndex + 1}" required>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label fw-bold text-secondary">상세 장소(몇관)</label>
                            <input type="text" class="form-control" name="schedules[${scheduleIndex}].roomName" placeholder="예: 1관 (IMAX)" required>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label fw-bold text-secondary">시작 시간</label>
                            <input type="datetime-local" class="form-control" name="schedules[${scheduleIndex}].startTime" required>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label fw-bold text-secondary">종료 시간</label>
                            <input type="datetime-local" class="form-control" name="schedules[${scheduleIndex}].endTime" required>
                        </div>
                        <div class="col-md-1">
                            <button type="button" class="btn btn-outline-danger w-100 delete-schedule-btn" onclick="this.closest('.schedule-item').remove()">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
            `;
            container.insertAdjacentHTML('beforeend', newSchedule);
            scheduleIndex++;
        });
    }

    // 🌟 [추가된 로직] 시작 시간 입력 시 러닝타임 더해서 종료 시간 자동 입력
    if (container) {
        container.addEventListener('change', function(e) {
            // 값이 변한 요소가 'startTime' 입력창일 때만 실행
            if (e.target && e.target.name && e.target.name.includes('startTime')) {
                const runningTimeInput = document.getElementById('runningTime');
                if (!runningTimeInput) return;

                const runningTime = parseInt(runningTimeInput.value);

                // 러닝타임이 제대로 입력되어 있으면 계산 시작
                if (!isNaN(runningTime) && runningTime > 0) {
                    const startTimeVal = e.target.value;
                    if (!startTimeVal) return;

                    // 문자열을 날짜 객체로 변환 후 분(minute) 단위 더하기
                    const startDate = new Date(startTimeVal);
                    const endDate = new Date(startDate.getTime() + runningTime * 60000);

                    // 다시 입력창 포맷(YYYY-MM-DDTHH:mm)으로 맞추기
                    const year = endDate.getFullYear();
                    const month = String(endDate.getMonth() + 1).padStart(2, '0');
                    const day = String(endDate.getDate()).padStart(2, '0');
                    const hours = String(endDate.getHours()).padStart(2, '0');
                    const minutes = String(endDate.getMinutes()).padStart(2, '0');

                    const formattedEndTime = `${year}-${month}-${day}T${hours}:${minutes}`;

                    // 같은 줄에 있는 종료 시간 입력창에 값 세팅
                    const parentRow = e.target.closest('.card-body');
                    const endTimeInput = parentRow.querySelector('input[name*="endTime"]');
                    if (endTimeInput) {
                        endTimeInput.value = formattedEndTime;
                    }
                }
            }
        });
    }
});

// 2. 폼 전송 시 유효성 검사 (지혜 님이 짜신 완벽한 로직 유지!)
document.querySelector('form').addEventListener('submit', function(e) {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    // 전체 기간 체크: 종료일이 시작일보다 빠르면 안 됨
    if (startDate && endDate && endDate < startDate) {
        alert('콘텐츠 종료일이 시작일보다 빠를 수 없습니다!');
        e.preventDefault(); // 폼 제출(새로고침) 막기
        return;
    }

    // 회차(Schedule) 날짜가 전체 진행 기간 안에 있는지 체크
    const scheduleStarts = document.querySelectorAll('input[name*=".startTime"]');
    const scheduleEnds = document.querySelectorAll('input[name*=".endTime"]');

    for (let i = 0; i < scheduleStarts.length; i++) {
        // 시작 시간 검사
        if (scheduleStarts[i].value) {
            const schedStartDay = scheduleStarts[i].value.split('T')[0]; // 날짜(YYYY-MM-DD)만 추출
            if (schedStartDay < startDate || schedStartDay > endDate) {
                alert(`${i + 1}회차 시작 시간이 전체 진행 기간(${startDate} ~ ${endDate})을 벗어났습니다.`);
                scheduleStarts[i].focus();
                e.preventDefault();
                return;
            }
        }

        // 종료 시간 검사
        if (scheduleEnds[i].value) {
            const schedEndDay = scheduleEnds[i].value.split('T')[0];
            if (schedEndDay < startDate || schedEndDay > endDate) {
                alert(`${i + 1}회차 종료 시간이 전체 진행 기간(${startDate} ~ ${endDate})을 벗어났습니다.`);
                scheduleEnds[i].focus();
                e.preventDefault();
                return;
            }
        }

        // 회차의 종료 시간이 시작 시간보다 빠른지 체크
        if (scheduleStarts[i].value && scheduleEnds[i].value && scheduleEnds[i].value < scheduleStarts[i].value) {
            alert(`${i + 1}회차의 종료 시간이 시작 시간보다 빠를 수 없습니다.`);
            scheduleEnds[i].focus();
            e.preventDefault();
            return;
        }
    }
});