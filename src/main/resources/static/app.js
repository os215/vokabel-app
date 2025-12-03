// Vokabeltrainer with server-side storage
let currentListId = null
let currentList = null
let words = []
let practiceQueue = []
let current = null

// DOM refs (set after DOMContentLoaded)
let listSelector, newListBtn, listEl, addForm, wordIn, transIn, startBtn, reverseToggle
let practiceCard, cardWord, answerIn, checkBtn, nextBtn, feedback, stats
let practiceReverse = false

// API helpers
async function apiGet(url) {
  const res = await fetch(url)
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json()
}

async function apiPost(url, body) {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json()
}

async function apiPut(url, body) {
  const res = await fetch(url, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json()
}

async function apiDelete(url) {
  const res = await fetch(url, { method: 'DELETE' })
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
}

// Load lists
async function loadLists() {
  try {
    const lists = await apiGet('/api/vocab/lists')
    listSelector.innerHTML = ''
    if (lists.length === 0) {
      listSelector.innerHTML = '<option value="">Keine Listen vorhanden</option>'
      currentListId = null
      words = []
      renderList()
      return
    }
    lists.forEach(list => {
      const opt = document.createElement('option')
      opt.value = list.id
      opt.textContent = list.name
      listSelector.appendChild(opt)
    })
    const storedId = localStorage.getItem('vokabel-app:currentListId')
    if (storedId && lists.find(l => l.id == storedId)) {
      listSelector.value = storedId
    } else {
      listSelector.value = lists[0].id
    }
    await loadCurrentList()
  } catch (e) {
    console.error('Failed to load lists', e)
    listSelector.innerHTML = '<option value="">Fehler beim Laden</option>'
  }
}

async function loadCurrentList() {
  const id = listSelector.value
  if (!id) {
    currentListId = null
    currentList = null
    words = []
    renderList()
    return
  }
  try {
    currentList = await apiGet(`/api/vocab/lists/${id}`)
    currentListId = currentList.id
    words = currentList.words || []
    localStorage.setItem('vokabel-app:currentListId', currentListId)
    renderList()
    renderStats()
  } catch (e) {
    console.error('Failed to load list', e)
  }
}

function renderList() {
  listEl.innerHTML = ''
  if (words.length === 0) {
    listEl.innerHTML = '<li style="color:#999">Keine Vokabeln in dieser Liste</li>'
    return
  }
  words.forEach((w) => {
    const li = document.createElement('li')
    li.innerHTML = `<span>${escapeHtml(w.word)} — ${escapeHtml(w.translation)}</span>`
    const right = document.createElement('div')
    const del = document.createElement('button')
    del.textContent = 'Löschen'
    del.onclick = async () => {
      if (!confirm('Vokabel löschen?')) return
      try {
        await apiDelete(`/api/vocab/words/${w.id}`)
        await loadCurrentList()
      } catch (e) {
        alert('Fehler beim Löschen')
      }
    }
    right.appendChild(del)
    li.appendChild(right)
    listEl.appendChild(li)
  })
}

function escapeHtml(s) {
  return (s + '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

function renderStats() {
  const total = words.length
  const done = words.filter(w => w.attempts > 0).length
  stats.textContent = `Vokabeln: ${total} · bearbeitet: ${done}`
}

function shuffle(a) {
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]]
  }
  return a
}

function nextCard() {
  feedback.textContent = ''
  answerIn.value = ''
  if (practiceQueue.length === 0) {
    cardWord.textContent = 'Fertig!'
    current = null
    return
  }
  current = practiceQueue.shift()
  cardWord.textContent = practiceReverse ? current.translation : current.word
  answerIn.placeholder = practiceReverse ? 'Original eingeben' : 'Übersetzung eingeben'
  renderStats()
}

// Init after DOM loaded
document.addEventListener('DOMContentLoaded', () => {
  listSelector = document.getElementById('listSelector')
  newListBtn = document.getElementById('newListBtn')
  listEl = document.getElementById('list')
  addForm = document.getElementById('addForm')
  wordIn = document.getElementById('word')
  transIn = document.getElementById('translation')
  startBtn = document.getElementById('startPractice')
  reverseToggle = document.getElementById('reverseToggle')
  practiceCard = document.getElementById('practiceCard')
  cardWord = document.getElementById('cardWord')
  answerIn = document.getElementById('answer')
  checkBtn = document.getElementById('check')
  nextBtn = document.getElementById('next')
  feedback = document.getElementById('feedback')
  stats = document.getElementById('stats')

  listSelector.onchange = () => loadCurrentList()

  newListBtn.onclick = async () => {
    const name = prompt('Name der neuen Liste:')
    if (!name) return
    try {
      await apiPost('/api/vocab/lists', { name })
      await loadLists()
    } catch (e) {
      alert('Fehler beim Erstellen der Liste')
    }
  }

  addForm.onsubmit = async (e) => {
    e.preventDefault()
    const w = wordIn.value.trim()
    const t = transIn.value.trim()
    if (!w || !t) return
    if (!currentListId) {
      alert('Bitte wähle oder erstelle eine Liste')
      return
    }
    try {
      await apiPost(`/api/vocab/lists/${currentListId}/words`, { word: w, translation: t })
      await loadCurrentList()
      wordIn.value = ''
      transIn.value = ''
    } catch (e) {
      alert('Fehler beim Hinzufügen der Vokabel')
    }
  }

  startBtn.onclick = () => {
    if (words.length === 0) {
      alert('Keine Vokabeln vorhanden.')
      return
    }
    practiceReverse = !!(reverseToggle && reverseToggle.checked)
    practiceQueue = shuffle(words.slice())
    document.querySelectorAll('.tab').forEach(b => b.classList.toggle('active', b.dataset.tab === 'practice'))
    document.querySelectorAll('.tab-panel').forEach(p => p.style.display = p.dataset.panel === 'practice' ? 'block' : 'none')
    practiceCard.style.display = 'block'
    nextCard()
  }

  document.querySelectorAll('.tab').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.tab').forEach(b => b.classList.toggle('active', b === btn))
      document.querySelectorAll('.tab-panel').forEach(p => p.style.display = p.dataset.panel === btn.dataset.tab ? 'block' : 'none')
      if (btn.dataset.tab === 'list') renderList()
    })
  })

  checkBtn.onclick = async () => {
    if (!current) return
    const ans = answerIn.value.trim().toLowerCase()
    const correct = (practiceReverse ? current.word : current.translation).trim().toLowerCase()
    current.attempts = (current.attempts || 0) + 1
    if (ans === correct) {
      feedback.textContent = 'Richtig!'
      current.correct = (current.correct || 0) + 1
    } else {
      feedback.textContent = `Falsch — richtig: ${practiceReverse ? current.word : current.translation}`
    }
    try {
      await apiPut(`/api/vocab/words/${current.id}`, { correct: current.correct, attempts: current.attempts })
    } catch (e) {
      console.error('Failed to update word stats', e)
    }
    renderStats()
  }

  nextBtn.onclick = () => nextCard()

  loadLists()

  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('./sw.js').catch(() => { })
  }
})
