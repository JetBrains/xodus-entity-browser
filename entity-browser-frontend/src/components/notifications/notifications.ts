import store from './notifications.store';

export * from './NotificationsWrapper';

export const info = (text: string, ttl?: number) => store.notify(text, 'info', ttl);
export const warning = (text: string, ttl?: number) => store.notify(text, 'warning', ttl);
export const error = (text: string, ttl?: number) => store.notify(text, 'error', ttl);
export const success = (text: string, ttl?: number) => store.notify(text, 'success', ttl);

// export const showInfo = (text: string, title?: string, pre: boolean = false) => store.showInfo(text, title, pre);

export const hide = (id: number) => store.hide(id);
