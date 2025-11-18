# TODO - Northern Chile Platform

## Media Management - Future Optimizations

### Gallery Pagination (Low Priority)
**Issue**: Gallery endpoints (`/api/admin/media/tour/{tourId}/gallery` and `/api/admin/media/schedule/{scheduleId}/gallery`) return all photos without pagination.

**Current State**:
- Returns `List<MediaRes>` (all photos)
- Works fine for typical galleries (5-50 photos)
- Needed for drag-and-drop reordering UX

**Potential Issues**:
- If tours have 100+ photos, could be slow
- Currently not a problem with typical usage

**Solution Options** (if becomes issue):
1. Add pagination to gallery endpoints (but loses full-view reordering)
2. Implement lazy loading with infinite scroll
3. Add "Load More" button with chunked loading
4. Virtualization with `@vueuse/virtual-list`

**Decision**: Monitor in production. Only implement if users report slowness.

---

## Other TODOs
(Empty - add future tasks here)
