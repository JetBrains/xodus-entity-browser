import {observable} from "mobx";
import {Database} from "../api/backend-types";

class Store {

  @observable readonly: boolean = true;
  @observable databases: Database[] = [];

}

// @ts-ignore
const store = window.store = new Store();

export default store;
