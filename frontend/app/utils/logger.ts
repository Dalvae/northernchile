/**
 * Production-safe logger utility.
 * Only outputs to console in development mode.
 */

const isDev = import.meta.dev

export const logger = {
  log: (...args: unknown[]) => {
    if (isDev) console.log(...args)
  },

  warn: (...args: unknown[]) => {
    if (isDev) console.warn(...args)
  },

  error: (...args: unknown[]) => {
    if (isDev) console.error(...args)
  },

  /**
   * Always log, even in production.
   * Use sparingly for critical errors that need tracking.
   */
  critical: (...args: unknown[]) => {
    console.error('[CRITICAL]', ...args)
  }
}

export default logger
