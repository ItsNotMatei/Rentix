import api from './api';

export const buyNow = (listingId) =>
    api.post(`/api/payments/buy-now/${listingId}`).then(r => r.data);

export const payAcceptedOffer = (offerId) =>
    api.post(`/api/payments/offer-checkout/${offerId}`).then(r => r.data);

export const getMyOrders = () =>
    api.get('/api/payments/orders').then(r => r.data);

export const syncOrderPayment = (orderId) =>
    api.post(`/api/payments/orders/${orderId}/sync-payment`).then(r => r.data);

export const shipOrder = (orderId) =>
    api.post(`/api/payments/orders/${orderId}/ship`);

export const confirmDelivery = (orderId) =>
    api.post(`/api/payments/orders/${orderId}/confirm-delivery`);

export const acceptPayout = (orderId) =>
    api.post(`/api/payments/orders/${orderId}/accept-payout`).then((r) => r.data);

export const rentalCheckout = (listingId, startDate, endDate) =>
    api.post(`/api/payments/rental-checkout?listingId=${listingId}&startDate=${startDate}&endDate=${endDate}`).then((r) => r.data);

export const createOffer = (listingId, amount, conversationId) =>
    api.post('/api/offers', { listingId, amount, conversationId }).then(r => r.data);

export const acceptOffer = (offerId) =>
    api.post(`/api/offers/${offerId}/accept`).then(r => r.data);

export const rejectOffer = (offerId) =>
    api.post(`/api/offers/${offerId}/reject`).then(r => r.data);

export const counterOffer = (offerId, amount) =>
    api.post(`/api/offers/${offerId}/counter`, { amount }).then(r => r.data);
