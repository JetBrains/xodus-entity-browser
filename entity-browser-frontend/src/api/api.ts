import {BaseAPI, Http} from './http';
import {ApplicationState, Database, EntityType, SearchPager} from './backend-types';
import {error} from '../components/notifications/notifications';

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

  errorHandler = (r: Response) => {
    const invalidTokenErrorCode = 401;

    if (r.status === invalidTokenErrorCode) {
      error('Invalid token');
    } else if (r.status) {
      //@ts-ignore
      const data = r.data;
      if (data) {
        error(`Server respond with ${r.status}: ${data.message}`);
      }
    }

    return r;
  };

}

class SystemStateApi extends BaseAPI {

  constructor(http: Http) {
    super(http, '/dbs');
  }

  async state(): Promise<ApplicationState> {
    return this.get()
  }

  async deleteDB(db: Database) {
    return this.http.delete(this.url + '/' + db.uuid, null);
  }

  async newDB(db: Database) {
    return this.http.post(this.url, db, null);
  }

  async startOrStop(db: Database) {
    return this.http.post(this.url + '/' + db.uuid, JSON.stringify(db), {
      params: {
        op: db.opened ? 'start' : 'stop'
      }
    });
  }
}

export const PAGE_SIZE = 50

export class DatabaseApi extends BaseAPI {

  databaseUuid: string

  constructor(http: Http, databaseId: string) {
    super(http, '/dbs');
    this.databaseUuid = databaseId;
  }

  async entityTypes(): Promise<EntityType[]> {
    return this.http.get(this.url + '/' + this.databaseUuid + '/types', null);
  }

  async downloadBlob(entityId: string, blobName: string, asString: boolean) {
    const link = asString ? 'blobString' : 'blob';
    return this.download(`/${this.databaseUuid}/entities/${entityId}/${link}/${blobName}`,
      null, 'Can\'t download blob');
  }

  async entity(entityId: string) {
    return this.http.get(`${this.url}/${this.databaseUuid}/entities/${entityId}`, null);
  }

  async searchEntities(q: string, typeId: number, page: number = 1): Promise<SearchPager> {
    return this.http.get(this.url + '/' + this.databaseUuid + '/entities', {
      params: {
        q: q,
        id: typeId,
        offset: (page - 1) * PAGE_SIZE,
        pageSize: PAGE_SIZE
      }
    });
  }

}


export default new Api();
