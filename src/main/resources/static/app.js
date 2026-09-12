// TASKFLOW FRONTEND APPLICATION SCRIPT (VANILLA JS + SVG VISUALIZERS)
document.addEventListener('DOMContentLoaded', () => {
    let currentTasks = [];
    let historyTasks = [];
    let searchDebounceTimer = null;

    // INITIALIZATION
    initTheme();
    setupEventListeners();
    refreshAllData();
    setInterval(refreshStats, CONFIG.STATS_REFRESH_MS);

    // REST API HELPER
    async function fetchApi(endpoint, options = {}) {
        try {
            const url = `${CONFIG.API_BASE_URL}${endpoint}`;
            const res = await fetch(url, {
                headers: { 'Content-Type': 'application/json', ...options.headers },
                ...options
            });
            const data = await res.json();

            if (!res.ok || !data.success) {
                const errorMsg = data.error || `HTTP ${res.status} Error`;
                showToast(errorMsg, 'error');
                throw new Error(errorMsg);
            }
            return data.data;
        } catch (err) {
            console.error(`API Error [${endpoint}]:`, err);
            throw err;
        }
    }

    // DATA REFRESHERS
    async function refreshAllData() {
        await Promise.all([
            refreshStats(),
            loadTasks(),
            loadHistory(),
            renderHeapTree(),
            renderGraph()
        ]);
    }

    async function refreshStats() {
        try {
            const health = await fetchApi('/health');
            document.getElementById('healthStatus').textContent = `● ${health.status}`;
            document.getElementById('totalTasksCount').textContent = health.totalTasksTracked;
            document.getElementById('pendingCompletedRatio').textContent = `${health.pendingTasksCount} / ${health.completedTasksCount}`;
            document.getElementById('serverUptime').textContent = `${health.uptimeSeconds}s`;

            const nextTask = await fetchApi('/tasks/next');
            const nextEl = document.getElementById('nextTaskPeek');
            if (nextTask) {
                nextEl.textContent = `[P${nextTask.priority}] ${nextTask.title}`;
            } else {
                nextEl.textContent = 'None (Heap Empty)';
            }
        } catch (e) {
            document.getElementById('healthStatus').textContent = '● Offline';
        }
    }

    async function loadTasks() {
        try {
            const sortKey = document.getElementById('sortKeySelect').value;
            const sortAlgo = document.getElementById('sortAlgoSelect').value;
            currentTasks = await fetchApi(`/tasks?sortBy=${sortKey}&algorithm=${sortAlgo}`);
            renderTaskBoard(currentTasks);
            updateDependencySelects(currentTasks);
        } catch (e) {
            console.error('Failed to load tasks', e);
        }
    }

    async function loadHistory() {
        try {
            historyTasks = await fetchApi('/history');
            renderHistoryLog(historyTasks);
        } catch (e) {
            console.error('Failed to load history', e);
        }
    }

    // 1. KANBAN TASK BOARD RENDERER
    function renderTaskBoard(tasks) {
        const pendingContainer = document.getElementById('pendingContainer');
        const highestContainer = document.getElementById('highestPriorityContainer');
        const completedContainer = document.getElementById('completedContainer');

        pendingContainer.innerHTML = '';
        highestContainer.innerHTML = '';
        completedContainer.innerHTML = '';

        const pending = tasks.filter(t => !t.completed);
        const completed = tasks.filter(t => t.completed);

        document.getElementById('pendingCountBadge').textContent = pending.length;
        document.getElementById('completedCountBadge').textContent = completed.length;

        // MinHeap Root (Highest Priority)
        if (pending.length > 0) {
            // Priority sort to find min
            const minTask = [...pending].sort((a,b) => a.priority - b.priority)[0];
            highestContainer.appendChild(createTaskCard(minTask, true));
        } else {
            highestContainer.innerHTML = `<div class="empty-state">No active tasks in heap.</div>`;
        }

        // Pending Column Cards
        pending.forEach(t => {
            pendingContainer.appendChild(createTaskCard(t, false));
        });

        // Completed Column Cards
        completed.forEach(t => {
            completedContainer.appendChild(createTaskCard(t, false));
        });
    }

    function createTaskCard(t, isHighlight) {
        const card = document.createElement('div');
        card.className = `task-card ${isHighlight ? 'highlight-card' : ''}`;
        
        const tagsHtml = (t.tags || []).map(tag => `<span class="tag-pill">#${tag}</span>`).join('');
        const depsHtml = (t.dependencies || []).length > 0 ? `<div class="deps-info">Deps: ${t.dependencies.join(', ')}</div>` : '';

        const dateDisplayHtml = t.completed
            ? `<span class="task-completed-date">📅 Completed: ${t.completedAt ? t.completedAt : 'Just now'}</span>`
            : `<span class="task-deadline">📅 ${t.deadline ? t.deadline : 'No Deadline'}</span>`;

        card.innerHTML = `
            <div class="card-top">
                <span class="task-id">ID: ${t.id}</span>
                <span class="prio-badge prio-${t.priority}">Priority ${t.priority}</span>
            </div>
            <div class="task-title">${escapeHtml(t.title)}</div>
            ${t.description ? `<div class="task-desc">${escapeHtml(t.description)}</div>` : ''}
            <div class="tags-row">${tagsHtml}</div>
            ${depsHtml}
            <div class="card-footer">
                ${dateDisplayHtml}
                ${!t.completed ? `<button class="btn btn-sm btn-primary btn-complete" data-id="${t.id}">✅ Complete</button>` : `<span class="text-success" style="font-weight: 600;">Completed</span>`}
            </div>
        `;

        const completeBtn = card.querySelector('.btn-complete');
        if (completeBtn) {
            completeBtn.addEventListener('click', () => completeTask(t.id));
        }

        return card;
    }

    // TASK ACTIONS
    async function completeTask(id) {
        try {
            const completed = await fetchApi(`/tasks/${id}/complete`, { method: 'POST' });
            showToast(`Completed task: ${completed.title}`, 'success');
            refreshAllData();
        } catch (e) {}
    }

    async function undoLastAction() {
        try {
            const res = await fetchApi('/undo', { method: 'POST' });
            showToast(res.message, 'success');
            refreshAllData();
        } catch (e) {}
    }

    // 2. MINHEAP BINARY TREE VISUALIZER (SVG)
    async function renderHeapTree() {
        const svg = document.getElementById('heapSvg');
        const arrayContainer = document.getElementById('heapArrayContainer');
        svg.innerHTML = '';
        arrayContainer.innerHTML = '';

        const pending = currentTasks.filter(t => !t.completed);
        if (pending.length === 0) {
            svg.innerHTML = `<text x="50%" y="50%" text-anchor="middle" fill="var(--text-muted)" font-size="16">MinHeap is currently empty.</text>`;
            return;
        }

        // Sort into Heap ordering representation
        const heap = [...pending].sort((a,b) => a.priority - b.priority);

        // Render Array Grid
        heap.forEach((t, i) => {
            const cell = document.createElement('div');
            cell.className = `heap-array-cell ${i === 0 ? 'root-cell' : ''}`;
            cell.innerHTML = `
                <div class="cell-index">[${i}]</div>
                <div class="cell-id">${t.id}</div>
                <div class="cell-prio">Prio ${t.priority}</div>
            `;
            arrayContainer.appendChild(cell);
        });

        // Render Binary Tree in SVG
        const width = svg.clientWidth || 800;
        const startY = 40;
        const levelHeight = 70;

        function getCoords(index) {
            const level = Math.floor(Math.log2(index + 1));
            const posInLevel = index - (Math.pow(2, level) - 1);
            const totalInLevel = Math.pow(2, level);
            const sectionWidth = width / totalInLevel;
            const x = sectionWidth * posInLevel + sectionWidth / 2;
            const y = startY + level * levelHeight;
            return { x, y };
        }

        // Draw Edge Lines
        for (let i = 0; i < heap.length; i++) {
            const parentCoords = getCoords(i);
            const leftChild = 2 * i + 1;
            const rightChild = 2 * i + 2;

            if (leftChild < heap.length) {
                const childCoords = getCoords(leftChild);
                drawLine(svg, parentCoords.x, parentCoords.y, childCoords.x, childCoords.y);
            }
            if (rightChild < heap.length) {
                const childCoords = getCoords(rightChild);
                drawLine(svg, parentCoords.x, parentCoords.y, childCoords.x, childCoords.y);
            }
        }

        // Draw Nodes
        for (let i = 0; i < heap.length; i++) {
            const coords = getCoords(i);
            drawNode(svg, coords.x, coords.y, heap[i], i === 0);
        }
    }

    function drawLine(svg, x1, y1, x2, y2) {
        const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
        line.setAttribute('x1', x1);
        line.setAttribute('y1', y1);
        line.setAttribute('x2', x2);
        line.setAttribute('y2', y2);
        line.setAttribute('stroke', 'rgba(255,255,255,0.2)');
        line.setAttribute('stroke-width', '2');
        svg.appendChild(line);
    }

    function drawNode(svg, x, y, task, isRoot) {
        const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        
        const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
        circle.setAttribute('cx', x);
        circle.setAttribute('cy', y);
        circle.setAttribute('r', '22');
        circle.setAttribute('fill', isRoot ? 'rgba(56, 189, 248, 0.25)' : 'rgba(15, 23, 42, 0.9)');
        circle.setAttribute('stroke', isRoot ? 'var(--accent-cyan)' : 'var(--accent-purple)');
        circle.setAttribute('stroke-width', isRoot ? '3' : '2');
        g.appendChild(circle);

        const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        text.setAttribute('x', x);
        text.setAttribute('y', y + 4);
        text.setAttribute('text-anchor', 'middle');
        text.setAttribute('fill', '#ffffff');
        text.setAttribute('font-size', '11');
        text.setAttribute('font-weight', 'bold');
        text.textContent = `P${task.priority}:${task.id}`;
        g.appendChild(text);

        svg.appendChild(g);
    }

    // 3. DEPENDENCY GRAPH & TOPOLOGICAL SORT (SVG)
    async function renderGraph() {
        const svg = document.getElementById('graphSvg');
        const banner = document.getElementById('topoSequenceBanner');
        svg.innerHTML = '';
        banner.classList.add('hidden');

        if (currentTasks.length === 0) {
            svg.innerHTML = `<text x="50%" y="50%" text-anchor="middle" fill="var(--text-muted)">No tasks to display in graph.</text>`;
            return;
        }

        try {
            // Attempt Topological Sort
            const topoOrder = await fetchApi('/tasks/execution-order');
            banner.classList.remove('hidden', 'cycle-error');
            const seqStr = topoOrder.map((t, idx) => `<b>${idx + 1}.</b> ${t.id} (${escapeHtml(t.title)})`).join(' ➔ ');
            banner.innerHTML = `<b>Topological Execution Order (Kahn's Algorithm):</b><br>${seqStr}`;

            drawGraphLayout(svg, currentTasks, false);
        } catch (err) {
            // Cycle detected!
            banner.classList.remove('hidden');
            banner.classList.add('cycle-error');
            banner.innerHTML = `⚠️ <b>CIRCULAR DEPENDENCY DETECTED:</b> ${escapeHtml(err.message)}`;
            drawGraphLayout(svg, currentTasks, true);
        }
    }

    function drawGraphLayout(svg, tasks, hasCycle) {
        const width = svg.clientWidth || 800;
        const height = 400;
        const nodeMap = new Map();

        // Calculate layout coordinates (circular layout)
        const radius = Math.min(width, height) / 2.5;
        const centerX = width / 2;
        const centerY = height / 2;

        tasks.forEach((t, i) => {
            const angle = (i / tasks.length) * 2 * Math.PI - Math.PI / 2;
            const x = centerX + radius * Math.cos(angle);
            const y = centerY + radius * Math.sin(angle);
            nodeMap.set(t.id, { x, y, task: t });
        });

        // Draw Directed Arrows
        tasks.forEach(t => {
            const targetNode = nodeMap.get(t.id);
            if (t.dependencies) {
                t.dependencies.forEach(prereqId => {
                    const sourceNode = nodeMap.get(prereqId);
                    if (sourceNode && targetNode) {
                        drawArrow(svg, sourceNode.x, sourceNode.y, targetNode.x, targetNode.y, hasCycle);
                    }
                });
            }
        });

        // Draw Graph Nodes
        nodeMap.forEach(node => {
            const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
            
            const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
            rect.setAttribute('x', node.x - 45);
            rect.setAttribute('y', node.y - 18);
            rect.setAttribute('width', '90');
            rect.setAttribute('height', '36');
            rect.setAttribute('rx', '8');
            rect.setAttribute('fill', hasCycle ? 'rgba(239, 68, 68, 0.25)' : 'rgba(15, 23, 42, 0.85)');
            rect.setAttribute('stroke', hasCycle ? 'var(--accent-red)' : 'var(--accent-cyan)');
            rect.setAttribute('stroke-width', '2');
            g.appendChild(rect);

            const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            text.setAttribute('x', node.x);
            text.setAttribute('y', node.y + 4);
            text.setAttribute('text-anchor', 'middle');
            text.setAttribute('fill', '#ffffff');
            text.setAttribute('font-size', '12');
            text.setAttribute('font-weight', 'bold');
            text.textContent = node.task.id;
            g.appendChild(text);

            svg.appendChild(g);
        });
    }

    function drawArrow(svg, x1, y1, x2, y2, isError) {
        const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
        line.setAttribute('x1', x1);
        line.setAttribute('y1', y1);
        line.setAttribute('x2', x2);
        line.setAttribute('y2', y2);
        line.setAttribute('stroke', isError ? 'var(--accent-red)' : 'var(--accent-purple)');
        line.setAttribute('stroke-width', '2');
        line.setAttribute('marker-end', isError ? 'url(#arrow-red)' : 'url(#arrow-purple)');
        svg.appendChild(line);
    }

    // 4. SORT RACE PANEL (BENCHMARK)
    async function runSortRace() {
        try {
            const data = await fetchApi('/tasks/benchmark');
            const merge = data.mergeSort;
            const quick = data.quickSort;

            document.getElementById('mergeTime').textContent = `${merge.durationMillis.toFixed(3)} ms (${merge.durationNanos} ns)`;
            document.getElementById('mergeComp').textContent = merge.estimatedComparisons;
            document.getElementById('mergeProgress').style.width = '100%';

            document.getElementById('quickTime').textContent = `${quick.durationMillis.toFixed(3)} ms (${quick.durationNanos} ns)`;
            document.getElementById('quickComp').textContent = quick.estimatedComparisons;
            document.getElementById('quickProgress').style.width = '100%';

            const winnerBanner = document.getElementById('raceWinnerBanner');
            winnerBanner.classList.remove('hidden');
            winnerBanner.innerHTML = `🏆 <b>Algorithm Race Winner:</b> <span class="text-accent">${data.winner}</span> (Faster execution time!)`;
            showToast(`Race Completed! Winner: ${data.winner}`, 'success');
        } catch (e) {}
    }

    // 5. TRIE AUTOCOMPLETE SEARCH
    function setupTrieSearch() {
        const input = document.getElementById('trieSearchInput');
        const dropdown = document.getElementById('autocompleteDropdown');

        input.addEventListener('input', (e) => {
            clearTimeout(searchDebounceTimer);
            const prefix = e.target.value.trim();

            if (prefix.length === 0) {
                dropdown.classList.add('hidden');
                return;
            }

            searchDebounceTimer = setTimeout(async () => {
                try {
                    const results = await fetchApi(`/tasks/search?prefix=${encodeURIComponent(prefix)}`);
                    renderAutocompleteDropdown(results, dropdown);
                } catch (err) {}
            }, CONFIG.SEARCH_DEBOUNCE_MS);
        });

        document.addEventListener('click', (e) => {
            if (!input.contains(e.target) && !dropdown.contains(e.target)) {
                dropdown.classList.add('hidden');
            }
        });
    }

    function renderAutocompleteDropdown(results, dropdown) {
        dropdown.innerHTML = '';
        if (results.length === 0) {
            dropdown.classList.add('hidden');
            return;
        }

        results.forEach(t => {
            const item = document.createElement('div');
            item.className = 'autocomplete-item';
            item.innerHTML = `
                <div>
                    <strong>[${t.id}] ${escapeHtml(t.title)}</strong>
                    <span class="text-muted"> - Priority ${t.priority}</span>
                </div>
                <span class="tag-pill">Trie Match</span>
            `;
            item.addEventListener('click', () => {
                dropdown.classList.add('hidden');
                showToast(`Selected task: ${t.title}`, 'success');
            });
            dropdown.appendChild(item);
        });
        dropdown.classList.remove('hidden');
    }

    // 6. HISTORY LOG (QUEUE FIFO)
    function renderHistoryLog(history) {
        const container = document.getElementById('historyList');
        container.innerHTML = '';

        if (history.length === 0) {
            container.innerHTML = `<div class="empty-state">No completed task history recorded in Queue.</div>`;
            return;
        }

        history.forEach((t, i) => {
            const item = document.createElement('div');
            item.className = 'history-item';
            const timeStr = t.completedAt ? t.completedAt : (t.deadline ? t.deadline : 'N/A');
            item.innerHTML = `
                <div>
                    <span><b>#${i + 1}</b> Completed: [${t.id}] <b>${escapeHtml(t.title)}</b></span>
                    <div style="font-size: 0.82rem; color: var(--text-muted); margin-top: 4px;">📅 Completed: ${escapeHtml(timeStr)}</div>
                </div>
                <span class="text-success">Queue FIFO Position ${i + 1}</span>
            `;
            container.appendChild(item);
        });
    }

    // EVENT LISTENERS & MODALS
    function setupEventListeners() {
        // Tabs
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
                document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
                btn.classList.add('active');
                document.getElementById(btn.dataset.tab).classList.add('active');
            });
        });

        // Sort & Filter Controls
        document.getElementById('btnApplySort').addEventListener('click', loadTasks);
        document.getElementById('btnFilterTag').addEventListener('click', async () => {
            const tag = document.getElementById('tagSearchInput').value.trim();
            if (tag) {
                const results = await fetchApi(`/tasks/tag/${encodeURIComponent(tag)}`);
                renderTaskBoard(results);
                showToast(`HashMap tag lookup for '#${tag}': ${results.length} found`, 'success');
            } else {
                loadTasks();
            }
        });

        // Global Buttons
        document.getElementById('btnUndo').addEventListener('click', undoLastAction);
        document.getElementById('btnRefreshHeap').addEventListener('click', renderHeapTree);
        document.getElementById('btnComputeTopo').addEventListener('click', renderGraph);
        document.getElementById('btnRunSortRace').addEventListener('click', runSortRace);

        // Modals
        const addModal = document.getElementById('addTaskModal');
        document.getElementById('btnAddTask').addEventListener('click', () => addModal.classList.remove('hidden'));
        document.getElementById('btnCloseAddModal').addEventListener('click', () => addModal.classList.add('hidden'));
        document.getElementById('btnCancelAdd').addEventListener('click', () => addModal.classList.add('hidden'));

        document.getElementById('addTaskForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const title = document.getElementById('taskTitleInput').value.trim();
            const description = document.getElementById('taskDescInput').value.trim();
            const priority = parseInt(document.getElementById('taskPriorityInput').value);
            const deadline = document.getElementById('taskDeadlineInput').value;
            const tags = document.getElementById('taskTagsInput').value.split(',').map(s => s.trim()).filter(Boolean);

            const payload = {
                title, description, priority,
                deadline: deadline ? deadline.replace('T', ' ') : null,
                tags
            };

            try {
                const created = await fetchApi('/tasks', { method: 'POST', body: JSON.stringify(payload) });
                showToast(`Task created! ID: ${created.id}`, 'success');
                addModal.classList.add('hidden');
                document.getElementById('addTaskForm').reset();
                refreshAllData();
            } catch (err) {}
        });

        // Add Dependency Modal
        const depModal = document.getElementById('addDepModal');
        document.getElementById('btnAddDependencyModalBtn').addEventListener('click', () => depModal.classList.remove('hidden'));
        document.getElementById('btnCloseDepModal').addEventListener('click', () => depModal.classList.add('hidden'));
        document.getElementById('btnCancelDep').addEventListener('click', () => depModal.classList.add('hidden'));

        document.getElementById('addDepForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const taskId = document.getElementById('depTaskIdSelect').value;
            const dependsOnId = document.getElementById('depPrereqIdSelect').value;

            try {
                const res = await fetchApi('/dependencies', {
                    method: 'POST',
                    body: JSON.stringify({ taskId, dependsOnId })
                });
                showToast(res.message, 'success');
                depModal.classList.add('hidden');
                refreshAllData();
            } catch (err) {}
        });

        setupTrieSearch();
    }

    function updateDependencySelects(tasks) {
        const taskSelect = document.getElementById('depTaskIdSelect');
        const prereqSelect = document.getElementById('depPrereqIdSelect');
        taskSelect.innerHTML = '';
        prereqSelect.innerHTML = '';

        tasks.forEach(t => {
            const opt1 = document.createElement('option');
            opt1.value = t.id;
            opt1.textContent = `[${t.id}] ${t.title}`;
            taskSelect.appendChild(opt1);

            const opt2 = document.createElement('option');
            opt2.value = t.id;
            opt2.textContent = `[${t.id}] ${t.title}`;
            prereqSelect.appendChild(opt2);
        });
    }

    // TOAST NOTIFICATIONS
    function showToast(message, type = 'info') {
        const container = document.getElementById('toastContainer');
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        container.appendChild(toast);

        setTimeout(() => {
            toast.remove();
        }, 4000);
    }

    // THEME TOGGLE
    function initTheme() {
        const btn = document.getElementById('btnThemeToggle');
        const icon = document.getElementById('themeIcon');
        btn.addEventListener('click', () => {
            const current = document.documentElement.getAttribute('data-theme');
            const next = current === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-theme', next);
            icon.textContent = next === 'dark' ? '🌙' : '☀️';
        });
    }

    function escapeHtml(str) {
        return String(str || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }
});
