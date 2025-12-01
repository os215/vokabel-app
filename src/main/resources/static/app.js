// Einfacher Vokabeltrainer, speichert in localStorage
const STORAGE_KEY = 'vokabel-app:words'
let words = []
let practiceQueue = []
let current = null

// DOM
const listEl = document.getElementById('list')
const addForm = document.getElementById('addForm')
const wordIn = document.getElementById('word')
const transIn = document.getElementById('translation')
const startBtn = document.getElementById('startPractice')
const reverseToggle = document.getElementById('reverseToggle')
let practiceReverse = false
const exportBtn = document.getElementById('export')
const importBtn = document.getElementById('importBtn')
const importFile = document.getElementById('importFile')
const practiceCard = document.getElementById('practiceCard')
const cardWord = document.getElementById('cardWord')
const answerIn = document.getElementById('answer')
const checkBtn = document.getElementById('check')
const nextBtn = document.getElementById('next')
const feedback = document.getElementById('feedback')
const stats = document.getElementById('stats')

// helper to render list when the panel is visible
function ensureListRendered(){
  // renderList idempotent
  renderList()
}

document.querySelectorAll('.tab').forEach(btn=>{btn.addEventListener('click', ()=>{
  if(btn.dataset.tab==='list') ensureListRendered()
})
})

// Laden / Speichern
function load(){
  try{
    words = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
  }catch(e){words = []}
}
function save(){localStorage.setItem(STORAGE_KEY, JSON.stringify(words))}

// UI
function renderList(){
  listEl.innerHTML = ''
  words.forEach((w, i)=>{
    const li = document.createElement('li')
    li.innerHTML = `<span>${escapeHtml(w.word)} — ${escapeHtml(w.translation)}</span>`
    const right = document.createElement('div')
    const del = document.createElement('button')
    del.textContent = 'Löschen'
    del.onclick = ()=>{words.splice(i,1);save();renderList()}
    right.appendChild(del)
    li.appendChild(right)
    listEl.appendChild(li)
  })
}

function escapeHtml(s){return (s+'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')}

addForm.onsubmit = e=>{
  e.preventDefault()
  const w = wordIn.value.trim()
  const t = transIn.value.trim()
  if(!w||!t) return
  words.push({word:w,translation:t,correct:0,attempts:0})
  save();renderList();wordIn.value='';transIn.value='';
}

startBtn.onclick = ()=>{
  if(words.length===0){alert('Keine Vokabeln vorhanden.');return}
  practiceReverse = !!(reverseToggle && reverseToggle.checked)
  practiceQueue = shuffle(words.slice())
  // show practice panel and hide list
  document.querySelectorAll('.tab').forEach(b=>b.classList.toggle('active', b.dataset.tab==='practice'))
  document.querySelectorAll('.tab-panel').forEach(p=>p.style.display = p.dataset.panel==='practice' ? 'block' : 'none')
  document.getElementById('practiceCard').style.display = 'block'
  nextCard()
}

// tab switching
document.querySelectorAll('.tab').forEach(btn=>{
  btn.addEventListener('click', ()=>{
    document.querySelectorAll('.tab').forEach(b=>b.classList.toggle('active', b===btn))
    document.querySelectorAll('.tab-panel').forEach(p=>p.style.display = p.dataset.panel===btn.dataset.tab ? 'block' : 'none')
  })
})

checkBtn.onclick = ()=>{
  if(!current) return
  const ans = answerIn.value.trim().toLowerCase()
  const correct = (practiceReverse ? current.word : current.translation).trim().toLowerCase()
  current.attempts = (current.attempts||0)+1
  if(ans === correct){
    feedback.textContent = 'Richtig!'
    current.correct = (current.correct||0)+1
  }else{
    feedback.textContent = `Falsch — richtig: ${practiceReverse ? current.word : current.translation}`
  }
  save();renderStats();
}

nextBtn.onclick = ()=> nextCard()

function nextCard(){
  feedback.textContent=''
  answerIn.value=''
  if(practiceQueue.length===0){
    cardWord.textContent='Fertig!'
    current = null
    return
  }
  current = practiceQueue.shift()
  cardWord.textContent = practiceReverse ? current.translation : current.word
  answerIn.placeholder = practiceReverse ? 'Original eingeben' : 'Übersetzung eingeben'
  renderStats()
}

function renderStats(){
  const total = words.length
  const done = words.filter(w=>w.attempts>0).length
  stats.textContent = `Vokabeln: ${total} · bearbeitet: ${done}`
}

exportBtn.onclick = ()=>{
  const data = JSON.stringify(words, null, 2)
  const blob = new Blob([data],{type:'application/json'})
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = 'vokabeln.json'
  document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(url)
}

importBtn.onclick = ()=> importFile.click()
importFile.onchange = e=>{
  const f = e.target.files[0]
  if(!f) return
  const reader = new FileReader()
  reader.onload = ()=>{
    try{
      const data = JSON.parse(reader.result)
      if(Array.isArray(data)){
        // merge
        data.forEach(d=>{
          if(d.word && d.translation) words.push({word:d.word,translation:d.translation,correct:d.correct||0,attempts:d.attempts||0})
        })
        save();renderList();
      }else alert('Ungültiges Format')
    }catch(err){alert('Fehler beim Einlesen')}
  }
  reader.readAsText(f)
}

function shuffle(a){for(let i=a.length-1;i>0;i--){const j=Math.floor(Math.random()*(i+1));[a[i],a[j]]=[a[j],a[i]]}return a}

// init
load();renderList();renderStats()

// register service worker for PWA
if('serviceWorker' in navigator){
  navigator.serviceWorker.register('./sw.js').catch(()=>{})
}
