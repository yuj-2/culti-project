/* inquiry.js */

let currentStep = 'title';
let inquiryData = { title: '', content: '' };

$(document).ready(function() {
    loadInquiryList();
    startNewInquiry();

    // 엔터키 전송 로직
    $(document).on('keydown', '#userInput', function(e) {
        if (e.keyCode == 13 && !e.shiftKey) {
            e.preventDefault();
            handleUserInput();
        }
    });
});

// 날짜 포맷팅 함수
function formatDateTime(dateTimeStr) {
    if(!dateTimeStr) return "";
    return dateTimeStr.replace('T', ' ').split('.')[0];
}

// 1. 문의 목록 로드
function loadInquiryList() {
    $.get("/inquiry/list", function(data) {
        let html = "";
        data.forEach(item => {
            const statusText = item.inquiryStatus === 'PENDING' ? '답변 전' : '답변 완료';
            const statusClass = item.inquiryStatus === 'PENDING' ? 'status_pending' : 'status_answered';
            const dotClass = item.inquiryStatus === 'PENDING' ? 'offline' : '';

            html += `
            <li id="inquiry-${item.inquiryId}" onclick="viewInquiry(${item.inquiryId})">
                <div class="d-flex bd-highlight">
                    <div class="img_cont">
                        <img src="/images/manager.png" class="user_img">
                        <span class="online_icon ${dotClass}"></span>
                    </div>
                    <div class="user_info">
                        <span>운영자</span>
                        <p>${item.inquiryTitle}</p>
                        <span class="status_label ${statusClass}">${statusText}</span>
                    </div>
                </div>
            </li>`;
        });
        $('#inquiryList').html(html);
    });
}

// 2. 새로운 문의 모드 시작
function startNewInquiry() {
    $('#chatBody').html("");
    $('#chatTitle').text("운영자 (새 문의)");
    $('#chatFooter').show();
    $('#userInput').attr('disabled', false).attr('placeholder', '문의 내용을 입력하세요...');
    currentStep = 'title';
    appendBotMessage("안녕하세요! 어떤 도움이 필요하신가요?<br>먼저 문의하실 <b>제목</b>을 입력해주세요.");
}

// 3. 기존 문의 클릭 (조회)
function viewInquiry(id) {
    $('.contacts li').removeClass('active_chat');
    $('#inquiry-' + id).addClass('active_chat');

    $.get("/inquiry/detail/" + id, function(data) {
        $('#chatBody').html("");
        $('#chatTitle').text("운영자 (" + data.inquiryTitle + ")");
        $('#chatFooter').hide();

        const dbTime = formatDateTime(data.createdAt);

        appendUserMessage("<b>[문의 제목]</b><br>" + data.inquiryTitle, dbTime);
        appendUserMessage("<b>[문의 내용]</b><br>" + data.inquiryContent, dbTime);
        
        if(data.inquiryAnswer) {
            appendBotMessage("<b>[운영자 답변]</b><br>" + data.inquiryAnswer, dbTime);
        } else {
            appendBotMessage("아직 답변을 준비 중입니다.", dbTime);
        }
    });
}

// 메시지 처리 메인 로직
function handleUserInput() {
    let val = $('#userInput').val().trim();
    if(!val) return;
    
    appendUserMessage(val);
    $('#userInput').val("");

    if(currentStep === 'title') {
        inquiryData.title = val;
        currentStep = 'content';
        appendBotMessage("상세한 <b>문의 내용</b>을 입력해 주시겠어요?");
    } 
    else if(currentStep === 'content') {
        inquiryData.content = val;
        currentStep = 'confirm';
        showConfirmation();
    }
    else if(currentStep === 'editTitle') {
        inquiryData.title = val;
        askConfirmAgain();
    }
    else if(currentStep === 'editContent') {
        inquiryData.content = val;
        askConfirmAgain();
    }
}

