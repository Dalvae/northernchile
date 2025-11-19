/**
 * Load CSS asynchronously to prevent render-blocking
 * Based on: https://www.filamentgroup.com/lab/load-css-simpler/
 */
export default defineNuxtPlugin(() => {
  if (import.meta.server) return

  // Find all stylesheets with data-async attribute
  const asyncStylesheets = document.querySelectorAll<HTMLLinkElement>('link[rel="stylesheet"][data-async]')

  asyncStylesheets.forEach((link) => {
    // Load stylesheet asynchronously
    link.media = 'print'
    link.onload = function () {
      link.media = 'all'
      link.onload = null
    }
  })
})
