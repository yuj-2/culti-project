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