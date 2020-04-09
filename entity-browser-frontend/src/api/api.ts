import {BaseAPI, Http} from "./http";
import {ApplicationState, Database} from "./backend-types";

class Api {

  // @ts-ignore
  apiContext: string = AppBuildConfig.apiContext;
  // @ts-ignore
  appContext: string = AppBuildConfig.appContext;

  http: Http = new Http(this.apiContext);

  get system(): SystemStateApi {
    return new SystemStateApi(this.http);
  }

}

class SystemStateApi extends BaseAPI {

  constructor(http: Http) {
    super(http, '/dbs');
  }

  async state(): Promise<ApplicationState> {
    return this.get()
  }

  async deleteDB(db: Database) {
    return this.http.delete(this.url + "/" + db.uuid, null);
  }

  async startOrStop(db: Database) {
    return this.http.post(this.url + "/" + db.uuid, JSON.stringify(db), {
        params : {
          op: db.opened ? "start" : "stop"
        }
    });
  }

}


export default new Api();
