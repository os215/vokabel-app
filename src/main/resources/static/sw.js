// Service worker disabled: no caching performed
self.addEventListener('install', evt=>{
  self.skipWaiting()
})
self.addEventListener('activate', evt=>{
  evt.waitUntil(clients.claim())
})
