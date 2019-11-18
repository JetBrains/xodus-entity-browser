import {action, observable} from "mobx";
import {Database} from "../api/backend-types";

class Store {

  @observable pageId: string = '';

  @observable readonly: boolean = true;
  @observable databases: Database[] = [];

  @action
  setPage(pageId: string) {
    this.pageId = pageId;
  }
}

// @ts-ignore
const store = window.store = new Store();

export default store;
