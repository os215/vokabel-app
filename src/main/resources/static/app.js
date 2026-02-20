// Vokabeltrainer - Mobile Web App
let currentListId = null
let currentList = null
let words = []
let practiceQueue = []
let current = null
let practiceReverse = false

// DOM refs
let listSelector, newListBtn, listEl, addForm, wordIn, transIn, startBtn, reverseToggle
let practiceCard, practiceSetup, cardWord, answerIn, checkBtn, nextBtn, markCorrect, feedback, stats, statsSummary, progressBar

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
      updateStatsSummary()
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
    updateStatsSummary()
    return
  }
  try {
    currentList = await apiGet(`/api/vocab/lists/${id}`)
    currentListId = currentList.id
    words = currentList.words || []
    localStorage.setItem('vokabel-app:currentListId', currentListId)
    renderList()
    renderStats()
    updateStatsSummary()
  } catch (e) {
    console.error('Failed to load list', e)
  }
}

function renderList() {
  listEl.innerHTML = ''
  if (words.length === 0) {
    listEl.innerHTML = '<li style="color:#94a3b8;padding:24px;text-align:center">Keine Vokabeln vorhanden<br><small>Füge neue Vokabeln hinzu</small></li>'
    return
  }
  words.forEach((w) => {
    const li = document.createElement('li')
    const span = document.createElement('span')
    span.textContent = `${escapeHtml(w.word)} — ${escapeHtml(w.translation)}`
    li.appendChild(span)

    // show alternatives with remove buttons (only for PowerUsers)
    if (w.alternatives && (Array.isArray(w.alternatives) ? w.alternatives.length : (w.alternatives + '').trim().length)) {
      const altWrap = document.createElement('div')
      altWrap.className = 'alternatives'
      const altList = Array.isArray(w.alternatives) ? w.alternatives.map(a => a.text || a) : (w.alternatives + '').split(',')
      altList.forEach((a) => {
        const badge = document.createElement('span')
        badge.className = 'alt-badge'
        badge.textContent = a.trim()

        // Only show remove button for PowerUsers
        if (window.isPowerUser) {
          const remove = document.createElement('button')
          remove.className = 'alt-remove'
          remove.textContent = 'x'
          remove.title = 'Alternative entfernen'
          remove.onclick = async () => {
            try {
              const newList = altList.filter(item => item.trim() !== a.trim())
              await apiPut(`/api/vocab/words/${w.id}`, { alternatives: newList.join(', ') })
              await loadCurrentList()
            } catch (e) {
              alert('Fehler beim Entfernen')
            }
          }
          badge.appendChild(remove)
        }
        altWrap.appendChild(badge)
      })
      li.appendChild(altWrap)
    }

    // Only show delete button for PowerUsers
    if (window.isPowerUser) {
      const del = document.createElement('button')
      del.textContent = 'Löschen'
      del.onclick = async () => {
        if (!confirm('Vokabel wirklich löschen?')) return
        try {
          await apiDelete(`/api/vocab/words/${w.id}`)
          await loadCurrentList()
        } catch (e) {
          alert('Fehler beim Löschen')
        }
      }
      li.appendChild(del)
    }

    listEl.appendChild(li)
  })
}

