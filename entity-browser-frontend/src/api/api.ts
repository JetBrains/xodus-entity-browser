import {BaseAPI, Http} from "./http";
import {ApplicationState, Database, EntityType} from "./backend-types";

class Api {

  // @ts-ignore
  apiContext: string = AppBuildConfig.apiContext;
  // @ts-ignore
  appContext: string = AppBuildConfig.appContext;

  http: Http = new Http(this.apiContext);

  get system(): SystemStateApi {
    return new SystemStateApi(this.http);
  }

  database(db: Database): DatabaseApi {
    return new DatabaseApi(this.http, db.uuid);
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
      params: {
        op: db.opened ? "start" : "stop"
      }
    });
  }
}

export class DatabaseApi extends BaseAPI {

  databaseUuid: string

  constructor(http: Http, databaseId: string) {
    super(http, '/dbs');
    this.databaseUuid = databaseId;
  }

  async entityTypes(): Promise<EntityType[]> {
    return this.http.get(this.url + "/" + this.databaseUuid + "/types", null);
  }

}


export default new Api();
