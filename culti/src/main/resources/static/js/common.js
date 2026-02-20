window.addEventListener('DOMContentLoaded', function() {
    
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

    const menuContainer = document.getElementById('ticket-menu-container');
    const dropdown = document.getElementById('ticket-dropdown');
    const chevron = document.getElementById('ticket-chevron');

    if(menuContainer && dropdown && chevron) {
        menuContainer.addEventListener('mouseenter', function() {
            dropdown.classList.remove('hidden');
            chevron.style.transform = 'rotate(180deg)';
        });

        menuContainer.addEventListener('mouseleave', function() {
            dropdown.classList.add('hidden');
            chevron.style.transform = 'rotate(0deg)';
        });
    }
});