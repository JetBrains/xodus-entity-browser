import {action, observable} from "mobx";

type Notification = {
  message: string,
  open: boolean,
  type: string,
  id: number,
  ttl: number
};

class NotificationsStore {
  @observable notifications: Notification[] = [];

  @action
  notify(text: string, type: string = 'info', ttl: number = 5000) {
    const id = Date.now();

    this.notifications.push({
      message: text,
      open: true,
      type,
      id,
      ttl
    });

    return id;
  }

  @action
  hide(id: number) {
    this.notifications.find(n => n.id === id)!.open = false;

    setTimeout(() => {
      this.remove(id);
    }, 300)
  }

  @action
  remove(id: number) {
    this.notifications = this.notifications.filter(n => n.id !== id);
  }
}

export default new NotificationsStore();
