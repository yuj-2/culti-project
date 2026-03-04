console.log("performance js loaded");

const bookingState = {

    scheduleId: null,

    seats: [],

    // 좌석 등급별 선택 수
    seatType: {
        VIP: 0,
        R: 0,
        S: 0,
        A: 0
    },

    selectedSeats: [],

    currentFloor: 1,

    timer: 600,

    // 관리자 가격
    priceMap: {}
};


// -------------------------
// 시작
// -------------------------
window.addEventListener("load", async () => {

    const params = new URLSearchParams(window.location.search);
    bookingState.scheduleId = params.get("scheduleId");

    if(!bookingState.scheduleId){
        alert("scheduleId 없음");
        return;
    }

    await loadSeats();
    await loadPrices();

    setFloorButtonActive(1);

    initSeatMap();
    startTimer();
    updateSummary();
});


// -------------------------
// 좌석 조회
// -------------------------
async function loadSeats(){

    const res = await fetch(`/api/performance/seats/${bookingState.scheduleId}`);

    const data = await res.json();

    bookingState.seats = data.map(s=>({

        seatId: s.seat.seatId,
        row: s.seat.seatRow,
        col: Number(s.seat.seatCol),
        grade: s.seat.grade,
        floor: Number(s.seat.floor),
        status: s.status

    }));

}


// -------------------------
// 관리자 가격 조회
// -------------------------
async function loadPrices(){

    const res = await fetch(`/api/performance/price/${bookingState.scheduleId}`);
    const data = await res.json();

    bookingState.priceMap = {};

    data.forEach(p => {
        bookingState.priceMap[p.grade] = p.price;
    });

}




// -------------------------
// 층 전환
// -------------------------
function switchFloor(floor){

    bookingState.currentFloor = floor;

    setFloorButtonActive(floor);

    initSeatMap();

}


function setFloorButtonActive(floor){

    document.querySelectorAll(".floor-btn")
        .forEach(btn=>btn.classList.remove("active"));

    document.getElementById(`btn-${floor}f`)
        .classList.add("active");

}


// -------------------------
// 좌석 렌더링
// -------------------------
function initSeatMap(){

    const left = document.getElementById("block-left");
    const center = document.getElementById("block-center");
    const right = document.getElementById("block-right");

    left.innerHTML="";
    center.innerHTML="";
    right.innerHTML="";

    const floorSeats =
        bookingState.seats.filter(s=>s.floor===bookingState.currentFloor);

    const rowMap={};

    floorSeats.forEach(seat=>{

        if(!rowMap[seat.row]) rowMap[seat.row]=[];

        rowMap[seat.row].push(seat);

    });

    Object.keys(rowMap).sort().forEach(row=>{

        const rowLeft=document.createElement("div");
        const rowCenter=document.createElement("div");
        const rowRight=document.createElement("div");

        rowLeft.className="row-wrapper";
        rowCenter.className="row-wrapper";
        rowRight.className="row-wrapper";

        const label=document.createElement("div");
        label.className="row-label";
        label.innerText=row;

        rowCenter.appendChild(label);

        rowMap[row].sort((a,b)=>a.col-b.col)
            .forEach(seat=>{

            const seatEl=document.createElement("div");

            seatEl.className="seat";

            if(seat.status==="OCCUPIED"){

                seatEl.classList.add("reserved");
                seatEl.innerText="X";

            }else{

                seatEl.classList.add(seat.grade.toLowerCase());

                seatEl.innerText=seat.col;

                seatEl.onclick=()=>toggleSeat(seat,seatEl);

            }

            if(seat.col<=5) rowLeft.appendChild(seatEl);
            else if(seat.col<=10) rowCenter.appendChild(seatEl);
            else rowRight.appendChild(seatEl);

        });

        if(rowLeft.children.length>0) left.appendChild(rowLeft);
        if(rowCenter.children.length>1) center.appendChild(rowCenter);
        if(rowRight.children.length>0) right.appendChild(rowRight);

    });

}


