/**
 * 티켓 가격
 */
const TICKET_PRICES = {
    adult: 15000,
    youth: 12000,
    senior: 10000,
    special: 8000
};

/**
 * 전역 상태
 */
const bookingState = {
    adult: 0,
    youth: 0,
    senior: 0,
    special: 0,
    selectedSeats: [],
    timer: 600
};

/**
 * 총 인원
 */
function getTotalNeeded(){
    return bookingState.adult + bookingState.youth + bookingState.senior + bookingState.special;
}


/**
 * 좌석 초기화
 */
const seatStatusMap = {};

scheduleSeats.forEach(ss=>{
    seatStatusMap[ss.seat.seatId] = ss.status;
});

function initSeatMap(){

    const grid = document.getElementById("seat-map-grid");
    if(!grid) return;

    grid.innerHTML = "";

    const rows = ['A','B','C','D','E','F','G','H','I','J'];

    const seatMap = {};

    // DB 좌석 맵 생성
    seatListFromDb.forEach(seat=>{
        if(!seatMap[seat.seatRow]){
            seatMap[seat.seatRow] = [];
        }
        seatMap[seat.seatRow].push(seat);
    });


    rows.forEach(rowLabel=>{

        if(!seatMap[rowLabel]) return;

        const rowDiv = document.createElement("div");
        rowDiv.className = "flex justify-center items-center mb-1";

        const label = document.createElement("div");
        label.className = "w-8 text-[10px] text-gray-600 font-bold";
        label.innerText = rowLabel;

        rowDiv.appendChild(label);

        seatMap[rowLabel]
        .sort((a,b)=>a.seatCol-b.seatCol)
        .forEach(seat=>{

          const displayId = `${seat.seatRow}${seat.seatCol}`;

     const isReserved = seatStatusMap[seat.seatId] === "OCCUPIED";

            renderSeat(
                seat.seatId,
                displayId,
                isReserved,
                rowDiv
            );

        });

        grid.appendChild(rowDiv);

    });

    startTimer();
}



/**
 * 좌석 렌더링
 */
function renderSeat(dbId, displayId, isReserved, parent){

    const seat = document.createElement("div");

    seat.id = `seat-${displayId}`;
    seat.setAttribute("data-db-id", dbId);

    seat.className = `seat ${isReserved ? 'reserved' : 'available'}`;

    seat.innerText = isReserved
        ? "X"
        : displayId.replace(/[^0-9]/g,'');

    if(!isReserved){
        seat.onclick = () => toggleSeat(dbId, displayId, seat);
    }

    parent.appendChild(seat);
}



/**
 * 좌석 클릭
 */
function toggleSeat(dbId, displayId, element){

    const totalCount = getTotalNeeded();

    const index = bookingState.selectedSeats.findIndex(
        s => s.dbId === dbId
    );

    if(index > -1){

        bookingState.selectedSeats.splice(index,1);
        element.classList.remove("selected");

    }else{

        if(totalCount === 0){
            alert("인원을 먼저 선택하세요");
            return;
        }

        if(bookingState.selectedSeats.length >= totalCount){
            alert(`최대 ${totalCount}석까지 선택 가능합니다`);
            return;
        }

        bookingState.selectedSeats.push({
            dbId,
            displayId
        });

        element.classList.add("selected");

    }

    updateSummary();
}



/**
 * 인원 변경
 */
function updateCount(type,delta){

    bookingState[type] = Math.max(
        0,
        bookingState[type] + delta
    );

    document.getElementById(`${type}-count`)
        .innerText = bookingState[type];

    const total = getTotalNeeded();

    while(bookingState.selectedSeats.length > total){

        const removed = bookingState.selectedSeats.pop();

        const el = document.getElementById(
            `seat-${removed.displayId}`
        );

        if(el) el.classList.remove("selected");

    }

    updateSummary();
}



/**
 * 하단 요약 업데이트
 */
function updateSummary(){

    const totalNeeded = getTotalNeeded();
    const currentSelected = bookingState.selectedSeats.length;

    const priceEl = document.getElementById("display-total-price");
    const seatEl = document.getElementById("display-seat-names");

    const seatNames = bookingState.selectedSeats
        .map(s=>s.displayId)
        .sort()
        .join(", ");

    seatEl.innerText = seatNames || "선택 안 됨";


    let totalAmount = 0;

    if(currentSelected>0){

        const pricePool = [

            ...Array(bookingState.adult).fill(TICKET_PRICES.adult),
            ...Array(bookingState.youth).fill(TICKET_PRICES.youth),
            ...Array(bookingState.senior).fill(TICKET_PRICES.senior),
            ...Array(bookingState.special).fill(TICKET_PRICES.special)

        ].sort((a,b)=>b-a);


        for(let i=0;i<currentSelected;i++){
            if(pricePool[i]){
                totalAmount += pricePool[i];
            }
        }

        priceEl.innerText = `₩${totalAmount.toLocaleString()}`;

    }else{

        priceEl.innerText = "₩0";

    }


    if(totalNeeded>0 && currentSelected===totalNeeded){

        updateSubmitButton(true);

        const totalInput = document.getElementById("input-total-price");
        if(totalInput) totalInput.value = totalAmount;

        const seatInput = document.getElementById("input-seat-ids");
        if(seatInput){
            seatInput.value = bookingState.selectedSeats
                .map(s=>s.dbId)
                .join(",");
        }

    }else{

        updateSubmitButton(false);

    }

}



/**
 * 결제 버튼 상태
 */
function updateSubmitButton(active){

    const btn = document.getElementById("submit-booking-btn");

    if(active){

        btn.disabled=false;
        btn.classList.replace("bg-gray-800","bg-[#503396]");
        btn.classList.replace("cursor-not-allowed","cursor-pointer");

    }else{

        btn.disabled=true;
        btn.classList.replace("bg-[#503396]","bg-gray-800");
        btn.classList.replace("cursor-pointer","cursor-not-allowed");

    }

}



/**
 * 좌석 점유 타이머
 */
function startTimer(){

    const timerEl = document.getElementById("timer");
    if(!timerEl) return;

    const interval = setInterval(()=>{

        const min = String(Math.floor(bookingState.timer/60)).padStart(2,"0");
        const sec = String(bookingState.timer%60).padStart(2,"0");

        timerEl.innerText = `${min}:${sec}`;

        if(bookingState.timer<=0){

            clearInterval(interval);

            alert("좌석 점유 시간이 만료되었습니다");
            location.reload();

        }

        bookingState.timer--;

    },1000);

}



document.addEventListener("DOMContentLoaded",initSeatMap);
document.addEventListener("DOMContentLoaded", function () {

    startTimer();

});