// 확인 버튼들 표시
function showConfirmation() {
    appendBotMessage(`입력하신 내용이 맞나요?<br><br><b>제목:</b> ${inquiryData.title}<br><b>내용:</b> ${inquiryData.content}`);
    let btnHtml = `
        <div class="mt-2">
            <button class="btn btn-sm btn-primary mr-1" onclick="editField('title')">제목 수정</button>
            <button class="btn btn-sm btn-primary mr-1" onclick="editField('content')">내용 수정</button>
            <button class="btn btn-sm btn-primary" onclick="saveInquiry()">수정 사항 없음</button>
        </div>`;
    appendBotMessage(btnHtml);
    $('#userInput').attr('disabled', true).attr('placeholder', '버튼을 선택해주세요');
}

function editField(type) {
    $('#userInput').attr('disabled', false).focus().attr('placeholder', '수정할 내용을 입력하세요...');
    if(type === 'title') {
        currentStep = 'editTitle';
        appendBotMessage("새로운 <b>제목</b>을 입력해주세요.");
    } else {
        currentStep = 'editContent';
        appendBotMessage("새로운 <b>내용</b>을 입력해주세요.");
    }
}

function askConfirmAgain() {
    currentStep = 'confirm';
    appendBotMessage(`수정되었습니다. 다시 확인해주세요.<br><br><b>제목:</b> ${inquiryData.title}<br><b>내용:</b> ${inquiryData.content}`);
    let btnHtml = `
        <div class="mt-2">
            <button class="btn btn-sm btn-primary mr-1" onclick="editField('title')">제목 수정</button>
            <button class="btn btn-sm btn-primary mr-1" onclick="editField('content')">내용 수정</button>
            <button class="btn btn-sm btn-primary" onclick="saveInquiry()">수정 사항 없음</button>
        </div>`;
    appendBotMessage(btnHtml);
    $('#userInput').attr('disabled', true);
}

// 최종 저장
function saveInquiry() {
    $.post("/inquiry/save", { 
        title: inquiryData.title, 
        content: inquiryData.content 
    }, function(res) {
        if(res === "success") {
            appendBotMessage("<b>문의가 정상적으로 접수되었습니다.</b><br>운영자가 확인 후 답변 드릴 예정입니다. 감사합니다!");
            $('#chatFooter').hide();
            loadInquiryList();
        } else {
            appendBotMessage("저장 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
    });
}

// 말풍선 추가 함수들
function appendBotMessage(msg, time = "") {
    if(!time) {
        let now = new Date();
        time = now.getFullYear() + "-" + (now.getMonth()+1).toString().padStart(2, '0') + "-" + now.getDate().toString().padStart(2, '0') + " " +
               now.getHours().toString().padStart(2, '0') + ":" + now.getMinutes().toString().padStart(2, '0') + ":" + now.getSeconds().toString().padStart(2, '0');
    }

    let html = `
        <div class="d-flex justify-content-start mb-4">
            <div class="img_cont_msg"><img src="/images/manager.png" style="width:40px; height:40px; border-radius:50%;"></div>
            <div class="msg_cotainer">
                ${msg}
                <span class="msg_time">${time}</span>
            </div>
        </div>`;
    $('#chatBody').append(html);
    scrollToBottom();
}

function appendUserMessage(msg, time = "") {
    if(!time) {
        let now = new Date();
        time = now.getFullYear() + "-" + (now.getMonth()+1).toString().padStart(2, '0') + "-" + now.getDate().toString().padStart(2, '0') + " " +
               now.getHours().toString().padStart(2, '0') + ":" + now.getMinutes().toString().padStart(2, '0') + ":" + now.getSeconds().toString().padStart(2, '0');
    }

    let html = `
        <div class="d-flex justify-content-end mb-4">
            <div class="msg_cotainer_send">
                ${msg}
                <span class="msg_time_send">${time}</span>
            </div>
        </div>`;
    $('#chatBody').append(html);
    scrollToBottom();
}

function scrollToBottom() { 
    $('#chatBody').scrollTop($('#chatBody')[0].scrollHeight); 
}