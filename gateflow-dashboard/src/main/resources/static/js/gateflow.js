document.addEventListener('DOMContentLoaded', () => {
    const searchInputs = document.querySelectorAll('.table-search-input');
    searchInputs.forEach(input => {
        const targetTableId = input.getAttribute('data-table-target');
        const table = document.getElementById(targetTableId);
        if (!table) return;

        input.addEventListener('keyup', (e) => {
            const query = e.target.value.toLowerCase().trim();
            const rows = table.querySelectorAll('tbody tr');
            rows.forEach(row => {
                const text = row.innerText.toLowerCase();
                row.style.display = text.includes(query) ? '' : 'none';
            });
        });
    });

    const jsonContainers = document.querySelectorAll('.format-json');
    jsonContainers.forEach(container => {
        try {
            const raw = container.innerText.trim();
            if (raw.startsWith('{') || raw.startsWith('[')) {
                const parsed = JSON.parse(raw);
                container.innerText = JSON.stringify(parsed, null, 2);
            }
        } catch (e) {
        }
    });

    const methodSelect = document.getElementById('testerMethod');
    const bodyContainer = document.getElementById('testerBodyContainer');
    if (methodSelect && bodyContainer) {
        const updateBodyVisibility = () => {
            const m = methodSelect.value;
            if (m === 'GET' || m === 'DELETE') {
                bodyContainer.style.opacity = '0.5';
            } else {
                bodyContainer.style.opacity = '1';
            }
        };
        methodSelect.addEventListener('change', updateBodyVisibility);
        updateBodyVisibility();
    }
});

function confirmAction(message, formId) {
    if (confirm(message)) {
        document.getElementById(formId).submit();
    }
}
