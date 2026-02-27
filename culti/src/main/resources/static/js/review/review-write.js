document.addEventListener('DOMContentLoaded', function() {
    
    // 1. 별점 로직
    let currentRating = 0;
    const starBtns = document.querySelectorAll('.star-btn');
    const ratingInput = document.getElementById('ratingValue');

    starBtns.forEach((btn, index) => {
        // 클릭 시 점수 확정
        btn.addEventListener('click', () => {
            currentRating = parseInt(btn.getAttribute('data-rating'));
            ratingInput.value = currentRating;
            updateStars(currentRating, false);
        });

        // 마우스 올렸을 때
        btn.addEventListener('mouseenter', () => {
            updateStars(index + 1, true);
        });

        // 마우스 뗐을 때
        btn.addEventListener('mouseleave', () => {
            updateStars(currentRating, false);
        });
    });

    function updateStars(count, isHover) {
        starBtns.forEach((btn, index) => {
            // 버튼 안에 있는 진짜 아이콘(svg)을 찾아서 색칠
            const icon = btn.querySelector('svg'); 
            
            if (icon) {
                if (index < count) {
                    icon.classList.remove('text-gray-300');
                    icon.classList.add('fill-current'); // 색상 채우기
                    
                    if (isHover) {
                        icon.classList.add('text-yellow-400');
                        icon.classList.remove('text-[#503396]');
                    } else {
                        icon.classList.add('text-[#503396]'); // 확정은 보라색
                        icon.classList.remove('text-yellow-400');
                    }
                } else {
                    // 비어있는 별
                    icon.classList.remove('fill-current', 'text-[#503396]', 'text-yellow-400');
                    icon.classList.add('text-gray-300');
                }
            }
        });
    }

    // 2. 글자 수 세기
    const reviewTextarea = document.getElementById('reviewText');
    const charCounter = document.getElementById('charCounter');

    reviewTextarea.addEventListener('input', (e) => {
        const length = e.target.value.length;
        charCounter.textContent = `${length} / 1000`;
        
        if (length > 1000) {
            e.target.value = e.target.value.substring(0, 1000);
            charCounter.textContent = '1000 / 1000';
        }
    });

    // 3. 사진 업로드 및 미리보기
    const photoUpload = document.getElementById('photoUpload');
    const photoPreview = document.getElementById('photoPreview');
    let dataTransfer = new DataTransfer(); // 서버로 보낼 파일들을 관리하는 바구니

    photoUpload.addEventListener('change', (e) => {
        const files = Array.from(e.target.files);
        
        files.forEach(file => {
            if (dataTransfer.items.length >= 3) {
                alert('최대 3장까지 업로드할 수 있습니다.');
                return;
            }

            if (!file.type.startsWith('image/')) {
                alert('이미지 파일만 업로드할 수 있습니다.');
                return;
            }

            // 바구니에 파일 담기
            dataTransfer.items.add(file);
            
            // 화면에 미리보기 그리기
            const reader = new FileReader();
            reader.onload = (event) => {
                const photoElement = document.createElement('div');
                photoElement.className = 'relative w-24 h-24 rounded-lg overflow-hidden group border border-gray-200';
                photoElement.innerHTML = `
                    <img src="${event.target.result}" alt="업로드된 사진" class="w-full h-full object-cover">
                    <button type="button" class="absolute top-1 right-1 w-6 h-6 bg-black bg-opacity-60 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                            onclick="removePhoto(this, '${file.name}')">
                        <i data-lucide="x" class="w-4 h-4 text-white"></i>
                    </button>
                `;
                photoPreview.appendChild(photoElement);
                lucide.createIcons(); // 엑스 표시 아이콘 렌더링
            };
            reader.readAsDataURL(file);
        });

        // 실제 input 태그에 우리가 만든 바구니(dataTransfer) 덮어쓰기
        photoUpload.files = dataTransfer.files; 
    });

    // 삭제 버튼 누를 때 실행되는 함수
    window.removePhoto = function(button, fileName) {
        // 새로운 바구니를 만들어서 지울 파일만 빼고 옮겨 담음
        const newDataTransfer = new DataTransfer();
        Array.from(dataTransfer.files).forEach(file => {
            if (file.name !== fileName) {
                newDataTransfer.items.add(file);
            }
        });
        
        dataTransfer = newDataTransfer; // 바구니 교체
        photoUpload.files = dataTransfer.files; // 실제 input 태그 업데이트
        
        button.closest('div').remove(); // 화면에서 썸네일 날리기
    };

    // 4. 진짜 백엔드로 폼 전송(Submit) 전 마지막 검증
    const reviewForm = document.getElementById('reviewForm');
    reviewForm.addEventListener('submit', (e) => {
        // 별점 안 매겼으면 폼 전송 막기
        if (currentRating === 0) {
            e.preventDefault(); 
            alert('공연은 어떠셨나요? 별점을 선택해주세요.');
            return;
        }

        // 글자 수 검증
        const reviewText = reviewTextarea.value.trim();
        if (reviewText.length < 10) {
            e.preventDefault();
            alert('솔직한 관람 후기를 최소 10자 이상 작성해주세요.');
            reviewTextarea.focus();
            return;
        }

    });

});