function escapeHtml(s) {
  return (s + '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

function updateStatsSummary() {
  if (!statsSummary) return
  const total = words.length
  const done = words.filter(w => w.attempts > 0).length
  if (total === 0) {
    statsSummary.textContent = 'Noch keine Vokabeln vorhanden'
  } else {
    statsSummary.textContent = `${total} Vokabeln · ${done} geübt`
  }
}

function renderStats() {
  const total = words.length
  const done = words.filter(w => w.attempts > 0).length
  const remaining = practiceQueue.length
  if (stats) {
    stats.textContent = `${remaining} übrig · ${total - remaining} von ${total} geschafft`
  }
}

function updateProgress() {
  if (!progressBar) return
  const total = words.length
  const remaining = practiceQueue.length
  const progress = total > 0 ? ((total - remaining) / total) * 100 : 0
  progressBar.style.width = progress + '%'
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
  feedback.className = 'feedback'
  answerIn.value = ''
  checkBtn.style.display = 'block'
  nextBtn.style.display = 'none'
  markCorrect.style.display = 'none'
  
  if (practiceQueue.length === 0) {
    cardWord.textContent = '🎉 Fertig!'
    answerIn.style.display = 'none'
    checkBtn.style.display = 'none'
    nextBtn.textContent = 'Nochmal üben'
    nextBtn.style.display = 'block'
    nextBtn.onclick = () => {
      practiceQueue = shuffle(words.slice())
      answerIn.style.display = 'block'
      nextBtn.textContent = 'Weiter'
      nextCard()
    }
    current = null
    return
  }
  
  current = practiceQueue.shift()
  cardWord.textContent = practiceReverse ? current.translation : current.word
  answerIn.placeholder = practiceReverse ? 'Wort eingeben...' : 'Übersetzung eingeben...'
  renderStats()
  updateProgress()
  setTimeout(() => answerIn.focus(), 100)
}

// Navigation
function switchPanel(panelName) {
  document.querySelectorAll('.content-panel').forEach(p => {
    p.classList.toggle('active', p.dataset.panel === panelName)
  })
  document.querySelectorAll('.nav-btn').forEach(b => {
    b.classList.toggle('active', b.dataset.tab === panelName)
  })
}

// Init
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
  practiceSetup = document.getElementById('practiceSetup')
  cardWord = document.getElementById('cardWord')
  answerIn = document.getElementById('answer')
  checkBtn = document.getElementById('check')
  nextBtn = document.getElementById('next')
  markCorrect = document.getElementById('markCorrect')
  feedback = document.getElementById('feedback')
  stats = document.getElementById('stats')
  statsSummary = document.getElementById('statsSummary')
  progressBar = document.getElementById('progressBar')

  // Navigation
  document.querySelectorAll('.nav-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      switchPanel(btn.dataset.tab)
      if (btn.dataset.tab === 'list') renderList()
    })
  })

  listSelector.onchange = () => loadCurrentList()

  // Only set up newListBtn handler if button exists (PowerUsers only)
  if (newListBtn) {
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
  }

  // Only set up addForm handler if form exists (PowerUsers only)
  if (addForm) {
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
      wordIn.focus()
      // Show success feedback
      const btn = addForm.querySelector('button')
      const originalText = btn.textContent
      btn.textContent = '✓ Hinzugefügt'
      setTimeout(() => btn.textContent = originalText, 1500)
    } catch (e) {
      alert('Fehler beim Hinzufügen der Vokabel')
    }
  }
  }

  startBtn.onclick = () => {
    if (words.length === 0) {
      alert('Keine Vokabeln vorhanden.')
      return
    }
    practiceReverse = reverseToggle.checked
    practiceQueue = shuffle(words.slice())
    practiceSetup.style.display = 'none'
    practiceCard.style.display = 'block'
    checkBtn.style.display = 'block'
    nextBtn.style.display = 'none'
    nextCard()
  }

  checkBtn.onclick = async () => {
    if (!current) return
    const ans = answerIn.value.trim().toLowerCase()
    const correct = (practiceReverse ? current.word : current.translation).trim().toLowerCase()
    // normalize alternatives whether they're an array of objects or a comma-separated string
    const alternativesList = Array.isArray(current.alternatives)
      ? current.alternatives.map(a => (a && a.text ? a.text : (typeof a === 'string' ? a : '')).trim().toLowerCase()).filter(Boolean)
      : (current.alternatives || '').split(',').map(s => s.trim().toLowerCase()).filter(Boolean)
    current.attempts = (current.attempts || 0) + 1
    
    checkBtn.style.display = 'none'
    nextBtn.style.display = 'block'
    
    const matchedDirect = ans === correct
    const matchedAlt = alternativesList.includes(ans)
    if (matchedDirect || matchedAlt) {
      if (matchedAlt && !matchedDirect) {
        feedback.textContent = `✓ Richtig (Alternative) — richtig: ${practiceReverse ? current.word : current.translation}`
      } else {
        feedback.textContent = '✓ Richtig!'
      }
      feedback.className = 'feedback correct'
      current.correct = (current.correct || 0) + 1
    } else {
      feedback.textContent = `✗ Falsch — richtig: ${practiceReverse ? current.word : current.translation}`
      feedback.className = 'feedback incorrect'
      markCorrect.style.display = 'inline-block'
    }
    try {
      await apiPut(`/api/vocab/words/${current.id}`, { correct: current.correct, attempts: current.attempts })
    } catch (e) {
      console.error('Failed to update word stats', e)
    }
    renderStats()
  }

  // Mark as correct button handler
  markCorrect.onclick = async () => {
    if (!current) return
    const given = answerIn.value.trim()
    // build list of strings from either array of alternative objects or CSV
    let list = []
    if (Array.isArray(current.alternatives)) {
      list = current.alternatives.map(a => (a && a.text) ? a.text : (typeof a === 'string' ? a : '')).filter(Boolean)
    } else {
      list = (current.alternatives || '').split(',').map(s => s.trim()).filter(Boolean)
    }
    if (given) {
      if (!list.map(s => s.toLowerCase()).includes(given.toLowerCase())) {
        list.push(given)
      }
    }
    const altStr = list.join(', ')
    // represent alternatives in-memory as array of {text:...} for later checks
    current.alternatives = list.map(t => ({ text: t }))
    current.correct = (current.correct || 0) + 1
    feedback.textContent = '✓ Richtig (markiert)'
    feedback.className = 'feedback correct'
    markCorrect.style.display = 'none'
    try {
      await apiPut(`/api/vocab/words/${current.id}`, { correct: current.correct, attempts: current.attempts, alternatives: altStr })
    } catch (e) {
      console.error('Failed to update word stats', e)
    }
    renderStats()
  }

  nextBtn.onclick = () => nextCard()

  // Enter key to check/next
  answerIn.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      if (checkBtn.style.display !== 'none') {
        checkBtn.click()
      } else {
        nextBtn.click()
      }
    }
  })

  loadLists()
})
