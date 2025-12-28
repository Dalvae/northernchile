import { proxyDelete } from '../../../utils/apiProxy'

export default defineEventHandler(async (event): Promise<void> => {
  const itemId = getRouterParam(event, 'itemId')
  await proxyDelete(event, `/api/cart/items/${itemId}`, 'Failed to remove item from cart')
})
