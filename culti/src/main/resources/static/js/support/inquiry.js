/* inquiry.js */

const csrfToken = $("meta[name='_csrf']").attr("content");
const csrfHeader = $("meta[name='_csrf_header']").attr("content");
$.ajaxSetup({
    beforeSend: function(xhr) {
        if (csrfToken && csrfHeader) xhr.setRequestHeader(csrfHeader, csrfToken);
    }
});

let currentStep = 'title';
let inquiryData = { title: '', content: '' };



$(document).ready(function() {
    // 페이지 로드 시 목록 불러오기
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

// 1. 문의 목록 로드 (경로: /support/inquiry/list)
function loadInquiryList() {
    $.get("/support/inquiry/list", function(data) {
        let html = "";
        if(data && data.length > 0) {
            data.forEach(item => {
                const statusText = item.inquiryStatus === 'PENDING' ? '답변 전' : '답변 완료';
                const statusClass = item.inquiryStatus === 'PENDING' ? 'status_pending' : 'status_answered';
                const dotClass = item.inquiryStatus === 'PENDING' ? 'offline' : '';

                html += `
                <li id="inquiry-${item.inquiryId}" onclick="viewInquiry(${item.inquiryId})">
                    <div class="d-flex bd-highlight">
                        <div class="img_cont">
                            <img src="/images/support/manager.png" class="user_img">
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
        } else {
            html = '<li style="text-align:center; padding:20px; color:#ccc;">문의 내역이 없습니다.</li>';
        }
        $('#inquiryList').html(html);
    });
}

// 2. 새로운 문의 모드 시작
function startNewInquiry() {
    $('.contacts li').removeClass('active_chat'); // 목록 선택 해제
    $('#chatBody').html("");
    $('#chatTitle').text("운영자 (새 문의)");
    $('#chatFooter').show();
    
    // 입력창 초기화 및 활성화
    $('#userInput').attr('disabled', false)
                 .val('')
                 .attr('placeholder', '문의 제목을 입력하세요...')
                 .css('background-color', 'white');
    $('.send_btn').show(); // 전송 버튼 보이기
                 
    currentStep = 'title';
    appendBotMessage("안녕하세요! 어떤 도움이 필요하신가요?<br>먼저 문의하실 사항의 <b>제목</b>을 입력해주세요.");
}

// 3. 기존 문의 클릭 (상세 조회)
function viewInquiry(id) {
    $('.contacts li').removeClass('active_chat');
    $('#inquiry-' + id).addClass('active_chat');

    $.get("/support/inquiry/detail/" + id, function(data) {
        $('#chatBody').html("");
        $('#chatTitle').text("운영자 (" + data.inquiryTitle + ")");
        $('#chatFooter').hide(); // 상세보기 시에는 입력창 숨김

        const dbTime = formatDateTime(data.createdAt);

        appendUserMessage("<b>[문의 제목]</b><br>" + data.inquiryTitle, dbTime);
        appendUserMessage("<b>[문의 내용]</b><br>" + data.inquiryContent, dbTime);
        
        if(data.inquiryAnswer) {
            appendBotMessage("<b>[운영자 답변]</b><br>" + data.inquiryAnswer, dbTime);
        } else {
            appendBotMessage("아직 답변을 준비 중입니다.", dbTime);
        }
    }).fail(function() {
        alert("상세 내역을 불러오는 데 실패했습니다.");
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
        appendBotMessage("<b>문의 내용</b>을 입력하세요.");
        $('#userInput').attr('placeholder', '상세 내용을 입력하세요...');
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

// 확인 버튼들 표시 (수정/전송 선택 시 레이아웃 유지)
function showConfirmation() {
    appendBotMessage(`입력하신 내용이 맞나요?<br><br><b>제목:</b> ${inquiryData.title}<br><b>내용:</b> ${inquiryData.content}`);
    let btnHtml = `
        <div class="mt-2 confirmation-buttons">
            <button class="btn btn-sm btn-info mr-1" onclick="editField('title')">제목 수정</button>
            <button class="btn btn-sm btn-info mr-1" onclick="editField('content')">내용 수정</button>
            <button class="btn btn-sm btn-primary" onclick="saveInquiry()">수정 사항 없음</button>
        </div>`;
    appendBotMessage(btnHtml);
    
    // 버튼 선택 전까지 입력창 잠시 비활성화
    $('#userInput').attr('disabled', true).attr('placeholder', '위 버튼 중 하나를 선택해주세요.');
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
        <div class="mt-2 confirmation-buttons">
            <button class="btn btn-sm btn-info mr-1" onclick="editField('title')">제목 수정</button>
            <button class="btn btn-sm btn-info mr-1" onclick="editField('content')">내용 수정</button>
            <button class="btn btn-sm btn-primary" onclick="saveInquiry()">확인 및 전송</button>
        </div>`;
    appendBotMessage(btnHtml);
    $('#userInput').attr('disabled', true);
}


// 최종 저장
function saveInquiry() {
    // 서버 컨트롤러(@RequestParam)에서 지정한 이름인 
    // "title"과 "content"로 정확히 맞춰서 보냅니다.
    const sendData = { 
        title: inquiryData.title, 
        content: inquiryData.content 
    };

    $.post("/support/inquiry/save", sendData)
    .done(function(res) {
        // 컨트롤러가 return "success"; 를 하므로 문자열 비교
        if(res === "success") {
            appendBotMessage("<b>문의가 정상적으로 접수되었습니다.</b><br>운영자가 확인 후 답변 드릴 예정입니다. 감사합니다!");
            $('#chatFooter').hide();
            loadInquiryList(); 
        } else {
            appendBotMessage("저장 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
    })
    .fail(function(xhr) {
        console.error("Error Status:", xhr.status);
        if(xhr.status === 400) {
            // 이제 이 에러가 나지 않을 거예요!
            appendBotMessage("서버 데이터 전송 오류가 발생했습니다.");
        } else if(xhr.status === 403) {
            appendBotMessage("권한이 없거나 세션이 만료되었습니다. 다시 로그인해 주세요.");
        } else {
            appendBotMessage("시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    });
}


// 말풍선 추가 함수들
function appendBotMessage(msg, time = "") {
    if(!time) {
        let now = new Date();
        time = now.getHours().toString().padStart(2, '0') + ":" + now.getMinutes().toString().padStart(2, '0');
    }

    let html = `
        <div class="d-flex justify-content-start mb-4">
            <div class="img_cont_msg"><img src="/images/support/manager.png" style="width:40px; height:40px; border-radius:50%;"></div>
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
        time = now.getHours().toString().padStart(2, '0') + ":" + now.getMinutes().toString().padStart(2, '0');
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
    if($('#chatBody').length > 0) {
        $('#chatBody').scrollTop($('#chatBody')[0].scrollHeight); 
    }
}