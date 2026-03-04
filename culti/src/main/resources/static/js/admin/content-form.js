document.addEventListener('DOMContentLoaded', function() {
    let scheduleIndex = 1; // 기본으로 0번째가 있으니 다음은 1번째부터 시작
    
    const addBtn = document.getElementById('addScheduleBtn');
    if(addBtn) {
        addBtn.addEventListener('click', function() {
            const container = document.getElementById('schedule-container');
            
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
});

document.querySelector('form').addEventListener('submit', function(e) {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    // 1. 전체 기간 체크: 종료일이 시작일보다 빠르면 안 됨
    if (startDate && endDate && endDate < startDate) {
        alert('콘텐츠 종료일이 시작일보다 빠를 수 없습니다!');
        e.preventDefault(); // 폼 제출(새로고침) 막기
        return;
    }

    // 2. 회차(Schedule) 날짜가 전체 진행 기간 안에 있는지 체크
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

        // 3. 회차의 종료 시간이 시작 시간보다 빠른지 체크
        if (scheduleStarts[i].value && scheduleEnds[i].value && scheduleEnds[i].value < scheduleStarts[i].value) {
            alert(`${i + 1}회차의 종료 시간이 시작 시간보다 빠를 수 없습니다.`);
            scheduleEnds[i].focus();
            e.preventDefault();
            return;
        }
    }
});