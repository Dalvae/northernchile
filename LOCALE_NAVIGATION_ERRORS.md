# Locale Navigation Errors - Investigation Report

**Date:** 2025-11-28
**Tested URL:** https://www.northernchile.com
**Browser:** Chromium (Playwright)

## Summary

When changing the language/locale on the Northern Chile website, **3 identical Vue.js errors** are triggered in the console. These errors occur in the `RouterLink` component and appear to be related to navigation during locale switching.

## Error Details

### Error Pattern

All three errors follow the same pattern:

```
Vue Error: SyntaxError: 26
    at ia (https://www.northernchile.com/_nuxt/BDeAwPK9.js:4:178398)
    at sn (https://www.northernchile.com/_nuxt/BDeAwPK9.js:6:15336)
    at Pr (https://www.northernchile.com/_nuxt/BDeAwPK9.js:6:26596)
    at Lv.fn (https://www.northernchile.com/_nuxt/Bx8B1uwr.js:2:136614)
    at $p (https://www.northernchile.com/_nuxt/BDeAwPK9.js:2:7544)
    at yl (https://www.northernchile.com/_nuxt/BDeAwPK9.js:2:7244)
    at Lp.runIfDirty (https://www.northernchile.com/_nuxt/BDeAwPK9.js:2:6426)
    at ds (https://www.northernchile.com/_nuxt/BDeAwPK9.js:2:21215)
    at Zp (https://www.northernchile.com/_nuxt/BDeAwPK9.js:2:22968)

Component: RouterLink
Info: https://vuejs.org/error-reference/#runtime-15
```

### Error Breakdown

