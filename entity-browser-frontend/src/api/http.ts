import ExtendableError from 'es6-error';
import queryString from 'querystring';
import axios from 'axios';
import FileSaver from 'file-saver';
import {error} from '../components/notifications/notifications';

export const defaultFetchConfig = {
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json'
  },
  credentials: 'same-origin'
};


export class HTTPError extends ExtendableError {

  data: any;
  status: number;

  constructor(response: Response, data = {}) {
    super(`${response.status} ${response.statusText || ''}`);
    this.data = data;
    this.status = response.status;
  }
}

export class Http {

  url: string;

  constructor(url: string) {
    this.url = url;
  }

  async get(url: string, fetchConfig: any) {
    return fetch(this.urlOf(url, fetchConfig), {
      ...defaultFetchConfig, ...fetchConfig,
      ...{method: 'GET'}
    }).then((resp: Response) => this._processResponse(resp));
  }

  async post(url: string, body: any, fetchConfig: any) {
    return fetch(this.urlOf(url, fetchConfig), {
      ...defaultFetchConfig, ...fetchConfig,
      ...{
        method: 'POST',
        body: JSON.stringify(body)
      }
    }).then((resp: Response) => this._processResponse(resp));
  }

  async put(url: string, body: any, fetchConfig: any) {
    return fetch(this.urlOf(url, fetchConfig), {
      ...defaultFetchConfig, ...fetchConfig,
      ...{
        method: 'PUT',
        body: body
      }
    }).then((resp: Response) => this._processResponse(resp));
  }

  async delete(url: string, fetchConfig: any) {
    return fetch(this.urlOf(url, fetchConfig), {
      ...defaultFetchConfig, ...fetchConfig,
      ...{method: 'DELETE'}
    });
  }

  private urlOf(url: string, fetchConfig: any): string {
    let queryParams = (fetchConfig || {}).params ? (queryString.stringify(fetchConfig.params)) : "";
    return this.url + url + "?" + queryParams;
  }

  static _isErrorStatus(status: number) {
    return status < 200 || status >= 300;
  }

  async _processResponse(response: Response) {
    const contentType = response.headers.get('content-type');
    const isJson = contentType && contentType.indexOf('application/json') !== -1;

    if (Http._isErrorStatus(response.status)) {
      let resJson;
      try {
        resJson = await (isJson ? response.json() : response.text());
      } catch (err) {
        // noop
      }

      throw new HTTPError(response, resJson);
    }

    try {
      return await (isJson ? response.json() : {data: await response.text()});
    } catch (err) {
      return response;
    }
  }


}

export class BaseAPI {
  http: Http;
  url: string;

  constructor(http: Http, url: string) {
    this.http = http;
    this.url = url;
  }


  async get(fetchConfig: any = {}) {
    return this.http.get(this.url, fetchConfig);
  }

  async post(body: any, fetchConfig: any) {
    return this.http.post(this.url, body, fetchConfig);
  }

  async put(body: any, fetchConfig: any) {
    return this.http.put(this.url, body, fetchConfig);
  }

  async delete(body: any, fetchConfig: any) {
    return this.http.delete(this.url, fetchConfig);
  }


  async download(path: String, params: any, errorMessage: string) {
    // Fetch the dynamically generated document from the server.
    let response;
    try {
      response = await axios.get(
        `${this.http.url}${this.url}${path}`,
        {
          params,
          responseType: 'blob',
        }
      );
    } catch (response) {
      error(errorMessage, response);
      return;
    }
    // Log somewhat to show that the browser actually exposes the custom HTTP header
    const fileNameHeader = 'content-disposition';
    const suggestedFileName = response.headers[fileNameHeader];
    const effectiveFileName = suggestedFileName ? suggestedFileName.split('"')[1] : "unknown";
    // Let the user save the file.
    FileSaver.saveAs(response.data, effectiveFileName);
  }
}
