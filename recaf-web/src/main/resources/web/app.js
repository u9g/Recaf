// Recaf Web Interface JavaScript

const API_BASE = '/api';
let currentClasses = [];
let selectedClass = null;

// Initialize the application
document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    initSearch();
    checkWorkspaceStatus();
    loadClasses();
});

// Initialize tab switching
function initTabs() {
    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            const tabName = tab.dataset.tab;
            switchTab(tabName);
        });
    });
}

// Switch between tabs
function switchTab(tabName) {
    // Update tab styling
    document.querySelectorAll('.tab').forEach(tab => {
        tab.classList.toggle('active', tab.dataset.tab === tabName);
    });

    // Update content pane visibility
    document.querySelectorAll('.content-pane').forEach(pane => {
        pane.classList.toggle('active', pane.id === tabName + 'Pane');
    });

    // Load content if needed
    if (selectedClass) {
        if (tabName === 'decompiled') {
            loadDecompiledCode(selectedClass);
        }
    }
}

// Initialize search functionality
function initSearch() {
    const searchInput = document.getElementById('classSearch');
    searchInput.addEventListener('input', (e) => {
        filterClasses(e.target.value);
    });
}

// Filter classes based on search term
function filterClasses(searchTerm) {
    const filtered = currentClasses.filter(className =>
        className.toLowerCase().includes(searchTerm.toLowerCase())
    );
    renderClassList(filtered);
}

// Check workspace status
async function checkWorkspaceStatus() {
    try {
        const response = await fetch(`${API_BASE}/workspace/status`);
        const data = await response.json();

        const indicator = document.getElementById('statusIndicator');
        const text = document.getElementById('statusText');

        if (data.hasWorkspace) {
            indicator.classList.remove('no-workspace');
            text.textContent = `Workspace loaded (${data.primaryResourceName})`;
        } else {
            indicator.classList.add('no-workspace');
            text.textContent = 'No workspace loaded';
        }
    } catch (error) {
        console.error('Failed to check workspace status:', error);
        document.getElementById('statusText').textContent = 'Connection error';
    }
}

// Load all classes from the workspace
async function loadClasses() {
    try {
        const response = await fetch(`${API_BASE}/classes`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        currentClasses = data.classes;
        renderClassList(currentClasses);
    } catch (error) {
        console.error('Failed to load classes:', error);
        const classList = document.getElementById('classList');
        classList.innerHTML = '<div class="error">Failed to load classes. Make sure a workspace is loaded.</div>';
    }
}

// Render the class list
function renderClassList(classes) {
    const classList = document.getElementById('classList');

    if (classes.length === 0) {
        classList.innerHTML = '<div class="loading">No classes found</div>';
        return;
    }

    classList.innerHTML = classes.map(className => `
        <div class="class-item" data-class="${className}" onclick="selectClass('${escapeHtml(className)}')">
            ${escapeHtml(className)}
        </div>
    `).join('');
}

// Select a class
async function selectClass(className) {
    selectedClass = className;

    // Update selected styling
    document.querySelectorAll('.class-item').forEach(item => {
        item.classList.toggle('selected', item.dataset.class === className);
    });

    // Load class info
    await loadClassInfo(className);

    // If decompiled tab is active, load decompiled code
    const activeTab = document.querySelector('.tab.active');
    if (activeTab && activeTab.dataset.tab === 'decompiled') {
        await loadDecompiledCode(className);
    }
}

// Load class information
async function loadClassInfo(className) {
    const infoPane = document.getElementById('infoPane');
    infoPane.innerHTML = '<div class="loading">Loading class info...</div>';

    try {
        const response = await fetch(`${API_BASE}/classes/${encodeURIComponent(className)}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        renderClassInfo(data);
    } catch (error) {
        console.error('Failed to load class info:', error);
        infoPane.innerHTML = '<div class="error">Failed to load class information</div>';
    }
}

// Render class information
function renderClassInfo(classData) {
    const infoPane = document.getElementById('infoPane');

    const html = `
        <div class="info-grid">
            <div class="info-label">Class Name:</div>
            <div class="info-value">${escapeHtml(classData.name)}</div>

            <div class="info-label">Package:</div>
            <div class="info-value">${escapeHtml(classData.packageName || 'default')}</div>

            <div class="info-label">Super Class:</div>
            <div class="info-value">${escapeHtml(classData.superName || 'none')}</div>

            <div class="info-label">Access Flags:</div>
            <div class="info-value">${classData.access}</div>
        </div>

        ${classData.interfaces && classData.interfaces.length > 0 ? `
            <div class="list-section">
                <h3>Interfaces (${classData.interfaces.length})</h3>
                ${classData.interfaces.map(iface => `
                    <div class="list-item">${escapeHtml(iface)}</div>
                `).join('')}
            </div>
        ` : ''}

        ${classData.fields && classData.fields.length > 0 ? `
            <div class="list-section">
                <h3>Fields (${classData.fields.length})</h3>
                ${classData.fields.map(field => `
                    <div class="list-item">${escapeHtml(field)}</div>
                `).join('')}
            </div>
        ` : ''}

        ${classData.methods && classData.methods.length > 0 ? `
            <div class="list-section">
                <h3>Methods (${classData.methods.length})</h3>
                ${classData.methods.map(method => `
                    <div class="list-item">${escapeHtml(method)}</div>
                `).join('')}
            </div>
        ` : ''}
    `;

    infoPane.innerHTML = html;
}

// Load decompiled code
async function loadDecompiledCode(className) {
    const decompiledPane = document.getElementById('decompiledPane');
    decompiledPane.innerHTML = '<div class="loading">Decompiling...</div>';

    try {
        const response = await fetch(`${API_BASE}/decompile/${encodeURIComponent(className)}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        renderDecompiledCode(data);
    } catch (error) {
        console.error('Failed to decompile class:', error);
        decompiledPane.innerHTML = '<div class="error">Failed to decompile class</div>';
    }
}

// Render decompiled code
function renderDecompiledCode(data) {
    const decompiledPane = document.getElementById('decompiledPane');

    if (!data.success && data.error) {
        decompiledPane.innerHTML = `
            <div class="error">Decompilation failed: ${escapeHtml(data.error)}</div>
            ${data.text ? `<pre>${escapeHtml(data.text)}</pre>` : ''}
        `;
        return;
    }

    decompiledPane.innerHTML = `
        <div style="margin-bottom: 0.5rem; color: #858585;">
            Decompiled with: ${escapeHtml(data.decompiler)}
        </div>
        <pre>${escapeHtml(data.text)}</pre>
    `;
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