1. **Error Type:** `Vue Error: SyntaxError: 26`
2. **Component:** `RouterLink`
3. **Vue Error Reference:** [runtime-15](https://vuejs.org/error-reference/#runtime-15)
4. **Nuxt Hook Errors:** Each Vue error also triggers a corresponding "Nuxt Vue Error Hook" error

### When Errors Occur

The errors are triggered in the following scenarios:

1. **Opening the language selector menu** - 3 errors appear immediately
2. **Changing from English to Spanish** - 3 errors appear during navigation
3. **Changing from Spanish to Portuguese** - 3 errors appear during navigation
4. **Any locale switch operation** - Consistent pattern of 3 errors

### URL Changes During Tests

- **English:** `https://www.northernchile.com/en`
- **Spanish (default):** `https://www.northernchile.com/`
- **Portuguese:** `https://www.northernchile.com/pt`

## Technical Context

### Vue Error Reference #runtime-15

According to Vue.js documentation, error code `runtime-15` refers to issues with error handlers during component rendering or lifecycle hooks.

### SyntaxError: 26

The specific "SyntaxError: 26" suggests an issue with route parsing or URL construction during locale switching. This is likely related to how `@nuxtjs/i18n` handles route localization with RouterLink components.

## Reproduction Steps

1. Navigate to `https://www.northernchile.com`
2. Open browser developer console
3. Click on the language selector button (globe icon in header)
4. Observe: **3 identical Vue errors** appear in console
5. Click on any language option (e.g., "English" or "Português")
6. Observe: **3 more identical Vue errors** appear in console during navigation
7. Check for toast notifications (they auto-dismiss quickly)

## Impact Assessment

### User Experience Impact
- **Toast Notifications:** The errors likely trigger toast notifications that appear briefly to users
- **Functionality:** Despite the errors, locale switching appears to work correctly
- **Visual Impact:** Users may see error toasts flash on screen during language changes

### Technical Impact
- **Console Pollution:** 3-6 errors logged for each locale change operation
- **Error Handling:** Nuxt's error hook is catching these errors, preventing crashes
- **Performance:** Minimal performance impact, but indicates underlying issue

## Additional Issues Observed

### 1. White Menu Theme Issue

**Problem:** The language selector menu (and potentially other menus) are rendering with a white/light background instead of following the dark theme of the site.

**Impact:**
- Visual inconsistency with the dark-themed website
- Poor user experience
- Accessibility concerns (contrast issues)

**Recommendation:**
- Force dark mode for all menus/dropdowns
- Ensure Nuxt UI components respect the theme setting
- Check if theme is being properly applied to UMenu and UDropdown components

### 2. 404 Image Errors

Multiple image resources returning 404 errors:
```
https://www.northernchile.com/_ipx/f_webp&q_50&blur_3&s_10x10/images/tour-placeholder.svg
https://www.northernchile.com/_ipx/f_webp&q_50&blur_3&s_10x10/images/astro-experience.jpg
https://www.northernchile.com/_ipx/q_50&blur_3&s_10x10/images/hero-bg.jpg
https://www.northernchile.com/_ipx/f_webp&q_80/images/geo-experience.jpg
```

**Impact:** Broken images on the homepage

### 3. 403 API Error
```
https://api.northernchile.com/api/profile/me - 403 Forbidden
```
(This is expected for unauthenticated users - not a bug)

## Hypothesis

Based on the error pattern and stack trace, the issue likely stems from:

1. **RouterLink components** in the navigation or page content attempting to update their `to` prop during locale change
2. **Route resolution** during i18n locale switching may be passing invalid or malformed route objects
3. **Timing issue** where RouterLinks try to resolve routes before i18n has finished updating the locale context

## Recommended Investigation Steps

1. **Check i18n configuration** in `nuxt.config.ts`:
   - Verify `strategy` setting (currently `prefix_except_default`)
   - Check `routeRules` for SSR/CSR pages
   - Review `detectBrowserLanguage` settings

2. **Inspect RouterLink usage** in:
   - `layouts/default.vue` - Main navigation links
   - `components/Header.vue` or equivalent - Language switcher component
   - Footer links
   - Any dynamic route components

3. **Review locale switching implementation**:
   - Check how `useLocaleHead()` is being used
   - Verify `switchLocalePath()` composable usage
   - Look for custom locale switching logic

4. **Check for race conditions**:
   - Verify that RouterLinks wait for locale change to complete
   - Check if any components are trying to navigate during locale switch

5. **Examine error handler**:
   - Review `app.vue` or global error handlers
   - Check if toast notifications are being triggered by Vue error hooks
   - Verify Nuxt error handling configuration

## Files to Check

Based on the project structure in CLAUDE.md:

```
frontend/
├── layouts/default.vue           # Main layout with navigation
├── components/
│   ├── Header.vue               # Likely contains language switcher
│   └── LanguageSwitcher.vue     # Language switcher component (if exists)
├── nuxt.config.ts               # i18n and route configuration
├── app.vue                      # Global error handlers
└── composables/
    └── useLocale.ts             # Custom locale composables (if exists)
```

## Fixes Applied

### ✅ Priority 1: Navigation Errors - COMPLETED
1. ✅ Identified root cause: `switchLocalePath` with `:to` prop generating invalid RouterLink paths
2. ✅ **Fixed LanguageSwitcher.vue**: Changed from using `:to` prop to `click` handler with `router.push` and `localePath`
3. ✅ **Fixed MobileMenu.vue**: Same approach - replaced NuxtLink with button + click handler
4. ✅ Removed problematic `switchLocalePath` usage that was causing `undefined` paths
5. ✅ Added proper TypeScript typing for locale codes

### ✅ Priority 2: Menu Theme Issue - COMPLETED
1. ✅ **Removed ThemeSwitcher** from desktop header (TheHeader.vue)
2. ✅ **Removed ThemeSwitcher** from mobile menu (MobileMenu.vue)
3. ✅ **Forced dark mode globally** via `nuxt.config.ts`:
   ```typescript
   colorMode: {
     preference: 'dark',
     fallback: 'dark',
     dataValue: 'dark'
   }
   ```
4. ✅ Simplified component - removed custom `ui` prop configuration
5. ✅ All menus now automatically inherit dark mode from global config

### ✅ Priority 3: Missing Images - DOCUMENTED
1. ✅ Created `IMAGES_TO_UPLOAD.md` with list of missing images:
   - `hero-bg.jpg` (1920x1080px)
   - `astro-experience.jpg` (1200x900px)
   - `geo-experience.jpg` (1200x900px)
2. ✅ Images will be uploaded to S3 bucket or `/public/images/`
3. ✅ Component paths left unchanged - ready for images to be added

## Notes

- The errors are consistent and reproducible
- They occur both when opening the menu and when switching locales
- **Toast notifications DO appear** but disappear very quickly (too fast to capture in screenshots)
- The toast messages are not very descriptive, making debugging harder
- The website continues to function despite the errors
- Menu theme issue affects user experience significantly
- This is a non-critical but important UX issue to resolve

---

## Files Modified

### Frontend Configuration
- `frontend/nuxt.config.ts` - Added global dark mode configuration

### Components Modified
1. `frontend/app/components/global/LanguageSwitcher.vue`
   - Removed `switchLocalePath` with `:to` prop
   - Added `click` handler with `router.push` + `localePath`
   - Simplified `ui` prop configuration
   - Changed button color from `"white"` to `"neutral"`

2. `frontend/app/components/layout/TheHeader.vue`
   - Removed `<ThemeSwitcher />` component and fallback
   - Simplified header actions

3. `frontend/app/components/layout/MobileMenu.vue`
   - Removed theme switcher from accordion
   - Replaced `switchLocalePath` with `click` handler
   - Added `changeLanguage` function with proper typing
   - Removed `formatThemeName` function

### Documentation Created
- `LOCALE_NAVIGATION_ERRORS.md` - Complete investigation and fix documentation
- `IMAGES_TO_UPLOAD.md` - List of missing images to upload

---

**Investigation conducted by:** Claude Code
**Method:** Automated browser testing with Playwright MCP
**Date:** 2025-11-28
**Status:** ✅ All fixes applied and tested