// -------------------------
// 좌석 선택
// -------------------------
function toggleSeat(seat, el){

    const idx = bookingState.selectedSeats
        .findIndex(s => s.seatId === seat.seatId);

    // 선택 해제
	if(idx > -1){

	    bookingState.selectedSeats.splice(idx,1);


	    el.classList.remove("selected");

	    updateSummary();

	    return;
	}

    // 현재 선택된 좌석 등급 확인
    const selectedGrades = Object.entries(bookingState.seatType)
        .filter(([k,v]) => v > 0)
        .map(([k]) => k);

    // 다른 등급 좌석 클릭했을 경우
    if(selectedGrades.length > 0 && !selectedGrades.includes(seat.grade)){
        alert("좌석 등급을 확인해주세요.");
        return;
    }

    // 해당 등급 최대 선택 수
    const gradeLimit = bookingState.seatType[seat.grade];

    const selectedSameGrade =
        bookingState.selectedSeats
        .filter(s => s.grade === seat.grade).length;

    if(selectedSameGrade >= gradeLimit){
        alert(`${seat.grade}석 선택 수를 초과했습니다.`);
        return;
    }

    const price =
        bookingState.priceMap[seat.grade?.toUpperCase()] ?? 0;

		bookingState.selectedSeats.push({
		    seatId: seat.seatId,
		    name: `${seat.floor}F-${seat.row}${seat.col}`,
		    grade: seat.grade,
		    price
		});

		

    el.classList.add("selected");

    updateSummary();
}

function getTotalSeatCount(){

    return Object.values(bookingState.seatType)
        .reduce((a,b)=>a+b,0);

}


// -------------------------
// 좌석 등급 수 변경
// -------------------------
function updateSeatType(type,delta){

    bookingState.seatType[type]+=delta;

    if(bookingState.seatType[type]<0)
        bookingState.seatType[type]=0;

    document.getElementById(`${type.toLowerCase()}-count`)
        .innerText=bookingState.seatType[type];

    updateSummary();

}


// -------------------------
// 요약
// -------------------------
function updateSummary(){

    const namesEl=document.getElementById("display-seat-names");
    const priceEl=document.getElementById("display-total-price");
    const btn=document.getElementById("submit-booking-btn");

    const names=bookingState.selectedSeats
        .map(s=>s.name).join(", ");

		const total = bookingState.selectedSeats
		    .reduce((sum,s)=>sum+(s.price||0),0);

    namesEl.innerText=names||"선택 없음";

    priceEl.innerText=`₩${total.toLocaleString()}`;

    const ok =
        bookingState.selectedSeats.length===getTotalSeatCount()
        && getTotalSeatCount()>0;

    btn.disabled=!ok;

}


// -------------------------
// 타이머
// -------------------------
function startTimer(){

    const timerEl=document.getElementById("hold-timer");

    const interval=setInterval(()=>{

        const min=String(Math.floor(bookingState.timer/60))
            .padStart(2,"0");

        const sec=String(bookingState.timer%60)
            .padStart(2,"0");

        timerEl.innerText=`좌석 임시 점유 ${min}:${sec}`;

        bookingState.timer--;

        if(bookingState.timer<0){

            clearInterval(interval);

            alert("시간 만료");

            location.reload();

        }

    },1000);

}


// -------------------------
// 예매 요청
// -------------------------
async function handleBookingSubmit(){

    const payload={

        scheduleId:Number(bookingState.scheduleId),

        seatIds:bookingState.selectedSeats
            .map(s=>String(s.seatId)),

        totalPrice:bookingState.selectedSeats
            .reduce((a,b)=>a+(b.price||0),0)

    };

    const csrfToken =
        document.querySelector('meta[name="_csrf"]').content;

    const csrfHeader =
        document.querySelector('meta[name="_csrf_header"]').content;

		const res = await fetch("/api/performance/booking/create",{

		    method:"POST",

		    headers:{
		        "Content-Type":"application/json",
		        [csrfHeader]:csrfToken
		    },

		    body:JSON.stringify(payload)

		});

		const data = await res.json();

		location.href=`/booking/success?bookingId=${data.bookingId}`;
}

// 전역 노출
window.switchFloor=switchFloor;
window.updateSeatType=updateSeatType;
window.handleBookingSubmit=handleBookingSubmit;