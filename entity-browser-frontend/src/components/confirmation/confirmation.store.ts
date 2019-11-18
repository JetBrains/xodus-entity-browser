import {action, observable} from "mobx";


class ConfirmationStore {

  @observable show: boolean = false;
  @observable action?: () => Promise<any> | any = undefined;
  @observable title?: string;
  @observable message?: string;

  @action
  confirm(text: string, title: string, onConfirm: () => Promise<any> | any) {
    this.show = true;
    this.action = onConfirm;
    this.message = text;
    this.title = title;
  }

}

let store = new ConfirmationStore();
export const confirmationStore = store;

export const confirm = (args: {
  title?: string
  text: string,
  onConfirm: () => Promise<any> | any,
}) => store.confirm(args.text, args.title || 'Confirm operation', args.onConfirm);
